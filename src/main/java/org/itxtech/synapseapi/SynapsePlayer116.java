package org.itxtech.synapseapi;

import cn.nukkit.inventory.InventorySlotReference;
import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockDragonEgg;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntityLectern;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.command.Command;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityFullNames;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.knockback.KnockbackProfile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.event.inventory.ItemAttackDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.action.DropItemAction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.TakeResultAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.TransactionData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.item.NetworkItemHelper;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentOption;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.*;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.AnimatePacket.Action;
import cn.nukkit.network.protocol.AnimatePacket.SwingSource;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.ContainerType;
import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.ItemStackResponse;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.ClientChainData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.event.player.SynapsePlayerInputModeChangeEvent;
import org.itxtech.synapseapi.inventory.*;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.inventory.ItemStackContainerIdTranslator;
import org.itxtech.synapseapi.multiprotocol.common.inventory.LegacySetItemSlotData;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket.PlayerBlockAction;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.SharedConstants.EXPERIMENTAL_COMBAT_KNOCKBACK_TEST;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class SynapsePlayer116 extends SynapsePlayer113 {

	protected boolean inventoryOpen;

	protected boolean serverAuthoritativeInventory = SERVER_AUTHORITATIVE_INVENTORY;
	protected final InventoryManager inventoryManager = new InventoryManager(this);
	private boolean processingItemStackRequest;
	private boolean executingInventoryTransaction;
	private boolean closingAuthoritativeWindow;
	private boolean preparingAuthoritativeDisconnect;
	@Nullable
	private Inventory notifiedAuthoritativeCloseWindow;
	private boolean deferredWindowDeath;
	private boolean deferredWindowSave;
	private boolean deferredWindowSaveAsync;
	@Nullable
	private DeferredWindowClose deferredWindowClose;
	private boolean detachedTemporaryInventories;
	@Nullable
	private TemporaryInventoryRecovery temporaryInventoryRecovery;

	protected boolean serverAuthoritativeMovement = SERVER_AUTHORITATIVE_MOVEMENT;
	protected long clientTickRaw;
	private long clientTick = -1;
	private long clientTickDiff;
	private int clientTickTooFastCount;
	private int clientTickCheckCd;

	/**
	 * Server Authoritative Movement is required.
	 */
	protected boolean serverAuthoritativeBlockBreaking = SERVER_AUTHORITATIVE_BLOCK_BREAKING;
	protected BlockFace breakingBlockFace;

	protected int currentTickAttackPacketCount;
	@Nullable
	private final AttackAttempts inputAttackAttempts;

	private boolean emotedCurrentTick;

	public SynapsePlayer116(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.inputAttackAttempts = this.isMainThreadInputEnabled() ? new AttackAttempts(2) : null;
	}

	/** 到达原攻击计数点后才消费额度，所有输入阶段共用真实 tick 窗口。 */
	protected final boolean claimAttackAttempt() {
		return this.inputAttackAttempts == null ? ++this.currentTickAttackPacketCount < 2
				: this.inputAttackAttempts.claim(this.server.getTick());
	}

	public final int getInputAttackWindowTick() {
		return this.inputAttackAttempts == null ? Integer.MIN_VALUE : this.inputAttackAttempts.tick();
	}

	public final int getAttackAttemptsInCurrentWindow() {
		return this.inputAttackAttempts == null ? this.currentTickAttackPacketCount
				: this.inputAttackAttempts.count(this.server.getTick());
	}

	/**
	 * Returns an optional read-only display item without replacing the authoritative slot contents.
	 */
	@Nullable
	public Item getInventorySlotDisplay(Inventory inventory, int slot) {
		return null;
	}

	protected Boolean normalizeUseItemClientPrediction(Boolean clientPrediction) {
		if (this.isJavaClient() && Boolean.TRUE.equals(clientPrediction)) {
			return null;
		}
		return clientPrediction;
	}

	@Override
	protected void initEntity() {
		super.initEntity();

		if (isServerAuthoritativeInventoryEnabled()) {
			inventoryManager.initialize();
			if (namedTag.contains(TemporaryInventoryRecovery.NBT_KEY)) {
				temporaryInventoryRecovery = new TemporaryInventoryRecovery(this,
						namedTag.getList(TemporaryInventoryRecovery.NBT_KEY, CompoundTag.class));
			}
		}
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket116 startGamePacket = new StartGamePacket116();
		startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
		startGamePacket.netease = this.isNetEaseClient();
		startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
		startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
		startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.x = (float) this.x;
		startGamePacket.y = (float) this.y;
		startGamePacket.z = (float) this.z;
		startGamePacket.yaw = (float) this.yaw;
		startGamePacket.pitch = (float) this.pitch;
		startGamePacket.seed = -1;
		startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
		startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.difficulty = this.level.getDifficulty();
		startGamePacket.spawnX = (int) spawnPosition.x;
		startGamePacket.spawnY = (int) spawnPosition.y;
		startGamePacket.spawnZ = (int) spawnPosition.z;
		startGamePacket.hasAchievementsDisabled = true;
		startGamePacket.dayCycleStopTime = -1;
		startGamePacket.rainLevel = 0;
		startGamePacket.lightningLevel = 0;
		startGamePacket.commandsEnabled = this.isEnableClientCommand();
		startGamePacket.levelId = "";
		startGamePacket.worldName = this.getServer().getNetwork().getName();
		startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
		startGamePacket.gameRules = getSupportedRules();
		startGamePacket.isTexturePacksRequired = server.getForceResources();
		startGamePacket.isInventoryServerAuthoritative = isServerAuthoritativeInventoryEnabled();
		startGamePacket.isMovementServerAuthoritative = this.isNetEaseClient();
		startGamePacket.currentTick = 0;//this.server.getTick();
		startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
		return startGamePacket;
	}

	/**
	 * Returns a client-friendly gamemode of the specified real gamemode This
	 * function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that
	 * includes Survival, Creative and Adventure)
	 * <p>
	 * TODO: remove this when Spectator Mode gets added properly to MCPE
	 */
	private static int getClientFriendlyGamemode(int gamemode) {
		gamemode &= 0x03;
		if (gamemode == Player.SPECTATOR) {
			return Player.CREATIVE;
		}
		return gamemode;
	}

	@Override
	public void handleLoginPacket(PlayerLoginPacket packet) {
		super.handleLoginPacket(packet);
		if (!this.isSynapseLogin) {
			return;
		}

		if (isServerAuthoritativeInventoryEnabled() && cachedExtra != null) {
			if (cachedExtra.has("dynamicContainer")) {
				int nextContainerId = cachedExtra.get("dynamicContainer").getAsInt();
				if (nextContainerId != 0) {
					inventoryManager.getBundleRegistry().restoreContainerSequence(nextContainerId);
				}
			}
			if (cachedExtra.has("itemStack")) {
				int itemStackId = cachedExtra.get("itemStack").getAsInt();
				if (itemStackId != 0) {
					inventoryManager.setNextItemStackId(itemStackId + 50);
				}
			}
			if (cachedExtra.has("dynamicRecipe")) {
				int dynamicRecipeId = cachedExtra.get("dynamicRecipe").getAsInt();
				if (dynamicRecipeId != 0) {
					inventoryManager.setNextDynamicRecipeId(dynamicRecipeId + 50);
				}
			}
		}
	}

	@Override
	public void handleDataPacket(DataPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(packet);
			return;
		}
		packetswitch:
		switch (packet.pid()) {
			case ProtocolInfo.INTERACT_PACKET:
				InteractPacket113 interactPacket = (InteractPacket113) packet;
				if (interactPacket.action == InteractPacket113.ACTION_OPEN_INVENTORY
						&& (interactPacket.target == getLocalEntityId() || isRiding() && interactPacket.target == riding.getId() && riding.getNetworkId() != EntityID.CHEST_BOAT && (!riding.getDataFlag(DATA_FLAG_TAMED) || riding.getNetworkId() == EntityID.SKELETON_HORSE))
						&& (isServerAuthoritativeInventoryEnabled() || !this.inventoryOpen) && !isSpectator()) {
					if (!callPacketReceiveEvent(interactPacket.toDefault())) break;
					if (isServerAuthoritativeInventoryEnabled()) {
						if (inventoryManager != null) {
							inventoryManager.onClientOpenMainInventory();
						}
						break;
					}
//					this.openInventory();
					if (!this.deferPlayerInventoryOpen()) {
						this.inventoryOpen = this.inventory.open(this);
					}
					break;
				}
				super.handleDataPacket(packet);
				break;
			case ProtocolInfo.CONTAINER_CLOSE_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				this.abandonPendingCraftOnClientClose();
				ContainerClosePacket containerClosePacket = (ContainerClosePacket) packet;

				if (isServerAuthoritativeInventoryEnabled()) {
					if (inventoryManager != null) {
						inventoryManager.onClientRemoveWindow(containerClosePacket.windowId);
					} else {
						ContainerClosePacket pk = new ContainerClosePacket();
						pk.windowId = containerClosePacket.windowId;
						dataPacket(pk);
					}
					break;
				}

				if (this.handlePendingWindowClose(containerClosePacket.windowId)) {
					break;
				}
				if (this.acknowledgeStaleWindowClose(containerClosePacket.windowId, ContainerType.NONE)) {
					break;
				}
				if (containerClosePacket.windowId == ContainerIds.NONE) {
					this.craftingType = CRAFTING_SMALL;
					this.resetCraftingGridType();
					this.addWindow(this.craftingGrid, ContainerIds.NONE);

					ContainerClosePacket pk = new ContainerClosePacket();
					pk.windowId = ContainerIds.NONE;
					this.dataPacket(pk);
					break;
				}

				if (containerClosePacket.windowId == ContainerIds.INVENTORY) {
					this.inventoryOpen = false;
				}
				boolean closed = this.closeWindowFromClient(containerClosePacket.windowId);
				if (closed) {
					break;
				}

				this.getServer().getLogger().debug("{} unopened window: {}", getName(), containerClosePacket.windowId);
				this.sendContainerCloseResponse(containerClosePacket.windowId, ContainerType.NONE);
				break;
			case ProtocolInfo.INVENTORY_TRANSACTION_PACKET:
				InventoryTransactionPacket116 transactionPacket = (InventoryTransactionPacket116) packet;
					boolean javaClient = this.isJavaClient();
					boolean explicitItemUseHandAllowed = javaClient && this.supportsExplicitItemUseHand();
					JavaItemUseRouting.Route transactionRoute = JavaItemUseRouting.resolveTransaction(
							javaClient, explicitItemUseHandAllowed,
							transactionPacket.transactionType, transactionPacket.transactionData);
					if (transactionRoute == JavaItemUseRouting.Route.REJECT) {
						if (transactionPacket.transactionType == InventoryTransactionPacket.TYPE_RELEASE_ITEM
								&& JavaItemUseRouting.shouldCancelRejectedRelease(
								transactionPacket.transactionType, transactionRoute,
								this.isUsingItem(), this.getUsingItemHand())) {
							this.setUsingItem(false);
						}
						break;
					}
				if (!callPacketReceiveEvent(packet)) break;
				if (this.rejectSpectatorInventoryPart(transactionPacket)) {
					return;
				}

				if (isServerAuthoritativeInventoryEnabled()) {
					handleServerAuthoritativeInventoryTransaction(transactionPacket.transactionType,
							transactionPacket.legacyRequestId, transactionPacket.actions, transactionPacket.transactionData,
							transactionPacket.requestChangedSlots, transactionRoute);
					break;
				}

				Item item;
				Block block;

				boolean skipped = false;
				List<InventoryAction> actions = new ArrayList<>(transactionPacket.actions.length);
				for (NetworkInventoryAction networkInventoryAction : transactionPacket.actions) {
					InventoryAction a = networkInventoryAction.createInventoryAction(this);

					if (a == null) {
//						this.getServer().getLogger().debug("Unmatched inventory action from " + this.getName() + ": " + networkInventoryAction);
//						this.sendAllInventories();
//						break packetswitch;
						skipped = true;
						continue;
					}

					if (skipped && networkInventoryAction.windowId == ContainerIds.UI) {
						return;
					}

					actions.add(a);
				}

				if (this.rejectSpectatorCraftingContinuation(actions)) {
					return;
				}

				if (transactionPacket.isCraftingPart) {
					try {
						if (this.craftingTransaction == null) {
							this.craftingTransaction = new CraftingTransaction(this, actions);
						} else {
							for (InventoryAction action : actions) {
								this.craftingTransaction.addAction(action);
							}
						}

						if (this.craftingTransaction.getPrimaryOutput() != null && this.craftingTransaction.canExecute()) {
							//we get the actions for this in several packets, so we can't execute it until we get the result

							this.craftingTransaction.execute();
							this.craftingTransaction = null;
							break;
						}

						if ((craftingType >> 3) == 0 || craftingType == CRAFTING_STONECUTTER) {
							break;
						}
					} catch (Exception e) {
						this.getServer().getLogger().logException(e);
						this.craftingTransaction = null;
						this.getUIInventory().sendContents(this);
					}
				} else if (transactionPacket.isEnchantingPart) {
					if (this.enchantTransaction == null) {
						this.enchantTransaction = new EnchantTransaction(this, actions);
					} else {
						for (InventoryAction action : actions) {
							this.enchantTransaction.addAction(action);
						}
					}
					if (this.enchantTransaction.canExecute()) {
						this.enchantTransaction.execute();
						this.enchantTransaction = null;
					}
					return;
				} else if (transactionPacket.isRepairItemPart) {
					if (this.repairItemTransaction == null) {
						this.repairItemTransaction = new RepairItemTransaction(this, actions);
					} else {
						for (InventoryAction action : actions) {
							this.repairItemTransaction.addAction(action);
						}
					}

					if (this.repairItemTransaction.canExecute()) {
						this.repairItemTransaction.execute();
						this.repairItemTransaction = null;
						break;
					} else if (this.repairItemTransaction.isInvalid() || this.repairItemTransaction.isComplete()) {
						this.repairItemTransaction.execute();
						this.repairItemTransaction = null;
					}
					return;
				}

				if (this.craftingTransaction != null) {
					if (CraftingTransaction.checkForCraftingPart(actions)) {
						for (InventoryAction action : actions) {
							craftingTransaction.addAction(action);
						}
						return;
					}
//					else {
//						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete crafting transaction from " + this.getName() + ", refusing to execute crafting");
//						this.removeAllWindows(false);
//						this.sendAllInventories();
//						this.craftingTransaction = null;
//					}
				} else if (this.enchantTransaction != null) {
					if (enchantTransaction.checkForEnchantPart(actions)) {
						for (InventoryAction action : actions) {
							enchantTransaction.addAction(action);
						}
						return;
					} else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete enchanting transaction from " + this.getName() + ", refusing to execute enchant " + transactionPacket);
						this.removeAllWindows(false);
						this.sendAllInventories();
						this.enchantTransaction = null;
					}
				} else if (this.repairItemTransaction != null) {
					if (RepairItemTransaction.checkForRepairItemPart(actions)) {
						for (InventoryAction action : actions) {
							this.repairItemTransaction.addAction(action);
						}
						return;
					}/* else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete repair item transaction from " + this.getName() + ", refusing to execute repair item " + transactionPacket.toString());
						this.removeAllWindows(false);
						this.sendAllInventories();
						this.repairItemTransaction = null;
					}*/
					// smithing
					for (int i = 0; i < actions.size(); i++) {
						InventoryAction action = actions.get(i);
						if (!(action instanceof TakeResultAction)) {
							continue;
						}
						List<InventoryAction> sequence = new ObjectArrayList<>(actions.size());
						sequence.add(action);
						actions.remove(i);
						sequence.addAll(actions);
						actions = sequence;
						break;
					}
				}

				switch (transactionPacket.transactionType) {
					case InventoryTransactionPacket116.TYPE_NORMAL:
						InventoryTransaction transaction = new InventoryTransaction(this, actions);

						if (!transaction.execute()) {
							this.server.getLogger().debug("Failed to execute inventory transaction from " + this.getName() + " with actions: " + Arrays.toString(transactionPacket.actions));
							break packetswitch; //oops!
						}

						break packetswitch;
					case InventoryTransactionPacket116.TYPE_MISMATCH:
						if (transactionPacket.actions.length > 0) {
							this.server.getLogger().debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.length + ", " + Arrays.toString(transactionPacket.actions));
						}
						this.sendAllInventories();

						break packetswitch;
					case InventoryTransactionPacket116.TYPE_USE_ITEM: {
						UseItemData useItemData = (UseItemData) transactionPacket.transactionData;

						Vector3f clickPos = useItemData.clickPos;
						BlockVector3 blockVector = useItemData.blockPos;
						BlockFace face = useItemData.face;
						int type = useItemData.actionType;
						ItemUseHand interactionHand = transactionRoute.hand();
						ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

						try {
							if (interactionHand == ItemUseHand.OFF_HAND
									&& !JavaItemUseRouting.supportsUseItemAction(type)) {
								this.inventory.sendHeldItem(this);
								break packetswitch;
							}
							if (face == null && type != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR) {
								break packetswitch;
							}

							switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:
								// Remove if client bug is ever fixed
								boolean spamBug = lastRightClickData != null && System.currentTimeMillis() - lastRightClickTime < 100 &&
										lastRightClickData.face == face &&
										lastRightClickData.playerPos.distanceSquared(useItemData.playerPos) < Mth.EPSILON &&
										lastRightClickData.blockPos.equalsVec(blockVector) &&
										lastRightClickData.clickPos.distanceSquared(clickPos) < Mth.EPSILON && // signature spam bug has 0 distance, but allow some error
										Block.equals(lastRightClickData.block, useItemData.block) &&
										lastRightClickData.clientInteractPrediction == useItemData.clientInteractPrediction &&
										lastRightClickData.clientCooldownState == useItemData.clientCooldownState &&
//                                        lastRightClickData.itemInHand.equals(useItemData.itemInHand) &&
										lastRightClickData.hotbarSlot == useItemData.hotbarSlot;
								if (spamBug /*&& !(useItemData.itemInHand instanceof ItemBlock)*/) {
									return;
								}
								lastRightClickData = useItemData;
								lastRightClickTime = System.currentTimeMillis();

                                long itemSlotEpoch = this.getMovementEpoch();
                                boolean itemSlotReady = this.prepareItemUseSlot(useItemData.hotbarSlot, useItemData.itemInHand, interactionHand);
                                if (!itemSlotReady && (!this.isOnline() || !this.isAlive() || !this.isInputSessionActive()
                                        || this.getMovementEpoch() != itemSlotEpoch || !this.isCurrentInputPosition())) {
                                    break packetswitch;
                                }
                                Item i = inventory.getItemInHand();
								if (itemSlotReady && !i.is(Item.BRUSH)) {
                                    this.setDataFlag(DATA_FLAG_ACTION, false);
                                }

								// 从useItemData中设置玩家坐标，用于最精准的碰撞箱判断
								//this.newPosition = useItemData.playerPos.subtract(0, this.getBaseOffset(), 0);
								boolean clientPredictedFailure = false;
								boolean inventorySyncRequired = true;

								if (itemSlotReady && this.canInteractWithBlock(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
									if (this.isCreative()) {
										Vector3 blockPos;
										Block clientBlock = useItemData.block;
										if (clientBlock != null) {
											clientBlock.position(blockVector, level);
											blockPos = clientBlock;
										} else {
											blockPos = blockVector.asVector3();
										}
										Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
										clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
										BlockItemUseResult useResult = this.useItemOnBlockWithSync(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, clientPrediction, useItemData.itemInHand);
										inventorySyncRequired = useResult.inventorySyncRequired();
										if (useResult.item() != null) {
											break packetswitch;
										}
									} else if (i.equals(useItemData.itemInHand)) {
										Item oldItem = i.clone();
										Vector3 blockPos;
										Block clientBlock = useItemData.block;
										if (clientBlock != null) {
											clientBlock.position(blockVector, level);
											blockPos = clientBlock;
										} else {
											blockPos = blockVector.asVector3();
										}
										Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
										clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
										clientPredictedFailure = clientPrediction != null && !clientPrediction;
										Player predictedConsumer = this.predictedBlockConsumer(oldItem, useItemData.itemInHand, clientPrediction);
										InventorySlotReference predictedSource = predictedConsumer != null && !this.isMainThreadInputEnabled()
												? inventory.captureHeldItem() : null;
										BlockItemUseResult useResult = this.useItemOnBlockWithSync(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, clientPrediction, useItemData.itemInHand);
										inventorySyncRequired = useResult.inventorySyncRequired();
										if ((i = useResult.item()) != null) {
											if (!this.isMainThreadInputEnabled() && (!i.equals(oldItem) || i.getCount() != oldItem.getCount())) {
												if (predictedSource != null && (!predictedSource.isCurrent() || !predictedSource.isSelectedBy(inventory))) {
													predictedConsumer = null;
												}
												inventory.setItemInHand(i, predictedConsumer);
												inventory.sendHeldItem(this.getViewers().values());
											}
											break packetswitch;
										}
									}
								}

								// 双方都未消耗且原槽未变化时，不回发相同库存干扰后续预测。
								if (inventorySyncRequired) inventory.sendHeldItem(this);

								if (clientPredictedFailure) {
									break packetswitch;
								}

								if (blockVector.distanceSquared(this) > 10000) {
									break packetswitch;
								}

								Block target = level.getBlock(blockVector.asVector3());
								Block block0 = target.getSide(face);

								this.level.sendBlocks(new Player[]{this}, new Block[]{target, block0}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
								this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target), level.getExtraBlock(block0)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

								if (target instanceof BlockDoor) {
									BlockDoor door = (BlockDoor) target;
									Block part;

									if (door.isTop()) {
										part = target.down();

										if (part.getId() == target.getId()) {
											target = part;

											this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
										}
									}
								}
								break packetswitch;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_BREAK_BLOCK:
								if (!this.spawned || !this.isAlive()) {
									break packetswitch;
								}

								if (isServerAuthoritativeInventoryEnabled()) {
									removeCurrentWindow();
								}
								this.resetCraftingGridType();

								i = this.getInventory().getItemInHand();

								Item oldItem = i.clone();

								if (isBreakingBlock(blockVector)
										&& this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)
										&& (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
									this.breakingBlock = null;
									this.breakingBlockFace = null;
									if (this.isSurvival()) {
										this.getFoodData().updateFoodExpLevel(0.005f);
										if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
											inventory.setItemInHand(i);
											inventory.sendHeldItem(this.getViewers().values());
										}
									}
									break packetswitch;
								}

								inventory.sendContents(this);
								inventory.sendHeldItem(this);

								if (blockVector.distanceSquared(this) < 10000) {
									target = this.level.getBlock(blockVector.asVector3());
									this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
									this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

									BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(blockVector);
									if (blockEntity instanceof BlockEntitySpawnable) {
										((BlockEntitySpawnable) blockEntity).spawnTo(this);
									}
								}

								break packetswitch;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR:
							    if (!this.prepareItemUseSlot(useItemData.hotbarSlot, useItemData.itemInHand, interactionHand)) {
							        break packetswitch;
							    }
								Vector3 directionVector = this.getDirectionVector();

								if (this.isCreative()) {
									item = this.inventory.getItemInHand();
								} else if (!this.inventory.getItemInHand().equals(useItemData.itemInHand)) {
//									server.getLogger().debug(getName() + " held desync\nC: " + useItemData.itemInHand + "\nS: " + inventory.getItemInHand());
//									this.inventory.sendHeldItem(this); // 这里不再强制同步, 尝试解决使用物品后快速切换物品的回弹问题
									break packetswitch;
								} else {
									item = this.inventory.getItemInHand();
								}

								this.rebindStartedJavaItemUseHand(explicitItemUseHandAllowed, interactionHand);
								InventorySlotReference itemSource = this.isMainThreadInputEnabled() ? this.inventory.captureHeldItem() : null;
								long itemSourceEpoch = itemSource == null ? 0 : this.getMovementEpoch();
								PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
                                if (isSpectator()) {
                                    interactEvent.setCancelled();
                                }
								this.server.getPluginManager().callEvent(interactEvent);
								if (interactEvent.isCancelled()
								        || itemSource != null && !this.canStartItemUse(itemSource, itemSourceEpoch)) {
								    if (itemSource == null) {
								        this.inventory.sendHeldItem(this);
								    } else {
								        this.resyncItemUse(itemSource);
								    }
								    break packetswitch;
								}

								boolean explicitShieldUse = JavaItemUseRouting.supportsExplicitShieldUse(
										explicitItemUseHandAllowed, item.getId());
								boolean itemAccepted = item.onClickAir(this, directionVector) || explicitShieldUse;
								if (itemAccepted) {
									if (this.isSurvivalLike()) {
										if (itemSource == null) {
										    this.inventory.setItemInHand(item);
										} else if (!itemSource.setItem(item)) {
										    this.resyncItemUse(itemSource);
										    break packetswitch;
										}
									}

									if (itemSource != null && (!this.isOnline() || !this.isInputSessionActive()
									        || this.getMovementEpoch() != itemSourceEpoch || !itemSource.isSelectedBy(this.inventory))) {
									    break packetswitch;
									}

									if (!this.isUsingItem()) {
										this.setUsingItem(item.canRelease() || explicitShieldUse);
										break packetswitch;
									}

									// Used item
									//int ticksUsed = this.server.getTick() - this.startAction;
									int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

									this.completeItemUse(item, ticksUsed, explicitShieldUse);
								}

								break packetswitch;
							default:
								//unknown
								break;
							}
						} finally {
							this.setItemInteractionHand(previousHand);
						}
						break;
					}
					case InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY: {
						UseItemOnEntityData useItemOnEntityData = (UseItemOnEntityData) transactionPacket.transactionData;
						int type = useItemOnEntityData.actionType;
						ItemUseHand interactionHand = transactionRoute.hand();
						ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

						try {
							if (interactionHand == ItemUseHand.OFF_HAND
									&& !JavaItemUseRouting.supportsEntityAction(type)) {
								this.inventory.sendHeldItem(this);
								break packetswitch;
							}
							Entity target = this.level.getEntity(useItemOnEntityData.entityRuntimeId);
						if (target == null) {
							item = this.inventory.getItemInHand();
							PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, this.getDirectionVector(), BlockFace.UP, PlayerInteractEvent.Action.CLICK_UNKNOWN_ENTITY).setUnkownEntityId(useItemOnEntityData.entityRuntimeId);
                            if (isSpectator()) {
                                interactEvent.setCancelled();
                            }
							this.server.getPluginManager().callEvent(interactEvent);
							return;
						}

						if (!useItemOnEntityData.itemInHand.equalsExact(this.inventory.getItemInHand())) {
							this.inventory.sendHeldItem(this);
						}

						item = this.inventory.getItemInHand();

						switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_INTERACT:
								if (!this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : this.level.getMaxEntityInteractionReachDistanceInSurvival())) {
									break;
								}

								InventorySlotReference interactionSource = this.isMainThreadInputEnabled() ? this.inventory.captureHeldItem() : null;
								long interactionEpoch = interactionSource == null ? 0 : this.getMovementEpoch();
								long interactionTargetEpoch = interactionSource != null && target instanceof Player targetPlayer ? targetPlayer.getMovementEpoch() : 0;
								PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(this, target, item, useItemOnEntityData.clickPos);
								if (this.isSpectator()) playerInteractEntityEvent.setCancelled();
								getServer().getPluginManager().callEvent(playerInteractEntityEvent);

								if (playerInteractEntityEvent.isCancelled()
								        || interactionSource != null && !this.canStartEntityInteraction(interactionSource, interactionEpoch, target, interactionTargetEpoch)) {
								    if (interactionSource != null) {
								        this.resyncItemUse(interactionSource);
								    }
									break;
								}
								if (target.onInteract(this, item, useItemOnEntityData.clickPos) && this.isSurvival()) {
								    if (interactionSource != null) {
								        this.finishEntityInteractionItem(interactionSource, item, target);
								        break;
								    }
									if (item.isTool()) {
										if (item.useOn(target) && item.getDamage() > item.getMaxDurability()) {
											item = Items.air();
											level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BREAK);
										}
									} else {
										if (item.count > 1) {
											item.count--;
										} else {
											item = Items.air();
										}
									}

									this.inventory.setItemInHand(item);
								}
								break;
							case InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK:
//								if (++currentTickAttackPacketCount >= 10) {
//									violation += 25; // 近期TPS不稳定容易误判, 先禁用
//									return;
//								}
								if (!this.claimAttackAttempt()) {
									return;
								}

								// 命中边缘时有极小概率没有看着实体 (InteractPacket::InteractUpdate-4)
/*
								if (this.lookAtEntity != target) {
									this.violation += 6;
									break;
								}
*/
								if (!this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : this.level.getMaxEntityInteractionReachDistanceInSurvival())) {
									break;
								}

								InventorySlotReference attackSource = this.isMainThreadInputEnabled() ? this.inventory.captureHeldItem() : null;
								long attackEpoch = attackSource == null ? 0 : this.getMovementEpoch();
								long targetEpoch = attackSource != null && target instanceof Player targetPlayer ? targetPlayer.getMovementEpoch() : 0;
								ItemAttackDamageEvent event = new ItemAttackDamageEvent(item);
								this.server.getPluginManager().callEvent(event);
								float itemDamage = event.getAttackDamage();

								Enchantment[] enchantments = item.getId() != Item.ENCHANTED_BOOK ? item.getEnchantments() : Enchantment.EMPTY;

								float damageBonus = 0;
								for (Enchantment enchantment : enchantments) {
									damageBonus += enchantment.getDamageBonus(this, target);
								}
								itemDamage += Mth.floor(damageBonus);

								Map<EntityDamageEvent.DamageModifier, Float> damage = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
								damage.put(EntityDamageEvent.DamageModifier.BASE, itemDamage);

								KnockbackProfile targetProfile = this.getKnockbackProfile();
								float knockBackH = targetProfile.getBaseH();
								float knockBackV = targetProfile.getBaseV();
                                int knockBackEnchantment = !item.is(Item.ENCHANTED_BOOK) ? item.getEnchantmentLevel(Enchantment.KNOCKBACK) : 0;

								if (target instanceof Player) {
									if ((((Player) target).getGamemode() & 0x01) > 0) {
										break;
									} else if (!this.server.getConfiguration().isPvp() || this.level.getDifficulty() == 0 || !level.getGameRules().getBoolean(GameRule.PVP)) {
										break;
									}
								}

								// 伤害计算扩展可能切槽、替换物品或改变两端的位置代际。
								if (attackSource != null && (!this.canStartItemUse(attackSource, attackEpoch)
								        || target.isClosed() || !target.isAlive() || target.getLevel() != this.level
								        || this.level.getEntity(target.getId()) != target
								        || target instanceof Player targetPlayer && targetPlayer.getMovementEpoch() != targetEpoch
								        || !this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : this.level.getMaxEntityInteractionReachDistanceInSurvival()))) {
								    this.resyncItemUse(attackSource);
								    break;
								}

								EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(this, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, knockBackH, knockBackV, enchantments);
								entityDamageByEntityEvent.getKnockbackProfile().setEnchantLevel(knockBackEnchantment);
								if (this.isSpectator()) entityDamageByEntityEvent.setCancelled();
								if (!target.attack(entityDamageByEntityEvent)) {
									if (item.isTool() && this.isSurvival()) {
										this.inventory.sendContents(this);
									}
									break;
								}

								for (Enchantment enchantment : enchantments) {
									enchantment.doPostAttack(item, this, target, null);
								}

								if (item.isTool() && this.isSurvival()) {
								    if (attackSource != null) {
								        this.finishAttackItem(attackSource, item, target);
								        return;
								    }
									if (item.useOn(target)) {
										if (item.getDamage() > item.getMaxDurability()) {
											this.inventory.setItemInHand(Items.air());
											level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BREAK);
										} else {
											this.inventory.setItemInHand(item);
										}
									} else {
										this.inventory.setItemInHand(item);
									}
								}
								return;
							default:
								break; //unknown
						}

						} finally {
							this.setItemInteractionHand(previousHand);
						}

						break;
					}
					case InventoryTransactionPacket116.TYPE_RELEASE_ITEM: {
						// 新模式已在事务入口完成全部包监听，不能重复派发释放事件。
						if (!this.isMainThreadInputEnabled() && !callPacketReceiveEvent(packet)) break;
						if (this.isSpectator()) {
							this.sendAllInventories();
							break packetswitch;
						}
						ReleaseItemData releaseItemData = (ReleaseItemData) transactionPacket.transactionData;
						ItemUseHand interactionHand = transactionRoute.hand();
						if (explicitItemUseHandAllowed
								&& this.isUsingItem()
								&& interactionHand != this.getUsingItemHand()) {
							ItemUseHand previousHand = this.setItemInteractionHand(this.getUsingItemHand());
							try {
								this.inventory.sendHeldItem(this);
							} finally {
								this.setItemInteractionHand(previousHand);
								this.setUsingItem(false);
							}
							break packetswitch;
						}
						ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

						try {
							try {
								int type = releaseItemData.actionType;
								switch (type) {
									case InventoryTransactionPacket116.RELEASE_ITEM_ACTION_RELEASE:
										if (this.isUsingItem()) {
											item = this.inventory.getItemInHand();
											if (this.isUsingOffhandItem() && !this.isUsingSameItem(item)) {
												this.inventory.sendHeldItem(this);
												return;
											}

											//int ticksUsed = this.server.getTick() - this.startAction;
											int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

											if (!item.onRelease(this, ticksUsed, releaseItemData.headRot)) {
												this.inventory.sendContents(this);
											}
										} else {
											this.inventory.sendContents(this);
										}
										return;
									case InventoryTransactionPacket116.RELEASE_ITEM_ACTION_CONSUME:
										this.getServer().getLogger().debug("Unexpected release item action consume from " + this.getName());
										return;
									default:
										break;
								}
							} finally {
								this.setUsingItem(false);
							}
						} finally {
							this.setItemInteractionHand(previousHand);
						}
						break;
					}
					default:
						this.inventory.sendContents(this);
						break;
				}
				break;
			case ProtocolInfo.ACTOR_EVENT_PACKET:
				EntityEventPacket entityEventPacket = (EntityEventPacket) packet;
				if (entityEventPacket.event == EntityEventPacket.ENCHANT && this.getWindowById(ENCHANT_WINDOW_ID) != null) {
					break; //附魔现在在 EnchantTransaction 中扣减经验等级
				}
				super.handleDataPacket(packet);
				break;
			case ProtocolInfo.PACKET_VIOLATION_WARNING_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				PacketViolationWarningPacket116 packetViolationWarningPacket = (PacketViolationWarningPacket116) packet;
				this.getServer().getLogger().warning("Received PacketViolationWarningPacket from " + this.getName());
				this.getServer().getLogger().warning("type=" + packetViolationWarningPacket.type.name());
				this.getServer().getLogger().warning("severity=" + packetViolationWarningPacket.severity.name());
				this.getServer().getLogger().warning("packetId=0x" + Integer.toHexString(packetViolationWarningPacket.packetId) + " (" + packetViolationWarningPacket.packetId + ")");
				this.getServer().getLogger().warning("context=" + packetViolationWarningPacket.context);
				break;
			case ProtocolInfo.SET_PLAYER_GAME_TYPE_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				SetPlayerGameTypePacket setPlayerGameTypePacket = (SetPlayerGameTypePacket) packet;
				int gamemode = vanillaGamemodeToNukkitGamemode(setPlayerGameTypePacket.gamemode);
				if (gamemode == this.gamemode) {
					break;
				}

				if (!this.hasPermission("nukkit.command.gamemode") || gamemode == -1) {
					UpdatePlayerGameTypePacket116 updatePlayerGameTypePacket = new UpdatePlayerGameTypePacket116();
					updatePlayerGameTypePacket.gamemode = getClientFriendlyGamemode(this.gamemode);
					updatePlayerGameTypePacket.playerEntityUniqueId = getId();
					this.dataPacket(updatePlayerGameTypePacket);

					this.sendAbilities(this, this.getAdventureSettings());
					break;
				}

				this.setGamemode(gamemode, true);
				Command.broadcastCommandMessage(this, new TranslationContainer("commands.gamemode.success.self", Server.getGamemodeString(this.gamemode)));

				UpdatePlayerGameTypePacket116 updatePlayerGameTypePacket = new UpdatePlayerGameTypePacket116();
				updatePlayerGameTypePacket.gamemode = getClientFriendlyGamemode(this.gamemode);
				updatePlayerGameTypePacket.playerEntityUniqueId = getId();
				this.dataPacket(updatePlayerGameTypePacket);
				break;
			case ProtocolInfo.EMOTE_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (!this.spawned) {
					break;
				}
				EmotePacket116 emotePacket = (EmotePacket116) packet;
				if (emotePacket.runtimeId != this.getLocalEntityId()) {
					server.getLogger().warning(this.username + " sent EmotePacket with invalid entity id: " + emotePacket.runtimeId + " != " + this.getLocalEntityId());
					break;
				}
				if (emotePacket.emoteID.length() != 32 + 4) { // 00000000-0000-0000-0000-000000000000
					onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "emote");
					break;
				}
				if ((emotePacket.flags & EmotePacket116.FLAG_SERVER) != 0) {
					break;
				}
				if (!emoteRequest()) {
					this.addViolationLevel(8, "emote_req");
					break;
				}
				if (isNetEaseClient()) {
					onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "ce_emote", emotePacket.emoteID);
					break;
				}
				if (isSpectator()) {
					break;
				}
				if (emoting) {
					break;
				}

				int flags = emotePacket.flags | EmotePacket116.FLAG_SERVER;
				if (MUTE_EMOTE_CHAT) {
					flags |= EmotePacket116.FLAG_MUTE_ANNOUNCEMENT;
				}
				for (Player viewer : this.getViewers().values()) {
					viewer.playEmote(emotePacket.emoteID, this.getId(), flags);
				}
				break;
			case ProtocolInfo.PLAYER_AUTH_INPUT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (!this.canContinueInputState()) return;

				if (!isServerAuthoritativeMovementEnabled()) {
					onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "auth_input");
					return;
				}

				IPlayerAuthInputPacket playerAuthInputPacket = (IPlayerAuthInputPacket) packet;
				if (!validateCoordinate(playerAuthInputPacket.getX()) || !validateCoordinate(playerAuthInputPacket.getY()) || !validateCoordinate(playerAuthInputPacket.getZ())
						|| !validateFloat(playerAuthInputPacket.getPitch()) || !validateFloat(playerAuthInputPacket.getYaw()) || !validateFloat(playerAuthInputPacket.getHeadYaw())
						|| !validateFloat(playerAuthInputPacket.getDeltaX()) || !validateFloat(playerAuthInputPacket.getDeltaY()) || !validateFloat(playerAuthInputPacket.getDeltaZ())
						|| !validateFloat(playerAuthInputPacket.getVrGazeDirectionX()) || !validateFloat(playerAuthInputPacket.getVrGazeDirectionY()) || !validateFloat(playerAuthInputPacket.getVrGazeDirectionZ())
						|| !validateFloat(playerAuthInputPacket.getAnalogMoveVecX()) || !validateFloat(playerAuthInputPacket.getAnalogMoveVecZ())
						|| !validateFloat(playerAuthInputPacket.getCameraOrientationX()) || !validateFloat(playerAuthInputPacket.getCameraOrientationY()) || !validateFloat(playerAuthInputPacket.getCameraOrientationZ())
						|| !validateFloat(playerAuthInputPacket.getInteractRotationX()) || !validateFloat(playerAuthInputPacket.getInteractRotationY())
						|| !validateFloat(playerAuthInputPacket.getRawMoveVecX()) || !validateFloat(playerAuthInputPacket.getRawMoveVecZ())) {
					this.getServer().getLogger().warning("Invalid movement received: " + this.getName());
					this.close("", "Invalid movement");
					return;
				}
				float moveVecX = playerAuthInputPacket.getMoveVecX();
				float moveVecY = playerAuthInputPacket.getMoveVecZ();
				if (!validateVehicleInput(moveVecX) || !validateVehicleInput(moveVecY)) {
					this.getServer().getLogger().warning("Invalid vehicle input received: " + this.getName());
					this.close("", "Invalid vehicle input");
					return;
				}

				this.onClientTickUpdated(playerAuthInputPacket.getTick());
				if (!this.canContinueInputState()) return;

				if (this.getLoginChainData().getCurrentInputMode() != playerAuthInputPacket.getInputMode()) {
					int from = this.getLoginChainData().getCurrentInputMode();
					this.getLoginChainData().setCurrentInputMode(playerAuthInputPacket.getInputMode());
					this.getServer().getPluginManager().callEvent(new SynapsePlayerInputModeChangeEvent(this, from, playerAuthInputPacket.getInputMode()));
					if (!this.canContinueInputState()) return;
				}

				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_SPRINTING) && !this.isSprinting()) {
					PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, true);
					if (hasEffect(Effect.BLINDNESS)) {
						playerToggleSprintEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleSprintEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleSprintEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSprinting(true);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_SPRINTING) && this.isSprinting()) {
					PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleSprintEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleSprintEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSprinting(false);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_SNEAKING) && !this.isSneaking()) {
					PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, true);
					this.server.getPluginManager().callEvent(playerToggleSneakEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleSneakEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSneaking(true);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_SNEAKING) && this.isSneaking()) {
					PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleSneakEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleSneakEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSneaking(false);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_SWIMMING) && !this.isSwimming()) {
					PlayerToggleSwimEvent playerToggleSwimEvent = new PlayerToggleSwimEvent(this, true);
					this.server.getPluginManager().callEvent(playerToggleSwimEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleSwimEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSwimming(true);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_SWIMMING) && this.isSwimming()) {
					PlayerToggleSwimEvent playerToggleSwimEvent = new PlayerToggleSwimEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleSwimEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleSwimEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSwimming(false);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_JUMPING)) {
					this.server.getPluginManager().callEvent(new PlayerJumpEvent(this));
					if (!this.canContinueInputState()) return;

					if (EXPERIMENTAL_COMBAT_KNOCKBACK_TEST && this.isSprinting()) {
						this.motionX = playerAuthInputPacket.getDeltaX();
						this.motionY = playerAuthInputPacket.getDeltaY();
						this.motionZ = playerAuthInputPacket.getDeltaZ();
					}
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_GLIDING) && !this.isGliding()) {
					PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
					Item chestplate = getArmorInventory().getChestplate();
					if (chestplate.getId() != Item.ELYTRA || chestplate.getDamage() >= chestplate.getMaxDurability() - 1) {
						playerToggleGlideEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleGlideEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleGlideEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setGliding(true);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_GLIDING) && this.isGliding()) {
					PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleGlideEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleGlideEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setGliding(false);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_CRAWLING) && !this.isCrawling()) {
					PlayerToggleCrawlEvent playerToggleCrawlEvent = new PlayerToggleCrawlEvent(this, true);
					this.server.getPluginManager().callEvent(playerToggleCrawlEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleCrawlEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setCrawling(true);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_CRAWLING) && this.isCrawling()) {
					PlayerToggleCrawlEvent playerToggleCrawlEvent = new PlayerToggleCrawlEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleCrawlEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleCrawlEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setCrawling(false);
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_FLYING) && !getAdventureSettings().get(Type.FLYING)) {
					if (!this.canApplyFlightState(true)) {
						this.sendAbilities(this, this.getAdventureSettings());
						return;
					}
					if (!server.getAllowFlight() && !getAdventureSettings().get(Type.ALLOW_FLIGHT)) {
						kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server", false);
						break;
					}

					PlayerToggleFlightEvent playerToggleFlightEvent = new PlayerToggleFlightEvent(this, true);
					if (isSpectator()) {
						playerToggleFlightEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleFlightEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleFlightEvent.isCancelled() || !this.canApplyFlightState(playerToggleFlightEvent.isFlying())) {
						this.sendAbilities(this, this.getAdventureSettings());
					} else {
						this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_FLYING) && getAdventureSettings().get(Type.FLYING)) {
					PlayerToggleFlightEvent playerToggleFlightEvent = new PlayerToggleFlightEvent(this, false);
					if (isSpectator()) {
						playerToggleFlightEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleFlightEvent);
					if (!this.canContinueInputState()) return;
					if (playerToggleFlightEvent.isCancelled() || !this.canApplyFlightState(playerToggleFlightEvent.isFlying())) {
						this.sendAbilities(this, this.getAdventureSettings());
					} else {
						this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.START_SPIN_ATTACK)) {
					if (isSpectator()) {
						setDataFlag(DATA_FLAG_SPIN_ATTACK, false);
					} else {
						Item trident = getInventory().getItemInHand();
						if (trident.is(Item.TRIDENT)) {
							int riptide = trident.getEnchantmentLevel(Enchantment.RIPTIDE);
							if (riptide > 0) {
								//TODO: check water/rain
								if (setDataFlag(DATA_FLAG_SPIN_ATTACK, true)) {
									if (!this.canContinueInputState()) return;
									damageNearbyMobsTick = 20;
									level.addLevelSoundEvent(this.add(0, getHeight() * 0.5f, 0), switch (riptide) {
										case 1 -> LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_1;
										case 2 -> LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_2;
										default -> LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_3;
									});
								}
							}
						}
					}
					if (!this.canContinueInputState()) return;
				}
				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.STOP_SPIN_ATTACK)) {
					damageNearbyMobsTick = 0;
					setDataFlag(DATA_FLAG_SPIN_ATTACK, false);
					if (!this.canContinueInputState()) return;
				}

				emoting = playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.EMOTING);

				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.HANDLED_TELEPORT)) {
					//TODO
				}

				if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.MISSED_SWING) && isServerAuthoritativeSoundEnabled() && !isSpectator()) {
                    long animationEpoch = this.getMovementEpoch();
					level.addLevelSoundEvent(this, inventory.getItemInHand().getAttackMissSound(), EntityFullNames.PLAYER);
                    // 声音发包回调可退休或传送来源，停止尚未开始的触摸动画。
                    if (this.isMainThreadInputEnabled()
                            && (!this.canContinueInputState() || this.getMovementEpoch() != animationEpoch)) return;

					// touch bug: https://bugs.mojang.com/browse/MCPE-107865
					if (getLoginChainData().getCurrentInputMode() == ClientChainData.INPUT_TOUCH) {
						AnimatePacket pk = new AnimatePacket();
						pk.eid = getId();
						pk.action = Action.SWING_ARM;
                        pk.swingSource = SwingSource.ATTACK;
						dataPacket(pk);
                        // 自发动画回调之后仍需绑定原来源，不能继续向观察者发布旧动作。
                        if (this.isMainThreadInputEnabled()
                                && (!this.canContinueInputState() || this.getMovementEpoch() != animationEpoch)) return;
						Server.broadcastPacket(getViewers().values(), pk);
					}
				}

				if (riding instanceof EntityRideable rideable) {
					rideable.updatePlayerJump(this, playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.JUMPING));
					if (!this.canContinueInputState()) return;
				}

				if (!this.canContinueInputState()) {
					return;
				}
				boolean predictedInVehicle = playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.IN_CLIENT_PREDICTED_IN_VEHICLE);
				boolean inPredictedVehicle = predictedInVehicle && riding != null && riding.getId() == playerAuthInputPacket.getPredictedVehicleEntityUniqueId();

				Vector3 newPos = new Vector3(playerAuthInputPacket.getX(), playerAuthInputPacket.getY() - this.getBaseOffset(), playerAuthInputPacket.getZ());
				if (inPredictedVehicle) {
					Vector3f offset = riding.getMountedOffset(this);
					newPos.x += offset.x;
					newPos.y += offset.y;
					newPos.z += offset.z;
				}
				double dis = newPos.distanceSquared(this);

				if (this.teleportPosition == null && (dis != 0 || playerAuthInputPacket.getYaw() % 360 != this.yaw || playerAuthInputPacket.getPitch() % 360 != this.pitch)) {
//				    if (dis > 100) {
//				    	this.sendPosition(this, playerAuthInputPacket.getYaw(), playerAuthInputPacket.getPitch(), MovePlayerPacket.MODE_RESET);
//				    	break;
//				    }

					boolean revert = false;
					if (!this.isAlive() || !this.spawned) {
						revert = true;
						this.forceMovement = new Vector3(this.x, this.y, this.z);
					}

					if (riding != null) {
						forceMovement = null;
					}

					if (this.forceMovement != null && (newPos.distanceSquared(this.forceMovement) > 0.1 || revert)) {
						this.sendPosition(this.forceMovement, this.yaw, this.pitch, MovePlayerPacket.MODE_TELEPORT);
					} else {
						float yaw = (predictedInVehicle ? playerAuthInputPacket.getHeadYaw() : playerAuthInputPacket.getYaw()) % 360;
						float pitch = playerAuthInputPacket.getPitch() % 360;

						if (yaw < 0) {
							yaw += 360;
						}

						this.setPendingMovement(newPos, yaw, pitch);
						this.forceMovement = null;
					}
				}

				if (this.isMainThreadInputEnabled() && this.riding == null
						&& !this.commitReceivedMovement()) {
					return;
				}
				PlayerBlockAction[] blockActions = playerAuthInputPacket.getBlockActions();
				if (blockActions != null && blockActions.length != 0 && !this.prepareBlockActionsWhileUsingItem(blockActions)) {
                    // 只跳过竞争的挖掘动作，同包移动、物品交互及库存请求仍继续处理。
                    blockActions = null;
                }
				if (blockActions != null && blockActions.length != 0) {
                    if (isNetEaseClient() && blockActions.length > 1) {
                        // damn netease r21u0n1
                        int startDestroyIndex = -1;
                        int predictDestroyIndex = -1;
                        for (int i = 0; i < blockActions.length; i++) {
                            switch (blockActions[i].action) {
                                case PlayerActionPacket.ACTION_START_BREAK -> startDestroyIndex = i;
                                case PlayerActionPacket.ACTION_BLOCK_PREDICT_DESTROY -> predictDestroyIndex = i;
                            }
                        }
                        if (startDestroyIndex != -1 && predictDestroyIndex != -1 && predictDestroyIndex < startDestroyIndex) {
                            PlayerBlockAction startAction = blockActions[startDestroyIndex];
                            blockActions[startDestroyIndex] = blockActions[predictDestroyIndex];
                            blockActions[predictDestroyIndex] = startAction;
                        }
                    }
					for (PlayerBlockAction blockAction : blockActions) {
						if (!this.canContinueInputState()) {
							return;
						}
						if (!this.canProcessInventoryBlockAction(blockAction.action, true)) {
							continue;
						}
						Vector3 pos = new Vector3(blockAction.x, blockAction.y, blockAction.z);
						BlockFace face = null;
						boolean distanceChecked = false;

						actionswitch:
						switch (blockAction.action) {
							case PlayerActionPacket14.ACTION_BLOCK_CONTINUE_DESTROY: // server
								if (this.isBreakingBlock() && pos.distanceSquared(this) < 100) {
									if (this.isBreakingBlock(pos.asBlockVector3())) {
										this.breakingBlockFace = BlockFace.fromIndex(blockAction.data);
										break;
									}
									distanceChecked = true;
									this.level.addLevelEvent(this.breakingBlock, LevelEventPacket.EVENT_BLOCK_STOP_BREAK);
									this.breakingBlock = null;
									this.breakingBlockFace = null;
								}
							case PlayerActionPacket14.ACTION_START_BREAK: // both
								if (!this.spawned || !this.isAlive() || this.isSpectator() || this.isBreakingBlock() || !distanceChecked && pos.distanceSquared(this) > 100) {
									break;
								}
								face = BlockFace.fromIndex(blockAction.data);
								Block target = this.level.getBlock(pos, false);
								PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == Block.AIR ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
								this.getServer().getPluginManager().callEvent(playerInteractEvent);
								if (!this.canContinueInputState()) return;
								if (playerInteractEvent.isCancelled()) {
									this.inventory.sendHeldItem(this);
									break;
								}
								if (!isAdventure()) {
									switch (target.getId()) {
										case Block.NOTEBLOCK:
											if (!isCreative()) {
												((BlockNoteblock) target).emitSound();
											}
											break;
										case Block.DRAGON_EGG:
											if (!this.isCreative()) {
												((BlockDragonEgg) target).teleport();
												break actionswitch;
											}
											break;
										case Block.FRAME:
										case Block.GLOW_FRAME:
											BlockEntity itemFrame = this.level.getBlockEntityIfLoaded(pos);
											if (itemFrame instanceof BlockEntityItemFrame && (((BlockEntityItemFrame) itemFrame).dropItem(this) || isCreative() && getProtocol() < AbstractProtocol.PROTOCOL_120_70.getProtocolStart())) {
												break actionswitch;
											}
											break;
										case Block.LECTERN:
											if (level.getBlockEntityIfLoaded(pos) instanceof BlockEntityLectern lectern && lectern.dropBook(this)) {
												lectern.spawnToAll();
												if (isCreative()) {
													break actionswitch;
												}
											}
											break;
									}
								}
								block = target.getSide(face);
								if (block.isFire()) {
									this.level.setBlock(block, Block.get(Block.AIR), true);
									this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
									break;
								}

								Item held = inventory.getItemInHand();
								if (!this.isCreative()) {
									//improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
									//Done by lmlstarqaq
									double breakTime = Mth.ceil(target.getBreakTime(held, this) * 20);
									if (breakTime > 0) {
										this.level.addLevelEvent(pos, LevelEventPacket.EVENT_BLOCK_START_BREAK, (int) (65535 / breakTime));
									}
								} else if (held.isSword() || held.is(Item.TRIDENT) || held.is(Item.MACE)) {
									break;
								}

								this.breakingBlock = target;
								this.breakingBlockFace = face;
								this.lastBreak = System.currentTimeMillis();
								break;
							case PlayerActionPacket14.ACTION_STOP_BREAK: // client
								if (!this.isBreakingBlock() || this.breakingBlockFace == null) {
									break;
								}
								pos = this.breakingBlock;
								face = this.breakingBlockFace;
							case PlayerActionPacket14.ACTION_BLOCK_PREDICT_DESTROY: // server
								if (!this.spawned || !this.isAlive() || pos.distanceSquared(this) > 100) {
									break;
								}
								distanceChecked = true;
								if (face == null) {
									face = BlockFace.fromIndex(blockAction.data);
								}
								if (isServerAuthoritativeInventoryEnabled()) {
									removeCurrentWindow();
								}
								this.resetCraftingGridType();

								item = this.getInventory().getItemInHand();
								Item oldItem = item.clone();
								if (isBreakingBlock(pos.asBlockVector3())
										&& this.canInteract(pos.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)
										&& (item = this.level.useBreakOn(pos, face, item, this, true)) != null) {
									// success
									if (this.isSurvival()) {
										this.getFoodData().updateFoodExpLevel(0.005f);
										if (!item.equals(oldItem) || item.getCount() != oldItem.getCount()) {
											this.inventory.setItemInHand(item);
											this.inventory.sendHeldItem(this.getViewers().values());
										}
									}
								} else { // revert
									this.inventory.sendContents(this);
									this.inventory.sendHeldItem(this);

									target = this.level.getBlock(pos);
									this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
									target = this.level.getExtraBlock(pos);
									this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

									BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(pos);
									if (blockEntity instanceof BlockEntitySpawnable) {
										((BlockEntitySpawnable) blockEntity).spawnTo(this);
									}
								}
							case PlayerActionPacket14.ACTION_ABORT_BREAK: // both
								if (!distanceChecked && pos.distanceSquared(this) > 100) {
									break;
								}
								if (face == null) {
//									int breakTime = blockAction.data;
								}
								this.level.addLevelEvent(pos, LevelEventPacket.EVENT_BLOCK_STOP_BREAK);
								this.breakingBlock = null;
								this.breakingBlockFace = null;
								break;
							case PlayerActionPacket14.ACTION_CONTINUE_BREAK: // client
								if (this.isBreakingBlock(pos.asBlockVector3()) && pos.distanceSquared(this) < 100) {
									this.breakingBlockFace = BlockFace.fromIndex(blockAction.data);
									block = this.level.getBlock(pos, false);
									Vector3 blockCenter = pos.blockCenter();
									this.level.addParticle(new PunchBlockParticle(blockCenter, block, this.breakingBlockFace));
									level.addLevelEvent(blockCenter, LevelEventPacket.EVENT_PARTICLE_PUNCH_BLOCK_DOWN + breakingBlockFace.getIndex(), block.getFullId());

									int breakTime = Mth.ceil(block.getBreakTime(inventory.getItemInHand(), this) * 20);
									level.addLevelEvent(pos, LevelEventPacket.EVENT_BLOCK_UPDATE_BREAK, breakTime <= 0 ? 0 : 65535 / breakTime);
								}
								break;
						}
					}

					this.setUsingItem(false);
				}

				if (!this.canContinueInputState()) {
					return;
				}
				NetworkInventoryAction[] inventoryActions = playerAuthInputPacket.getInventoryActions();
				if (!isServerAuthoritativeInventoryEnabled() && inventoryActions != null && inventoryActions.length != 0) {
					boolean resolve = true;

					skipped = false;
					actions = new ObjectArrayList<>(inventoryActions.length);
					for (NetworkInventoryAction networkAction : inventoryActions) {
						InventoryAction action = networkAction.createInventoryAction(this);

						if (action == null) {
							skipped = true;
							continue;
						}

						if (skipped && networkAction.windowId == ContainerIds.UI) {
							resolve = false;
							break;
						}

						actions.add(action);
					}
				}

				if (!this.canContinueInputState()) {
					return;
				}
				UseItemData useItemData = playerAuthInputPacket.getUseItemData();
				if (isServerAuthoritativeInventoryEnabled()) {
					handleServerAuthoritativeInputInventory(playerAuthInputPacket, inventoryActions, useItemData);
				} else if (useItemData != null) {
					Vector3f clickPos = useItemData.clickPos;
					BlockVector3 blockVector = useItemData.blockPos;
					BlockFace face = useItemData.face;
					int type = useItemData.actionType;
					boolean embeddedJavaClient = this.isJavaClient();
					JavaItemUseRouting.Route route = JavaItemUseRouting.resolveEmbeddedUse(
							embeddedJavaClient, embeddedJavaClient && this.supportsExplicitItemUseHand(),
							useItemData.hotbarSlot, type);

					if (route != JavaItemUseRouting.Route.REJECT) {
						ItemUseHand interactionHand = route.hand();
						ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);
						try {
							if (face != null || type == InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR) {
								switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:
								// Remove if client bug is ever fixed
								boolean spamBug = lastRightClickData != null && System.currentTimeMillis() - lastRightClickTime < 100 &&
										lastRightClickData.playerPos.distanceSquared(useItemData.playerPos) < Mth.EPSILON &&
										lastRightClickData.blockPos.equalsVec(blockVector) &&
										lastRightClickData.clickPos.distanceSquared(clickPos) < Mth.EPSILON &&
										Block.equals(lastRightClickData.block, useItemData.block) &&
										lastRightClickData.clientInteractPrediction == useItemData.clientInteractPrediction &&
										lastRightClickData.clientCooldownState == useItemData.clientCooldownState &&
//                                        lastRightClickData.itemInHand.equals(useItemData.itemInHand) &&
										lastRightClickData.hotbarSlot == useItemData.hotbarSlot;
								lastRightClickData = useItemData;
								lastRightClickTime = System.currentTimeMillis();
								if (spamBug /*&& !(useItemData.itemInHand instanceof ItemBlock)*/) {
									break;
								}

                                long itemSlotEpoch = this.getMovementEpoch();
                                boolean itemSlotReady = this.prepareItemUseSlot(useItemData.hotbarSlot, useItemData.itemInHand, interactionHand);
                                if (!itemSlotReady && (!this.isOnline() || !this.isAlive() || !this.isInputSessionActive()
                                        || this.getMovementEpoch() != itemSlotEpoch || !this.isCurrentInputPosition())) {
                                    break;
                                }
                                Item i = inventory.getItemInHand();
                                if (itemSlotReady && !i.is(Item.BRUSH)) {
                                    this.setDataFlag(DATA_FLAG_ACTION, false);
                                }
								boolean inventorySyncRequired = true;

								if (itemSlotReady && this.canInteractWithBlock(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
									if (this.isCreative()) {
										Vector3 blockPos;
										Block clientBlock = useItemData.block;
										if (clientBlock != null) {
											clientBlock.position(blockVector, level);
											blockPos = clientBlock;
										} else {
											blockPos = blockVector.asVector3();
										}
										Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
										clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
										BlockItemUseResult useResult = this.useItemOnBlockWithSync(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, clientPrediction, useItemData.itemInHand);
										inventorySyncRequired = useResult.inventorySyncRequired();
										if (useResult.item() != null) {
											break;
										}
									} else if (i.equals(useItemData.itemInHand)) {
										Item oldItem = i.clone();
										Vector3 blockPos;
										Block clientBlock = useItemData.block;
										if (clientBlock != null) {
											clientBlock.position(blockVector, level);
											blockPos = clientBlock;
										} else {
											blockPos = blockVector.asVector3();
										}
										Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
										clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
										Player predictedConsumer = this.predictedBlockConsumer(oldItem, useItemData.itemInHand, clientPrediction);
										InventorySlotReference predictedSource = predictedConsumer != null && !this.isMainThreadInputEnabled()
												? inventory.captureHeldItem() : null;
										BlockItemUseResult useResult = this.useItemOnBlockWithSync(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, clientPrediction, useItemData.itemInHand);
										inventorySyncRequired = useResult.inventorySyncRequired();
										if ((i = useResult.item()) != null) {
											if (i.getId() != 10000) {  // Hack
												if (!this.isMainThreadInputEnabled() && (!i.equals(oldItem) || i.getCount() != oldItem.getCount())) {
													if (predictedSource != null && (!predictedSource.isCurrent() || !predictedSource.isSelectedBy(inventory))) {
														predictedConsumer = null;
													}
													inventory.setItemInHand(i, predictedConsumer);
													inventory.sendHeldItem(this.getViewers().values());
												}
												break;
											}
										}
									}
								}

								if (inventorySyncRequired) inventory.sendHeldItem(this);

								if (blockVector.distanceSquared(this) > 10000) {
									break;
								}

								Block target = this.level.getBlock(blockVector.asVector3());
								block = target.getSide(face);

								this.level.sendBlocks(new Player[]{this}, new Block[]{target, block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
								this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target), level.getExtraBlock(block)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

								if (target.isDoor()) {
									BlockDoor door = (BlockDoor) target;
									Block part;

									if (door.isTop()) {
										part = target.down();

										if (part.getId() == target.getId()) {
											target = part;

											this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
										}
									}
								}
								break;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_BREAK_BLOCK:
								if (!this.spawned || !this.isAlive()) {
									break;
								}

								i = this.getInventory().getItemInHand();
								Item oldItem = i.clone();

								if (isBreakingBlock(blockVector)
										&& this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)
										&& (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
									this.breakingBlock = null;
									this.breakingBlockFace = null;
									if (this.isSurvival()) {
										this.getFoodData().updateFoodExpLevel(0.005f);
										if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
											inventory.setItemInHand(i);
											inventory.sendHeldItem(this.getViewers().values());
										}
									}
									break;
								}

								inventory.sendContents(this);
								inventory.sendHeldItem(this);

								if (blockVector.distanceSquared(this) < 10000) {
									target = this.level.getBlock(blockVector.asVector3());
									this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
									this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

									BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(blockVector);
									if (blockEntity instanceof BlockEntitySpawnable) {
										((BlockEntitySpawnable) blockEntity).spawnTo(this);
									}
								}

								break;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR:
							    if (!this.prepareItemUseSlot(useItemData.hotbarSlot, useItemData.itemInHand, interactionHand)) {
							        break;
							    }
								Vector3 directionVector = this.getDirectionVector();

								if (this.isCreative()) {
									item = this.inventory.getItemInHand();
								} else if (!this.inventory.getItemInHand().equals(useItemData.itemInHand)) {
//									server.getLogger().debug(getName() + " held desync\nC: " + useItemData.itemInHand + "\nS: " + inventory.getItemInHand());
//									this.inventory.sendHeldItem(this); // 这里不再强制同步, 尝试解决使用物品后快速切换物品的回弹问题
									break ;
								} else {
									item = this.inventory.getItemInHand();
								}

								InventorySlotReference itemSource = this.isMainThreadInputEnabled() ? this.inventory.captureHeldItem() : null;
								long itemSourceEpoch = itemSource == null ? 0 : this.getMovementEpoch();
								PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
                                if (isSpectator()) {
                                    interactEvent.setCancelled();
                                }
								this.server.getPluginManager().callEvent(interactEvent);
								if (!this.canContinueInputState()) return;
								if (interactEvent.isCancelled()
								        || itemSource != null && !this.canStartItemUse(itemSource, itemSourceEpoch)) {
								    if (itemSource == null) {
								        this.inventory.sendHeldItem(this);
								    } else {
								        this.resyncItemUse(itemSource);
								    }
								    break;
								}

								if (item.onClickAir(this, directionVector)) {
									if (this.isSurvivalLike()) {
										if (itemSource == null) {
										    this.inventory.setItemInHand(item);
										} else if (!itemSource.setItem(item)) {
										    this.resyncItemUse(itemSource);
										    break;
										}
									}

									if (itemSource != null && (!this.isOnline() || !this.isInputSessionActive()
									        || this.getMovementEpoch() != itemSourceEpoch || !itemSource.isSelectedBy(this.inventory))) {
									    break;
									}

									if (!this.isUsingItem()) {
										this.setUsingItem(item.canRelease());
										break;
									}

									// Used item
									//int ticksUsed = this.server.getTick() - this.startAction;
									int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

									this.completeItemUse(item, ticksUsed, false);
								}

								break;
								}
							}
						} finally {
							this.setItemInteractionHand(previousHand);
						}
					}
				}

				ItemStackRequest request = playerAuthInputPacket.getItemStackRequest();
				if (isServerAuthoritativeInventoryEnabled() && request != null) {
					ItemStackResponse response = handleItemStackRequest(request);

					ItemStackResponsePacket116 responsePacket = new ItemStackResponsePacket116();
					responsePacket.responses = new ItemStackResponse[]{response};
					dataPacket(responsePacket);
				}

				if (predictedInVehicle) {
					if (!inPredictedVehicle) {
						break;
					}

					if (!(riding instanceof EntityRideable rideable)) {
						break;
					}

					if (!riding.isControlling(this)) {
						break;
					}

					if (newPos.distanceSquared(riding) > 1000) {
						break;
					}

					float vehiclePitch;
					float vehicleYaw;
					if (getProtocol() >= AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
						vehiclePitch = playerAuthInputPacket.getVehiclePitch();
						vehicleYaw = playerAuthInputPacket.getVehicleYaw();

						if (riding instanceof EntityBoat) {
							EntityBoat boat = (EntityBoat) rideable;

							boolean paddleLeft = playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.PADDLE_LEFT);
							boolean paddleRight = playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.PADDLE_RIGHT);
							float left = 0;
							float right = 0;
							if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.UP)) {
								left = 0.04f;
								right = 0.04f;
								if (!paddleLeft || !paddleRight) {
									if (paddleLeft) {
										left = 0.05f;
										right = 0.02f;
									} else if (paddleRight) {
										left = 0.02f;
										right = 0.05f;
									}
								}
							} else if (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.DOWN)) {
								left = -0.004f;
								right = -0.004f;
								if (!paddleLeft || !paddleRight) {
									if (paddleLeft) {
										left = -0.005f;
										right = -0.002f;
									} else if (paddleRight) {
										left = -0.002f;
										right = -0.005f;
									}
								}
							} else if (!paddleLeft || !paddleRight) {
								if (paddleLeft) {
									left = 0.04f;
								} else if (paddleRight) {
									right = 0.04f;
								}
							}
							float random = 0.0002f * ThreadLocalRandom.current().nextFloat();
							left += left * random;
							right += right * random;

							float frameSecondsLeft = boat.getDataPropertyFloat(DATA_PADDLE_TIME_LEFT);
							if (Mth.sign(frameSecondsLeft) != Mth.sign(left)) {
								frameSecondsLeft = left;
							} else {
								frameSecondsLeft += left;
								if (frameSecondsLeft > 1000) {
									frameSecondsLeft -= 1000;
								} else if (frameSecondsLeft < -1000) {
									frameSecondsLeft += 1000;
								}
							}
							boat.setDataProperty(new FloatEntityData(DATA_PADDLE_TIME_LEFT, frameSecondsLeft));

							float frameSecondsRight = boat.getDataPropertyFloat(DATA_PADDLE_TIME_RIGHT);
							if (Mth.sign(frameSecondsRight) != Mth.sign(right)) {
								frameSecondsRight = right;
							} else {
								frameSecondsRight += right;
								if (frameSecondsRight > 1000) {
									frameSecondsRight -= 1000;
								} else if (frameSecondsRight < -1000) {
									frameSecondsRight += 1000;
								}
							}
							boat.setDataProperty(new FloatEntityData(DATA_PADDLE_TIME_RIGHT, frameSecondsRight));
						}
					} else {
						vehiclePitch = 0;
						vehicleYaw = playerAuthInputPacket.getYaw();
					}

					rideable.onPlayerInput(this, playerAuthInputPacket.getX(), playerAuthInputPacket.getY(), playerAuthInputPacket.getZ(), vehicleYaw, vehiclePitch);
				} else if (this.riding != null && (moveVecX != 0 || moveVecY != 0
						|| EntityRideable.isServerAuthoritativeRide(getProtocol())
						&& (playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.PADDLE_LEFT) || playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.PADDLE_RIGHT)))
						&& riding.isControlling(this)) {
					moveVecX = Mth.clamp(moveVecX, -1, 1);
					moveVecY = Mth.clamp(moveVecY, -1, 1);

					if (this.riding instanceof EntityRideable rideable) {
						if (EntityRideable.isServerAuthoritativeRide(getProtocol()) && rideable instanceof EntityBoat boat) {
							boolean paddleLeft = playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.PADDLE_LEFT);
							boolean paddleRight = playerAuthInputPacket.hasFlag(PlayerAuthInputFlags.PADDLE_RIGHT);

							switch (playerAuthInputPacket.getInputMode()) {
								case PlayerAuthInputPacket116220.INPUT_MODE_MOUSE_KEYBOARD:
									boolean temp = paddleLeft;
									paddleLeft = paddleRight;
									paddleRight = temp;
									break;
								case PlayerAuthInputPacket116220.INPUT_MODE_TOUCHSCREEN:
									if (playerAuthInputPacket.getInteractionModel() == PlayerAuthInputPacket116220.INTERACTION_MODEL_CLASSIC) {
										if (paddleLeft && paddleRight) {
											moveVecY = 1;
										} else if (paddleLeft) {
											moveVecX = -1;
										} else if (paddleRight) {
											moveVecX = 1;
										}
										break;
									}
								case PlayerAuthInputPacket116220.INPUT_MODE_GAME_PAD:
									paddleLeft = moveVecX < 0;
									paddleRight = moveVecX > 0;
									break;
							}

							float left = 0;
							float right = 0;
							if (moveVecY > 0) {
								left = 0.04f;
								right = 0.04f;
								if (!paddleLeft || !paddleRight) {
									if (paddleLeft) {
										left = 0.05f;
										right = 0.02f;
									} else if (paddleRight) {
										left = 0.02f;
										right = 0.05f;
									}
								}
							} else if (moveVecY < 0) {
								left = -0.004f;
								right = -0.004f;
								if (!paddleLeft || !paddleRight) {
									if (paddleLeft) {
										left = -0.005f;
										right = -0.002f;
									} else if (paddleRight) {
										left = -0.002f;
										right = -0.005f;
									}
								}

								float temp = left;
								left = right;
								right = temp;
							} else if (!paddleLeft || !paddleRight) {
								if (paddleLeft) {
									left = 0.04f;
								} else if (paddleRight) {
									right = 0.04f;
								}
							}
							float random = 0.0002f * ThreadLocalRandom.current().nextFloat();
							left += left * random;
							right += right * random;

							float frameSecondsLeft = boat.getDataPropertyFloat(DATA_PADDLE_TIME_LEFT);
							if (Mth.sign(frameSecondsLeft) != Mth.sign(left)) {
								frameSecondsLeft = left;
							} else {
								frameSecondsLeft += left;
								if (frameSecondsLeft > 1000) {
									frameSecondsLeft -= 1000;
								} else if (frameSecondsLeft < -1000) {
									frameSecondsLeft += 1000;
								}
							}
							boat.setDataProperty(new FloatEntityData(DATA_PADDLE_TIME_LEFT, frameSecondsLeft));

							float frameSecondsRight = boat.getDataPropertyFloat(DATA_PADDLE_TIME_RIGHT);
							if (Mth.sign(frameSecondsRight) != Mth.sign(right)) {
								frameSecondsRight = right;
							} else {
								frameSecondsRight += right;
								if (frameSecondsRight > 1000) {
									frameSecondsRight -= 1000;
								} else if (frameSecondsRight < -1000) {
									frameSecondsRight += 1000;
								}
							}
							boat.setDataProperty(new FloatEntityData(DATA_PADDLE_TIME_RIGHT, frameSecondsRight));
						} else if (moveVecX == 0 && moveVecY == 0) {
							break;
						}

						rideable.onPlayerInput(this, moveVecX, moveVecY);
					}

					new PlayerVehicleInputEvent(this, moveVecX, moveVecY).call();
				}

				break;
			case ProtocolInfo.MOVE_PLAYER_PACKET:
				if (this.serverAuthoritativeMovement) {
					break;
				}
				super.handleDataPacket(packet);
				break;
			case ProtocolInfo.ITEM_STACK_REQUEST_PACKET:
				if (!isServerAuthoritativeInventoryEnabled()) {
					super.handleDataPacket(packet);
					break;
				}
				if (!callPacketReceiveEvent(packet)) break;
				ItemStackRequestPacket116 itemStackRequestPacket = (ItemStackRequestPacket116) packet;
				ItemStackRequest[] requests = itemStackRequestPacket.requests;

				ItemStackResponse[] responses = new ItemStackResponse[requests.length];
				for (int i = 0; i < requests.length; i++) {
					responses[i] = handleItemStackRequest(requests[i]);
				}

				ItemStackResponsePacket116 response = new ItemStackResponsePacket116();
				response.responses = responses;
				dataPacket(response);
				break;
			case ProtocolInfo.MOB_EQUIPMENT_PACKET:
				if (!isServerAuthoritativeInventoryEnabled()) {
					super.handleDataPacket(packet);
					break;
				}
				if (!callPacketReceiveEvent(packet) || !this.spawned || !this.isAlive()) break;
				MobEquipmentPacket mobEquipmentPacket = (MobEquipmentPacket) packet;
				switch (mobEquipmentPacket.windowId) {
					case ContainerIds.OFFHAND:
						break;
					case ContainerIds.INVENTORY:
						int slot = mobEquipmentPacket.hotbarSlot;
						inventoryManager.onClientSelectHotbarSlot(slot);
						if (!selectHotbarSlot(slot)) {
							inventoryManager.syncSelectedHotbarSlot();
						}
						break;
				}
				break;
			case ProtocolInfo.PLAYER_HOTBAR_PACKET:
				if (!isServerAuthoritativeInventoryEnabled()) {
					super.handleDataPacket(packet);
					break;
				}
				if (!callPacketReceiveEvent(packet) || !this.spawned || !this.isAlive()) break;
				PlayerHotbarPacket hotbarPacket = (PlayerHotbarPacket) packet;
				switch (hotbarPacket.windowId) {
					case ContainerIds.OFFHAND:
						break;
					case ContainerIds.INVENTORY:
						int slot = hotbarPacket.selectedHotbarSlot;
						inventoryManager.onClientSelectHotbarSlot(slot);
						if (!selectHotbarSlot(slot)) {
							inventoryManager.syncSelectedHotbarSlot();
						}
						break;
				}
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}

	}

	@Override
	public int addWindow(Inventory inventory, Integer forceId, boolean isPermanent, boolean alwaysOpen) {
		if (isServerAuthoritativeInventoryEnabled()) {
			return super.addWindow(inventory, forceId, isPermanent, alwaysOpen);
		}

		Integer index = this.windows.get(inventory);
		if (index != null) {
			return index;
		}
		int cnt;
		if (forceId == null) {
			this.windowCnt = cnt = Math.max(FIRST_AVAILABLE_WINDOW_ID, ++this.windowCnt % 99);
		} else {
			cnt = forceId;
		}
		this.windows.forcePut(inventory, cnt);

		if (isPermanent) {
			this.permanentWindows.add(cnt);
		}

		if (this.spawned && inventory.open(this)) {
			return cnt;
		} else if (!alwaysOpen) {
			this.removeWindow(inventory);

			return -1;
		} else {
			inventory.getViewers().add(this);
		}

		return cnt;
	}

	@Override
	public void removeWindow(Inventory inventory) {
		if (isServerAuthoritativeInventoryEnabled()) {
			super.removeWindow(inventory);
			return;
		}
		if (!this.beginInventoryWindowRemoval(inventory)) {
			return;
		}
		Inventory previousInventoryCloseInProgress = this.inventoryCloseInProgress;
		this.inventoryCloseInProgress = inventory;
		try {
			inventory.close(this);
			if (!this.permanentWindows.contains(this.getWindowId(inventory))) {
				this.windows.remove(inventory);
			}
		} finally {
			this.inventoryCloseInProgress = previousInventoryCloseInProgress;
		}
	}

	protected boolean acknowledgeStaleWindowClose(int windowId, int windowType) {
		if (!shouldAcknowledgeWithoutClosing(this.spawned, windowId, this.inventoryOpen)) {
			return false;
		}
		if (this.lastOpenedWindowId == windowId) {
			this.lastOpenedWindowId = ContainerIds.NONE;
		}
		if (windowId == ContainerIds.INVENTORY) {
			this.inventoryOpen = false;
		}
		this.sendContainerCloseResponse(windowId, windowType);
		return true;
	}

	protected boolean handlePendingWindowClose(int windowId) {
		return false;
	}

	protected boolean deferPlayerInventoryOpen() {
		return false;
	}

	private static boolean shouldAcknowledgeWithoutClosing(boolean spawned, int windowId, boolean inventoryOpen) {
		return !spawned || windowId == ContainerIds.INVENTORY && !inventoryOpen;
	}

	@Override
	public void onInventoryOpen() {
		this.openInventory();
	}

	@Override
	public void sendAllInventories() {
		if (isServerAuthoritativeInventoryEnabled()) {
			inventoryManager.syncAll();
			return;
		}
		super.sendAllInventories();
	}

	@Override
	public void sendCreativeContents() {
//		if (this.isSpectator()) {
//			this.dataPacket(new CreativeContentPacket116());
//			return;
//		}

		DataPacket packet = CreativeItemsPalette.getCachedCreativeContentPacket(getAbstractProtocol(), isNetEaseClient());
		if (packet != null) {
			this.dataPacket(packet);
			return;
		}

		CreativeContentPacket116 pk = new CreativeContentPacket116();
		pk.entries = this.getCreativeItems().toArray(new Item[0]);
		this.dataPacket(pk);
	}

	public void openInventory() {
		this.openInventoryImmediately();
	}

	protected void openInventoryImmediately() {
		ContainerOpenPacket pk = new ContainerOpenPacket();
		pk.windowId = this.getWindowId(this.inventory);
		lastOpenedWindowId = pk.windowId;
		pk.type = this.inventory.getType().getNetworkType();
		pk.x = this.getFloorX();
		pk.y = this.getFloorY();
		pk.z = this.getFloorZ();
		pk.entityId = this.getId();
		this.dataPacket(pk);
	}

	public void closeInventory() {
		ContainerClosePacket pk = new ContainerClosePacket();
		pk.windowId = this.getWindowId(this.inventory);
		pk.wasServerInitiated = this.closingWindowId != pk.windowId;
		this.resetClosingWindowId(pk.windowId);
		this.dataPacket(pk);
	}

	@Override
	public boolean isServerAuthoritativeMovementEnabled() {
		return serverAuthoritativeMovement;
	}

	@Override
	public boolean isServerAuthoritativeBlockBreakingEnabled() {
		return serverAuthoritativeBlockBreaking;
	}

	@Override
	public boolean isServerAuthoritativeInventoryEnabled() {
		return SERVER_AUTHORITATIVE_INVENTORY && serverAuthoritativeInventory;
	}

	@Override
	public boolean onUpdate(int currentTick) {
		int tickDiff = currentTick - this.lastUpdate;
		if (tickDiff > 0) {
			if (this.serverAuthoritativeBlockBreaking && this.breakingBlockFace != null && this.isBreakingBlock() && this.spawned && this.isAlive()) {
				Vector3 blockCenter = breakingBlock.blockCenter();
				this.level.addParticle(new PunchBlockParticle(blockCenter, this.breakingBlock, this.breakingBlockFace));
				level.addLevelEvent(blockCenter, LevelEventPacket.EVENT_PARTICLE_PUNCH_BLOCK_DOWN + breakingBlockFace.getIndex(), breakingBlock.getFullId());

				int breakTime = Mth.ceil(breakingBlock.getBreakTime(inventory.getItemInHand(), this) * 20);
				level.addLevelEvent(breakingBlock, LevelEventPacket.EVENT_BLOCK_UPDATE_BREAK, breakTime <= 0 ? 0 : 65535 / breakTime);
			}

			this.emotedCurrentTick = false;
			// 新模式使用独立真实 tick 窗口；此处只清兼容路径的旧计数。
			this.currentTickAttackPacketCount = 0;

			if (clientTickCheckCd > 0) {
				clientTickCheckCd--;
			}
			if ((currentTick & INPUT_TICK_DESYNC_FORGET_CD) == 0 && clientTickTooFastCount > 0) {
				clientTickTooFastCount--;
			}

			if (isServerAuthoritativeInventoryEnabled()) {
				inventoryManager.flushPendingUpdates();
			}
		}
		return super.onUpdate(currentTick);
	}

	@Override
	protected void onClientTickUpdated(long tick) {
		clientTickRaw = tick;
        long tickDiff = tick - server.getTick();
		if (clientTick == -1) {
			clientTick = tick;
			clientTickDiff = tickDiff;
		} else if (tick != ++clientTick) {
			clientTick = tick;
			clientTickDiff = tickDiff;
		} else if (Math.abs(tickDiff - clientTickDiff) > INPUT_TICK_ACCEPTANCE_THRESHOLD && clientTickCheckCd <= 0) {
			if (server.getTicksPerSecondRaw() < 19 || server.getTicksPerSecondAverage() < 19) {
				if (clientTickTooFastCount > 0) {
					clientTickTooFastCount--;
				}
				clientTickCheckCd = INPUT_TICK_OVERLOAD_CHECK_CD;
				clientTickDiff = tickDiff;
			} else if (++clientTickTooFastCount > INPUT_TICK_DESYNC_KICK_THRESHOLD) {
				onPacketViolation(PacketViolationReason.VIOLATION_OVER_THRESHOLD, "input_fast", String.valueOf(tick));
				clientTickTooFastCount = 0;
				clientTickCheckCd = INPUT_TICK_CHECK_CD;
				clientTickDiff = tickDiff;
			} else {
				clientTickCheckCd = INPUT_TICK_CHECK_CD;
				clientTickDiff = tickDiff;
			}
		}
	}

	@Override
	public void playEmote(String emoteId, long entityRuntimeId, int flags) {
		playEmote(emoteId, entityRuntimeId, flags, 0);
	}

	@Override
	public void playEmote(String emoteId, long entityRuntimeId, int flags, int emoteTicks) {
		EmotePacket116 packet = new EmotePacket116();
		packet.emoteID = emoteId;
		packet.runtimeId = entityRuntimeId;
		packet.flags = flags;
		dataPacket(packet);
	}

	@Override
	public boolean emoteRequest() {
		if (emotedCurrentTick) {
			return false;
		}
		emotedCurrentTick = true;
		return true;
	}

	@Override
	@Nullable
	public InventoryListener getInventoryListener() {
		return isServerAuthoritativeInventoryEnabled() ? inventoryManager : super.getInventoryListener();
	}

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	protected ItemStackResponse handleItemStackRequest(ItemStackRequest request) {
		if (!isServerAuthoritativeInventoryEnabled() || !this.spawned || !this.isAlive()
				|| this.isSpectator() || !this.canContinueInputState() || processingItemStackRequest || executingInventoryTransaction) {
			return rejectedItemStackResponse(request.requestId);
		}
		processingItemStackRequest = true;
		inventoryManager.setCurrentItemStackRequestId(request.requestId);
		try {
			Inventory window = getCurrentWindow();
			if (inventoryManager.isWindowOpen(window) && window.handleAuthoritativeItemStackRequest(this, request)) {
				return rejectedItemStackResponse(request.requestId);
			}
			ItemStackRequestExecutor executor = new ItemStackRequestExecutor(this, request);
			InventoryTransaction transaction = executor.generateInventoryTransaction();
			executor.beginBundleCommit();
			if (transaction != null && !executeInventoryTransaction(transaction, request.requestId)) {
				return rejectedItemStackResponse(request.requestId);
			}
			return executor.buildItemStackResponse();
		} catch (ItemStackRequestProcessException exception) {
			log.debug("{}: ItemStackRequest #{} rejected: {}", getName(), request.requestId, exception.getMessage());
			inventoryManager.requestSyncAll();
			return rejectedItemStackResponse(request.requestId);
		} catch (RuntimeException exception) {
			log.error("{}: Failed to process ItemStackRequest #{}", getName(), request.requestId, exception);
			inventoryManager.requestSyncAll();
			return rejectedItemStackResponse(request.requestId);
		} finally {
			inventoryManager.syncMismatchedPredictedSlotChanges();
			inventoryManager.removeCurrentItemStackRequestId();
			inventoryManager.endBundleTransaction();
			processingItemStackRequest = false;
		}
	}

	private ItemStackResponse rejectedItemStackResponse(int requestId) {
		ItemStackResponse response = new ItemStackResponse();
		response.requestId = requestId;
		response.result = ItemStackResponse.RESULT_ERROR;
		return response;
	}

	protected boolean executeInventoryTransaction(InventoryTransaction transaction, int requestId) {
		if (executingInventoryTransaction) {
			return false;
		}
		executingInventoryTransaction = true;
		try {
			setUsingItem(false);
			inventoryManager.setCurrentItemStackRequestId(requestId);
			inventoryManager.addTransactionPredictedSlotChanges(transaction);
			if (!transaction.execute()) {
				inventoryManager.requestSyncAll();
				return false;
			}
		} catch (Exception e) {
			inventoryManager.requestSyncAll();
            log.debug("{}: Invalid inventory transaction {}", getName(), requestId, e);
			return false;
		} finally {
			inventoryManager.syncMismatchedPredictedSlotChanges();
			inventoryManager.removeCurrentItemStackRequestId();
			executingInventoryTransaction = false;
		}
		return true;
	}

	protected void handleServerAuthoritativeInventoryTransaction(int transactionType, int requestId,
			NetworkInventoryAction[] actions, @Nullable TransactionData transactionData,
			LegacySetItemSlotData[] requestChangedSlots, JavaItemUseRouting.Route route) {
		if (!this.spawned || !this.isAlive() || !this.canContinueInputState()
				|| executingInventoryTransaction || processingItemStackRequest) {
			return;
		}
		inventoryManager.setCurrentItemStackRequestId(requestId);
		try {
			inventoryManager.addRawPredictedSlotChanges(actions);
			switch (transactionType) {
				case InventoryTransactionPacket.TYPE_NORMAL -> {
					if (!isSpectator()) {
						handleNormalTransaction(actions, requestId);
					}
				}
				case InventoryTransactionPacket.TYPE_MISMATCH -> inventoryManager.requestSyncAll();
				case InventoryTransactionPacket.TYPE_USE_ITEM -> handleUseItemTransaction((UseItemData) transactionData, route);
				case InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY -> handleUseItemOnEntityTransaction((UseItemOnEntityData) transactionData, route);
				case InventoryTransactionPacket.TYPE_RELEASE_ITEM -> handleReleaseItemTransaction((ReleaseItemData) transactionData, route);
			}
			inventoryManager.setCurrentItemStackRequestId(requestId);
			syncRequestedInventorySlots(requestChangedSlots);
		} catch (IllegalArgumentException exception) {
			inventoryManager.requestSyncAll();
			log.debug("{}: Invalid inventory prediction {}", getName(), requestId, exception);
		} finally {
			inventoryManager.syncMismatchedPredictedSlotChanges();
			inventoryManager.removeCurrentItemStackRequestId();
		}
	}

	protected void handleServerAuthoritativeInputInventory(IPlayerAuthInputPacket packet,
			@Nullable NetworkInventoryAction[] actions, @Nullable UseItemData useItemData) {
		if (useItemData == null || !this.canContinueInputState()
				|| executingInventoryTransaction || processingItemStackRequest) {
			return;
		}
		boolean javaClient = this.isJavaClient();
		JavaItemUseRouting.Route route = JavaItemUseRouting.resolveEmbeddedUse(javaClient,
				javaClient && this.supportsExplicitItemUseHand(), useItemData.hotbarSlot, useItemData.actionType);
		if (route == JavaItemUseRouting.Route.REJECT) {
			return;
		}
		inventoryManager.setCurrentItemStackRequestId(packet.getLegacyRequestId());
		try {
			if (actions != null) {
				inventoryManager.addRawPredictedSlotChanges(actions);
			}
			handleUseItemTransaction(useItemData, route);
			inventoryManager.setCurrentItemStackRequestId(packet.getLegacyRequestId());
			LegacySetItemSlotData[] requestChangedSlots = packet.getRequestChangedSlots();
			if (requestChangedSlots != null) {
				syncRequestedInventorySlots(requestChangedSlots);
			}
		} catch (IllegalArgumentException exception) {
			inventoryManager.requestSyncAll();
			log.debug("{}: Invalid embedded inventory prediction", getName(), exception);
		} finally {
			inventoryManager.syncMismatchedPredictedSlotChanges();
			inventoryManager.removeCurrentItemStackRequestId();
		}
	}

	protected void syncRequestedInventorySlots(LegacySetItemSlotData[] requestChangedSlots) {
		// requestChangedSlots asks the server to always send out the contents of the specified slots, even if they haven't changed.
		// Handling these is necessary to ensure the client inventory stays in sync if the server rejects the transaction.
		// The most common example of this is equipping armor by right-click, which doesn't send a legacy prediction action for the destination armor slot.
		for (LegacySetItemSlotData containerInfo : requestChangedSlots) {
			int windowId = ItemStackContainerIdTranslator.translate(getAbstractProtocol(), isNetEaseClient(),
					containerInfo.containerId, inventoryManager.getCurrentWindowId());
			for (byte netSlot : containerInfo.changedSlotIndexes) {
				//TODO: HACK! The client sends an incorrect slot ID for the offhand as of 1.19.70 (though this doesn't really matter since the offhand has only 1 slot anyway)
				int slot = windowId == ContainerIds.OFFHAND ? 0 : netSlot & 0xff;
				ObjectIntPair<Inventory> inventoryAndSlot = inventoryManager.locateWindowAndSlot(windowId, slot);
				if (inventoryAndSlot != null) { // trigger the normal slot sync logic
					inventoryManager.onSlotChange(inventoryAndSlot.left(), inventoryAndSlot.rightInt());
				}
			}
		}
	}

	protected boolean selectServerAuthoritativeHotbarSlot(int hotbarSlot, ItemUseHand hand) {
		if (!this.spawned || !this.isAlive() || !this.canContinueInputState()) {
			return false;
		}
		if (hand == ItemUseHand.OFF_HAND) {
			return canUseServerAuthoritativeSlot(getOffhandInventory(), 0);
		}
		long movementEpoch = this.getMovementEpoch();
		return selectHotbarSlot(hotbarSlot) && this.canContinueInputState()
				&& this.getMovementEpoch() == movementEpoch && inventory.getHeldItemIndex() == hotbarSlot
				&& canUseServerAuthoritativeSlot(inventory, hotbarSlot);
	}

	private boolean canUseServerAuthoritativeSlot(Inventory inventory, int slot) {
		if (getInventorySlotDisplay(inventory, slot) == null) {
			return true;
		}
		if (isUsingItem() && (inventory == this.inventory && slot == this.inventory.getHeldItemIndex() && !isUsingOffhandItem()
				|| inventory == getOffhandInventory() && isUsingOffhandItem())) {
			setUsingItem(false);
		}
		inventoryManager.onSlotChange(inventory, slot);
		return false;
	}

	@Override
	protected final boolean canProcessPlayerBlockAction(int action) {
		return canProcessInventoryBlockAction(action, false);
	}

	private boolean canProcessInventoryBlockAction(int action, boolean embedded) {
		if (!isServerAuthoritativeInventoryEnabled() || !this.spawned || !this.isAlive()) {
			return true;
		}
		if (!embedded && (action == PlayerActionPacket.ACTION_BLOCK_PREDICT_DESTROY
				|| action == PlayerActionPacket.ACTION_BLOCK_CONTINUE_DESTROY)) {
			return true;
		}
		switch (action) {
			case PlayerActionPacket.ACTION_START_BREAK,
				 PlayerActionPacket.ACTION_STOP_BREAK,
				 PlayerActionPacket.ACTION_CONTINUE_BREAK,
				 PlayerActionPacket.ACTION_BLOCK_PREDICT_DESTROY,
				 PlayerActionPacket.ACTION_BLOCK_CONTINUE_DESTROY,
				 PlayerActionPacket.ACTION_ITEM_USE_ON_START -> {
				if (!canUseServerAuthoritativeSlot(inventory, inventory.getHeldItemIndex())) {
					cancelBlockBreakingWhileUsingItem();
					return false;
				}
			}
		}
		return true;
	}

	protected void handleNormalTransaction(NetworkInventoryAction[] actions, int requestId) {
		// When the ItemStackRequest system is used, this transaction type is used for dropping items by pressing Q.
		// I don't know why they don't just use ItemStackRequest for that too, which already supports dropping items by
		// clicking them outside an open inventory menu, but for now it is what it is.
		// Fortunately, this means we can be much stricter about the validation criteria.

		int actionCount = actions.length;
		if (actionCount > 2) {
			// Due to a bug in the game, this transaction type is still sent when a player edits a book.
			// We don't need these transactions for editing books, since we have BookEditPacket, so we can just ignore them.
			log.debug("Ignoring normal inventory transaction with {} actions (drop-item should have exactly 2 actions)", actionCount);
			return;
		}

		int sourceSlot = Integer.MIN_VALUE;
		Item clientItemStack = null;
		int droppedCount = Integer.MIN_VALUE;

		for (NetworkInventoryAction networkInventoryAction : actions) {
			if (networkInventoryAction.sourceType == NetworkInventoryAction.SOURCE_WORLD && networkInventoryAction.inventorySlot == InventoryTransactionPacket116.ACTION_MAGIC_SLOT_DROP_ITEM) {
				droppedCount = networkInventoryAction.newItem.getCount();
				if (droppedCount <= 0) {
					throw new IllegalArgumentException(getName() + " expected positive count for dropped item");
				}
			} else if (networkInventoryAction.sourceType == NetworkInventoryAction.SOURCE_CONTAINER && networkInventoryAction.windowId == ContainerIds.INVENTORY) {
				// mobile players can drop an item from a non-selected hotbar slot
				sourceSlot = networkInventoryAction.inventorySlot;
				clientItemStack = networkInventoryAction.oldItem;
			} else {
				log.debug("{} missing information in drop item transaction, need source slot, client item stack and dropped count", getName());
				return;
			}
		}
		if (sourceSlot == Integer.MIN_VALUE || clientItemStack == null || droppedCount == Integer.MIN_VALUE) {
			log.debug("{} missing information in drop item transaction, need source slot, client item stack and dropped count", getName());
			return;
		}

		if (!inventory.slotExists(sourceSlot)) {
			return; //TODO: size desync??
		}
		if (!canUseServerAuthoritativeSlot(inventory, sourceSlot)) {
			return;
		}

		Item sourceSlotItem = inventory.getItem(sourceSlot);
		if (sourceSlotItem.getCount() < droppedCount) {
			return;
		}

		// Sadly we don't have itemstack IDs here, so we have to compare the basic item properties to ensure that we're
		// dropping the item the client expects (inventory might be out of sync with the client).
		if (!NetworkItemHelper.matches(sourceSlotItem, clientItemStack, false)) {
			return;
		}

		Item droppedItem = sourceSlotItem.split(droppedCount);

		TransactionBuilder builder = new TransactionBuilder();
		builder.getInventory(inventory).setItem(sourceSlot, sourceSlotItem);
		builder.addAction(new DropItemAction(Items.air(), droppedItem));

		InventoryTransaction transaction = new InventoryTransaction(this, builder.generateActions());
		executeInventoryTransaction(transaction, requestId);
	}

	private Item restoreAuthoritativeItemClaim(Item clientItem) {
		Item serverItem = inventory.getItemInHand();
		if (!NetworkItemHelper.matches(serverItem, clientItem, false)) {
			return clientItem;
		}
		// 共享输入校验使用完整服务端物品，客户端只提供本次动作的数量快照。
		serverItem.setCount(clientItem.getCount());
		return serverItem;
	}

    private boolean prepareBlockActionsWhileUsingItem(PlayerBlockAction[] actions) {
        if (isServerAuthoritativeInventoryEnabled() && this.spawned && this.isAlive()
                && !canUseServerAuthoritativeSlot(inventory, inventory.getHeldItemIndex())) {
            cancelBlockBreakingWhileUsingItem();
            return false;
        }
        if (!isServerAuthoritativeInventoryEnabled() || isJavaClient() || !this.isUsingItem()) {
            return true;
        }
        boolean advancesBreaking = false;
        for (PlayerBlockAction action : actions) {
            switch (action.action) {
                case PlayerActionPacket.ACTION_START_BREAK,
                     PlayerActionPacket.ACTION_CONTINUE_BREAK,
                     PlayerActionPacket.ACTION_BLOCK_CONTINUE_DESTROY,
                     PlayerActionPacket.ACTION_STOP_BREAK,
                     PlayerActionPacket.ACTION_BLOCK_PREDICT_DESTROY -> advancesBreaking = true;
            }
        }
        if (advancesBreaking && this.finishItemUseBeforeBlockBreaking()) {
            return true;
        }
        this.cancelBlockBreakingWhileUsingItem();
        return false;
    }

    private void cancelBlockBreakingWhileUsingItem() {
        Block previous = this.breakingBlock;
        this.breakingBlock = null;
        this.breakingBlockFace = null;
        if (previous != null) {
            previous.level.addLevelEvent(previous, LevelEventPacket.EVENT_BLOCK_STOP_BREAK);
        }
    }

	protected void handleUseItemTransaction(UseItemData useItemData, JavaItemUseRouting.Route transactionRoute) {
		if (transactionRoute == JavaItemUseRouting.Route.REJECT
				|| !selectServerAuthoritativeHotbarSlot(useItemData.hotbarSlot, transactionRoute.hand())) {
			return;
		}
		boolean explicitItemUseHandAllowed = this.isJavaClient() && this.supportsExplicitItemUseHand();
		Item item;
		Vector3f clickPos = useItemData.clickPos;
		BlockVector3 blockVector = useItemData.blockPos;
		BlockFace face = useItemData.face;
		int type = useItemData.actionType;
		ItemUseHand interactionHand = transactionRoute.hand();
		ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);
		Item claimedItem = restoreAuthoritativeItemClaim(useItemData.itemInHand);

		try {
			if (interactionHand == ItemUseHand.OFF_HAND
					&& !JavaItemUseRouting.supportsUseItemAction(type)) {
				this.inventory.sendHeldItem(this);
				return;
			}
			if (face == null && type != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR) {
				return;
			}

			switch (type) {
			case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:

				// Remove if client bug is ever fixed
				boolean spamBug = lastRightClickData != null && System.currentTimeMillis() - lastRightClickTime < 100 &&
						lastRightClickData.face == face &&
						lastRightClickData.playerPos.distanceSquared(useItemData.playerPos) < Mth.EPSILON &&
						lastRightClickData.blockPos.equalsVec(blockVector) &&
						lastRightClickData.clickPos.distanceSquared(clickPos) < Mth.EPSILON && // signature spam bug has 0 distance, but allow some error
						Block.equals(lastRightClickData.block, useItemData.block) &&
						lastRightClickData.clientInteractPrediction == useItemData.clientInteractPrediction &&
						lastRightClickData.clientCooldownState == useItemData.clientCooldownState &&

						lastRightClickData.hotbarSlot == useItemData.hotbarSlot;
				if (spamBug) {
					return;
				}
				lastRightClickData = useItemData;
				lastRightClickTime = System.currentTimeMillis();

				long itemSlotEpoch = this.getMovementEpoch();
				boolean itemSlotReady = this.prepareItemUseSlot(useItemData.hotbarSlot, claimedItem, interactionHand);
				if (!itemSlotReady && (!this.isOnline() || !this.isAlive() || !this.isInputSessionActive()
						|| this.getMovementEpoch() != itemSlotEpoch || !this.isCurrentInputPosition())) {
					return;
				}
				Item i = inventory.getItemInHand();
				if (itemSlotReady && !i.is(Item.BRUSH)) {
					this.setDataFlag(DATA_FLAG_ACTION, false);
				}

				boolean clientPredictedFailure = false;
				boolean inventorySyncRequired = true;

				if (itemSlotReady && this.canInteractWithBlock(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
					if (this.isCreative()) {
						Vector3 blockPos;
						Block clientBlock = useItemData.block;
						if (clientBlock != null) {
							clientBlock.position(blockVector, level);
							blockPos = clientBlock;
						} else {
							blockPos = blockVector.asVector3();
						}
						Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
						clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
						BlockItemUseResult useResult = this.useItemOnBlockWithSync(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, clientPrediction, claimedItem);
						inventorySyncRequired = useResult.inventorySyncRequired();
						if (useResult.item() != null) {
							return;
						}
					} else if (i.equals(claimedItem)) {
						Vector3 blockPos;
						Block clientBlock = useItemData.block;
						if (clientBlock != null) {
							clientBlock.position(blockVector, level);
							blockPos = clientBlock;
						} else {
							blockPos = blockVector.asVector3();
						}
						Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
						clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
						clientPredictedFailure = clientPrediction != null && !clientPrediction;
						BlockItemUseResult useResult = this.useItemOnBlockWithSync(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, clientPrediction, claimedItem);
						inventorySyncRequired = useResult.inventorySyncRequired();
						if ((i = useResult.item()) != null) {
							return;
						}
					}
				}

				if (inventorySyncRequired) inventory.sendHeldItem(this);

				if (clientPredictedFailure) {
					return;
				}

				if (blockVector.distanceSquared(this) > 10000) {
					return;
				}

				Block target = level.getBlock(blockVector.asVector3());
				Block block0 = target.getSide(face);

				this.level.sendBlocks(new Player[]{this}, new Block[]{target, block0}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
				this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target), level.getExtraBlock(block0)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

				if (target instanceof BlockDoor) {
					BlockDoor door = (BlockDoor) target;
					Block part;

					if (door.isTop()) {
						part = target.down();

						if (part.getId() == target.getId()) {
							target = part;

							this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
						}
					}
				}
				return;
			case InventoryTransactionPacket116.USE_ITEM_ACTION_BREAK_BLOCK:
				if (!this.spawned || !this.isAlive()) {
					return;
				}

				if (isServerAuthoritativeInventoryEnabled() && !isJavaClient()
						&& !this.finishItemUseBeforeBlockBreaking()) {
					this.cancelBlockBreakingWhileUsingItem();
				}

				removeCurrentWindow();
				this.resetCraftingGridType();

				i = this.getInventory().getItemInHand();

				Item oldItem = i.clone();

				if (isBreakingBlock(blockVector)
						&& this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)
						&& (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
					this.breakingBlock = null;
					this.breakingBlockFace = null;
					if (this.isSurvival()) {
						this.getFoodData().updateFoodExpLevel(0.005f);
						if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
							inventory.setItemInHand(i);
							inventory.sendHeldItem(this.getViewers().values());
						}
					}
					return;
				}

				inventory.sendContents(this);
				inventory.sendHeldItem(this);

				if (blockVector.distanceSquared(this) < 10000) {
					target = this.level.getBlock(blockVector.asVector3());
					this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
					this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

					BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(blockVector);
					if (blockEntity instanceof BlockEntitySpawnable) {
						((BlockEntitySpawnable) blockEntity).spawnTo(this);
					}
				}

				return;
			case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR:
			    if (!this.prepareItemUseSlot(useItemData.hotbarSlot, claimedItem, interactionHand)) {
			        return;
			    }
				Vector3 directionVector = this.getDirectionVector();

				if (this.isCreative()) {
					item = this.inventory.getItemInHand();
				} else if (!this.inventory.getItemInHand().equals(claimedItem)) {

					return;
				} else {
					item = this.inventory.getItemInHand();
				}

				this.rebindStartedJavaItemUseHand(explicitItemUseHandAllowed, interactionHand);
				InventorySlotReference itemSource = this.inventory.captureHeldItem();
				long itemSourceEpoch = itemSource == null ? 0 : this.getMovementEpoch();
				Item beforeUse = itemSource == null ? item.clone() : itemSource.getSnapshot();
				PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
				if (isSpectator()) {
					interactEvent.setCancelled();
				}
				this.server.getPluginManager().callEvent(interactEvent);
				if (interactEvent.isCancelled()
				        || itemSource != null && !this.canStartItemUse(itemSource, itemSourceEpoch)) {
				    if (itemSource == null) {
				        this.inventory.sendHeldItem(this);
				    } else {
				        this.resyncItemUse(itemSource);
				    }
				    return;
				}

				boolean explicitShieldUse = JavaItemUseRouting.supportsExplicitShieldUse(
						explicitItemUseHandAllowed, item.getId());
				boolean itemAccepted = item.onClickAir(this, directionVector) || explicitShieldUse;
				if (itemAccepted) {
					if (this.isSurvivalLike() && !this.updateAuthoritativeItemUse(beforeUse, item, itemSource)) {
						this.resyncItemUse(itemSource);
						return;
					}

					if (itemSource != null && (!this.isOnline() || this.isMainThreadInputEnabled() && !this.isInputSessionActive()
					        || this.getMovementEpoch() != itemSourceEpoch || !itemSource.isSelectedBy(this.inventory))) {
					    return;
					}

					if (!this.isUsingItem()) {
						this.setUsingItem(item.canRelease() || explicitShieldUse);
						return;
					}

					// Used item
					//int ticksUsed = this.server.getTick() - this.startAction;
					int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

					this.completeItemUse(item, ticksUsed, explicitShieldUse);
				}

				return;
			default:

				break;
			}
		} finally {
			this.setItemInteractionHand(previousHand);
		}
	}

	protected void handleUseItemOnEntityTransaction(UseItemOnEntityData useItemOnEntityData, JavaItemUseRouting.Route transactionRoute) {
		if (transactionRoute == JavaItemUseRouting.Route.REJECT
				|| !selectServerAuthoritativeHotbarSlot(useItemOnEntityData.hotbarSlot, transactionRoute.hand())) {
			return;
		}
		Item item;
		int type = useItemOnEntityData.actionType;
		ItemUseHand interactionHand = transactionRoute.hand();
		ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

		try {
			if (interactionHand == ItemUseHand.OFF_HAND
					&& !JavaItemUseRouting.supportsEntityAction(type)) {
				this.inventory.sendHeldItem(this);
				return;
			}
			Entity target = this.level.getEntity(useItemOnEntityData.entityRuntimeId);
			if (target == null) {
				item = this.inventory.getItemInHand();
				PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, this.getDirectionVector(), BlockFace.UP, PlayerInteractEvent.Action.CLICK_UNKNOWN_ENTITY).setUnkownEntityId(useItemOnEntityData.entityRuntimeId);
				if (isSpectator()) {
					interactEvent.setCancelled();
				}
				this.server.getPluginManager().callEvent(interactEvent);
				return;
			}

			if (!NetworkItemHelper.matches(this.inventory.getItemInHand(), useItemOnEntityData.itemInHand, true)) {
				this.inventory.sendHeldItem(this);
			}

			item = this.inventory.getItemInHand();

			switch (type) {
				case InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_INTERACT:
					if (!this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : this.level.getMaxEntityInteractionReachDistanceInSurvival())) {
						break;
					}

					InventorySlotReference interactionSource = this.inventory.captureHeldItem();
					long interactionEpoch = interactionSource == null ? 0 : this.getMovementEpoch();
					long interactionTargetEpoch = interactionSource != null && target instanceof Player targetPlayer ? targetPlayer.getMovementEpoch() : 0;
					PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(this, target, item, useItemOnEntityData.clickPos);
					if (this.isSpectator()) playerInteractEntityEvent.setCancelled();
					getServer().getPluginManager().callEvent(playerInteractEntityEvent);

					if (playerInteractEntityEvent.isCancelled()
					        || interactionSource != null && !this.canStartEntityInteraction(interactionSource, interactionEpoch, target, interactionTargetEpoch)) {
					    if (interactionSource != null) {
					        this.resyncItemUse(interactionSource);
					    }
						break;
					}
					if (target.onInteract(this, item, useItemOnEntityData.clickPos) && this.isSurvival()) {
					    if (interactionSource != null) {
					        this.finishEntityInteractionItem(interactionSource, item, target);
					        break;
					    }
						if (item.isTool()) {
							if (item.useOn(target) && item.getDamage() > item.getMaxDurability()) {
								item = Items.air();
								level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BREAK);
							}
						} else {
							if (item.count > 1) {
								item.count--;
							} else {
								item = Items.air();
							}
						}

						this.inventory.setItemInHand(item);
					}
					break;
				case InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK:

					if (!this.claimAttackAttempt()) {
						return;
					}

					if (!this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : this.level.getMaxEntityInteractionReachDistanceInSurvival())) {
						break;
					}

					InventorySlotReference attackSource = this.inventory.captureHeldItem();
					long attackEpoch = attackSource == null ? 0 : this.getMovementEpoch();
					long targetEpoch = attackSource != null && target instanceof Player targetPlayer ? targetPlayer.getMovementEpoch() : 0;
					ItemAttackDamageEvent event = new ItemAttackDamageEvent(item);
					this.server.getPluginManager().callEvent(event);
					float itemDamage = event.getAttackDamage();

					Enchantment[] enchantments = item.getId() != Item.ENCHANTED_BOOK ? item.getEnchantments() : Enchantment.EMPTY;

					float damageBonus = 0;
					for (Enchantment enchantment : enchantments) {
						damageBonus += enchantment.getDamageBonus(this, target);
					}
					itemDamage += Mth.floor(damageBonus);

					Map<EntityDamageEvent.DamageModifier, Float> damage = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
					damage.put(EntityDamageEvent.DamageModifier.BASE, itemDamage);

					KnockbackProfile targetProfile = this.getKnockbackProfile();
					float knockBackH = targetProfile.getBaseH();
					float knockBackV = targetProfile.getBaseV();
					int knockBackEnchantment = !item.is(Item.ENCHANTED_BOOK) ? item.getEnchantmentLevel(Enchantment.KNOCKBACK) : 0;

					if (target instanceof Player) {
						if ((((Player) target).getGamemode() & 0x01) > 0) {
							break;
						} else if (!this.server.getConfiguration().isPvp() || this.level.getDifficulty() == 0 || !level.getGameRules().getBoolean(GameRule.PVP)) {
							break;
						}
					}

					if (attackSource != null && (!this.canStartItemUse(attackSource, attackEpoch)
					        || target.isClosed() || !target.isAlive() || target.getLevel() != this.level
					        || this.level.getEntity(target.getId()) != target
					        || target instanceof Player targetPlayer && targetPlayer.getMovementEpoch() != targetEpoch
					        || !this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : this.level.getMaxEntityInteractionReachDistanceInSurvival()))) {
					    this.resyncItemUse(attackSource);
					    break;
					}

					EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(this, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, knockBackH, knockBackV, enchantments);
					entityDamageByEntityEvent.getKnockbackProfile().setEnchantLevel(knockBackEnchantment);
					if (this.isSpectator()) entityDamageByEntityEvent.setCancelled();
					if (!target.attack(entityDamageByEntityEvent)) {
						if (item.isTool() && this.isSurvival()) {
							this.inventory.sendContents(this);
						}
						break;
					}

					for (Enchantment enchantment : enchantments) {
						enchantment.doPostAttack(item, this, target, null);
					}

					if (item.isTool() && this.isSurvival()) {
					    if (attackSource != null) {
					        this.finishAttackItem(attackSource, item, target);
					        return;
					    }
						if (item.useOn(target)) {
							if (item.getDamage() > item.getMaxDurability()) {
								this.inventory.setItemInHand(Items.air());
								level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BREAK);
							} else {
								this.inventory.setItemInHand(item);
							}
						} else {
							this.inventory.setItemInHand(item);
						}
					}
					return;
				default:
					break;
			}

		} finally {
			this.setItemInteractionHand(previousHand);
		}
	}

	protected void handleReleaseItemTransaction(ReleaseItemData releaseItemData, JavaItemUseRouting.Route transactionRoute) {
		if (transactionRoute == JavaItemUseRouting.Route.REJECT
				|| !selectServerAuthoritativeHotbarSlot(releaseItemData.hotbarSlot, transactionRoute.hand())) {
			return;
		}
		boolean explicitItemUseHandAllowed = this.isJavaClient() && this.supportsExplicitItemUseHand();
		Item item;

		if (this.isSpectator()) {
			this.sendAllInventories();
			return;
		}
		ItemUseHand interactionHand = transactionRoute.hand();
		if (explicitItemUseHandAllowed
				&& this.isUsingItem()
				&& interactionHand != this.getUsingItemHand()) {
			ItemUseHand previousHand = this.setItemInteractionHand(this.getUsingItemHand());
			try {
				this.inventory.sendHeldItem(this);
			} finally {
				this.setItemInteractionHand(previousHand);
				this.setUsingItem(false);
			}
			return;
		}
		ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

		try {
			try {
				int type = releaseItemData.actionType;
				switch (type) {
					case InventoryTransactionPacket116.RELEASE_ITEM_ACTION_RELEASE:
						if (this.isUsingItem()) {
							item = this.inventory.getItemInHand();
							if (this.isUsingOffhandItem() && !this.isUsingSameItem(item)) {
								this.inventory.sendHeldItem(this);
								return;
							}

							//int ticksUsed = this.server.getTick() - this.startAction;
							int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

							if (!item.onRelease(this, ticksUsed, releaseItemData.headRot)) {
								this.inventory.sendContents(this);
							}
						} else {
							this.inventory.sendContents(this);
						}
						return;
					case InventoryTransactionPacket116.RELEASE_ITEM_ACTION_CONSUME:
						log.debug("Unexpected release item action consume from {}", getName());
						return;
					default:
						break;
				}
			} finally {
				this.setUsingItem(false);
			}
		} finally {
			this.setItemInteractionHand(previousHand);
		}
	}

	@Override
	public boolean setCurrentWindow(Inventory inventory) {
		if (!isServerAuthoritativeInventoryEnabled()) {
			return super.setCurrentWindow(inventory);
		}
		if (currentWindow == inventory) {
			return true;
		}
		if (!this.spawned || closingAuthoritativeWindow || preparingAuthoritativeDisconnect) {
			return false;
		}

		InventoryOpenEvent event = new InventoryOpenEvent(inventory, this);
		event.call();
		if (event.isCancelled()) {
			return false;
		}

		if (!tryRemoveCurrentWindow()) {
			return false;
		}

		if (inventoryManager == null) {
			throw new IllegalStateException("Player cannot open inventories in this state");
		}
		log.debug("{} opening inventory {}@{}", getName(), inventory.getClass().getSimpleName(), System.identityHashCode(inventory));
		currentWindow = inventory;
		inventoryManager.onCurrentWindowChange(inventory);
		if (currentWindow != inventory) {
			return false;
		}
		inventory.onOpen(this);
		return true;
	}

	public void onAuthoritativeInventoryOpened(Inventory inventory) {
	}

	public void onAuthoritativeInventoryCloseAcknowledged(Inventory inventory) {
	}

	protected void onAuthoritativeInventoryClosed(Inventory inventory, boolean wasOpen) {
	}

	@Override
	public void removeCurrentWindow() {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.removeCurrentWindow();
			return;
		}
		tryRemoveCurrentWindow();
	}

	@Override
	public void removeAllWindows(boolean permanent) {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.removeAllWindows(permanent);
			return;
		}
		if (tryRemoveCurrentWindow() && permanent) {
			removePermanentInventories();
		}
	}

	public boolean tryRemoveCurrentWindow() {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.removeCurrentWindow();
			return true;
		}
		if (closingAuthoritativeWindow) {
			return false;
		}
		closingAuthoritativeWindow = true;
		try {
			Inventory previous = currentWindow;
			boolean wasOpen = inventoryManager.isWindowOpen(previous);
			Level origin = this.level;
			long movementEpoch = this.getMovementEpoch();
			boolean wasAlive = this.isAlive();
			notifyAuthoritativeWindowClose(previous);
			if (currentWindow != previous || deferredWindowClose != null || deferredWindowDeath) {
				return false;
			}
			if (!isConnected()) {
				detachTemporaryInventories();
			} else if (!this.isCurrentInputPosition() || this.level != origin || this.getMovementEpoch() != movementEpoch
					|| this.isAlive() != wasAlive || !evacuateTemporaryInventories()) {
				inventoryManager.requestSyncAll();
				return false;
			}
			if (currentWindow != previous) {
				return false;
			}
			if (previous != null) {
				previous.onClose(this);
			}
			if (currentWindow != previous) {
				return false;
			}
			inventoryManager.onCurrentWindowRemove();
			currentWindow = null;
			notifiedAuthoritativeCloseWindow = null;
			if (previous != null) {
				onAuthoritativeInventoryClosed(previous, wasOpen);
			}
			return true;
		} finally {
			closingAuthoritativeWindow = false;
			finishAuthoritativeWindowLifecycle();
		}
	}

	private void notifyAuthoritativeWindowClose(@Nullable Inventory inventory) {
		if (inventory != null && notifiedAuthoritativeCloseWindow != inventory) {
			notifiedAuthoritativeCloseWindow = inventory;
			new InventoryCloseEvent(inventory, this).call();
		}
	}

	@Override
	public void kill() {
		if (isServerAuthoritativeInventoryEnabled() && closingAuthoritativeWindow) {
			deferredWindowDeath = true;
			return;
		}
		super.kill();
	}

	@Override
	public void close(TextContainer message, String reason, boolean notify) {
		if (deferCloseDuringAuthoritativeDeath(message, reason, notify)) {
			return;
		}
		if (isServerAuthoritativeInventoryEnabled() && closingAuthoritativeWindow) {
			if (!preparingAuthoritativeDisconnect && isConnected() && !isClosed() && deferredWindowClose == null) {
				deferredWindowClose = new DeferredWindowClose(message, reason, notify, captureDeferredCloseObservation());
			}
			return;
		}
		if (preparingAuthoritativeDisconnect) {
			return;
		}
		if (isServerAuthoritativeInventoryEnabled() && isConnected() && !isClosed()) {
			preparingAuthoritativeDisconnect = true;
			try {
				if (currentWindow != null) {
					closingAuthoritativeWindow = true;
					try {
						notifyAuthoritativeWindowClose(currentWindow);
					} finally {
						closingAuthoritativeWindow = false;
						finishAuthoritativeWindowLifecycle();
					}
				}
				super.close(message, reason, notify);
			} finally {
				preparingAuthoritativeDisconnect = false;
			}
			return;
		}
		super.close(message, reason, notify);
	}

	private void finishAuthoritativeWindowLifecycle() {
		boolean death = deferredWindowDeath;
		boolean save = deferredWindowSave;
		boolean async = deferredWindowSaveAsync;
		DeferredWindowClose close = deferredWindowClose;
		deferredWindowDeath = false;
		deferredWindowSave = false;
		deferredWindowSaveAsync = false;
		deferredWindowClose = null;
		try {
			if (death && !isClosed()) {
				kill();
			}
			if (save && !isClosed()) {
				save(close == null && async);
			}
		} finally {
			if (close != null) {
				closeWithObservation(close.message(), close.reason(), close.notifyClient(), close.observation());
			}
		}
	}

	private record DeferredWindowClose(TextContainer message, String reason, boolean notifyClient,
	                                   DeferredCloseObservation observation) {
	}

	@Override
	protected void doCloseInventory() {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.doCloseInventory();
			return;
		}
		evacuateTemporaryInventories();
	}

	protected List<Inventory> getTemporaryInventories() {
		List<Inventory> inventories = new ArrayList<>(3);
		if (craftingGrid != null) {
			inventories.add(craftingGrid);
		}
		if (playerUIInventory != null) {
			inventories.add(getCursorInventory());
		}
		if (currentWindow instanceof PlayerUIComponent && !inventories.contains(currentWindow)) {
			inventories.add(currentWindow);
		}
		return inventories;
	}

	@Override
	protected List<Inventory> getAuthoritativeDeathInventories() {
		List<Inventory> inventories = super.getAuthoritativeDeathInventories();
		if (temporaryInventoryRecovery != null) {
			inventories.add(temporaryInventoryRecovery);
		}
		return inventories;
	}

	protected boolean evacuateTemporaryInventories() {
		TransactionBuilder builder = new TransactionBuilder();
		for (Inventory temporary : getTemporaryInventories()) {
			Collection<Item> contents = temporary.getContents().values();
			if (!contents.isEmpty()) {
				Item[] drops = builder.getInventory(inventory).addItem(contents.toArray(new Item[0]));
				for (Item drop : drops) {
					builder.addAction(new DropItemAction(Items.air(), drop));
				}
				builder.getInventory(temporary).clearAll();
			}
		}
		List<InventoryAction> actions = builder.generateActions();
		if (actions.isEmpty()) {
			return true;
		}
		boolean evacuated = new InventoryTransaction(this, actions) {
			@Override
			protected boolean callExecuteEvent() {
				return super.callExecuteEvent() && validateAuthoritativeContext();
			}

			@Override
			protected boolean validateAuthoritativeContext() {
				return !deferredWindowDeath && deferredWindowClose == null && isCurrentInputPosition()
						&& super.validateAuthoritativeContext();
			}
		}.execute();
		if (!evacuated) {
			log.debug("{}: Temporary inventory evacuation was rejected; items and window are retained", getName());
		}
		return evacuated;
	}

	private void detachTemporaryInventories() {
		if (detachedTemporaryInventories) {
			return;
		}
		List<Inventory> temporary = getTemporaryInventories();
		ListTag<CompoundTag> saved = TemporaryInventoryRecovery.snapshot(temporary, temporaryInventoryRecovery);
		temporaryInventoryRecovery = new TemporaryInventoryRecovery(this, saved);
		// 断线时已保存的临时物品转交恢复区，不能再掉落或触发第二次物品转移。
		for (Inventory inventory : temporary) {
			if (inventory instanceof BaseInventory base) {
				base.slots.clear();
			}
		}
		detachedTemporaryInventories = true;
	}

	@Override
	public void save(boolean async) {
		if (deferSaveDuringAuthoritativeDeath(async)) {
			return;
		}
		if (isServerAuthoritativeInventoryEnabled() && closingAuthoritativeWindow) {
			deferredWindowSaveAsync = deferredWindowSave ? deferredWindowSaveAsync && async : async;
			deferredWindowSave = true;
			return;
		}
		if (isServerAuthoritativeInventoryEnabled() && !isConnected() && !isClosed() && currentWindow != null) {
			tryRemoveCurrentWindow();
		}
		if (preparingAuthoritativeDisconnect) {
			async = false;
		}
		if (isServerAuthoritativeInventoryEnabled() && namedTag != null) {
			ListTag<CompoundTag> saved = TemporaryInventoryRecovery.snapshot(
					detachedTemporaryInventories ? List.of() : getTemporaryInventories(), temporaryInventoryRecovery);
			namedTag.putList(saved);
		}
		super.save(async);
	}

	@Override
	public void clearPendingInventoryRecovery() {
		if (!isServerAuthoritativeInventoryEnabled() || temporaryInventoryRecovery == null) {
			return;
		}
		TemporaryInventoryRecovery recovery = temporaryInventoryRecovery;
		boolean previouslyExecuting = executingInventoryTransaction;
		executingInventoryTransaction = true;
		try {
			recovery.clearAll();
			if (temporaryInventoryRecovery == recovery && recovery.isEmpty()) {
				temporaryInventoryRecovery = null;
			}
		} finally {
			executingInventoryTransaction = previouslyExecuting;
		}
	}

	public void restoreTemporaryInventoryItems() {
		if (isServerAuthoritativeInventoryEnabled() && temporaryInventoryRecovery != null
				&& isConnected() && isAlive() && !processingItemStackRequest && !executingInventoryTransaction
				&& !closingAuthoritativeWindow) {
			executingInventoryTransaction = true;
			try {
				temporaryInventoryRecovery.restore();
			} finally {
				executingInventoryTransaction = false;
			}
		}
	}

	@Override
	public void sendContainerData(Inventory inventory, int propertyId, int value) {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.sendContainerData(inventory, propertyId, value);
			return;
		}

		if (inventoryManager == null) {
			return;
		}
		inventoryManager.syncData(inventory, propertyId, value);
	}

	@Override
	public void sendEnchantingTableOptions(List<EnchantmentOption> options) {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.sendEnchantingTableOptions(options);
			return;
		}

		if (inventoryManager == null) {
			return;
		}
		inventoryManager.syncEnchantingTableOptions(options.toArray(new EnchantmentOption[0]));
	}

	@Override
	public void openBlockEditor(int x, int y, int z, int type) {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.openBlockEditor(x, y, z, type);
			return;
		}

		if (inventoryManager.isCurrentBlockEditor(x, y, z, type) || !tryRemoveCurrentWindow()) {
			return;
		}
		inventoryManager.openBlockEditor(x, y, z, type);
	}

	@Override
	protected void addDefaultWindows() {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.addDefaultWindows();
			return;
		}

		this.addPermanentInventory(this.inventory, ContainerIds.INVENTORY);
		this.addPermanentInventory(this.armorInventory, ContainerIds.ARMOR);
		this.addPermanentInventory(this.offhandInventory, ContainerIds.OFFHAND);

		this.playerUIInventory = new PlayerUIInventory(this);
		this.playerUIInventory.onOpen(this);
		this.addPermanentInventory(this.playerUIInventory.getCursorInventory(), ContainerIds.UI);

		this.craftingGrid = this.playerUIInventory.getCraftingGrid();
		this.addPermanentInventory(this.craftingGrid, ContainerIds.NONE);
	}

	protected void addPermanentInventory(Inventory inventory, int id) {
		inventory.onOpen(this);
		permanentWindowList.add(inventory);
	}

	@Override
	protected void removePermanentInventories() {
		if (!isServerAuthoritativeInventoryEnabled()) {
			super.removePermanentInventories();
			return;
		}

		for (Inventory inventory : permanentWindowList) {
			inventory.onClose(this);
		}
		permanentWindowList.clear();
	}

	@Override
	protected void doFirstSpawn() {
		super.doFirstSpawn();

		if (isServerAuthoritativeInventoryEnabled()) {
			restoreTemporaryInventoryItems();
			inventoryManager.syncAll();
			inventoryManager.syncSelectedHotbarSlot();
		}
	}

	@Override
	protected void respawn() {
		super.respawn();

		if (isServerAuthoritativeInventoryEnabled()) {
			inventoryManager.syncAll();
		}
	}

	protected boolean selectHotbarSlot(int hotbarSlot) {
		if (!inventory.isHotbarSlot(hotbarSlot)) {
			return false;
		}
		if (hotbarSlot == inventory.getHeldItemIndex()) {
			return true;
		}

		Item item = inventory.getItem(hotbarSlot);
		int previousSlot = inventory.getHeldItemIndex();
		long movementEpoch = this.getMovementEpoch();
		PlayerItemHeldEvent ev = new PlayerItemHeldEvent(this, item, hotbarSlot);
		ev.call();
		if (ev.isCancelled() || !this.canContinueInputState() || this.getMovementEpoch() != movementEpoch
				|| inventory.getHeldItemIndex() != previousSlot) {
			return false;
		}

		if (fishing != null) {
			if (!item.equals(fishing.rod)) {
				stopFishing(false);
			}
		}

		inventory.setHeldItemIndex(hotbarSlot);
		if (!this.isMainThreadInputEnabled() || this.getUsingItemHand() == ItemUseHand.MAIN_HAND) {
			setUsingItem(false);
		}
		return true;
	}

	@Override
	public int getNextItemStackId() {
		return isServerAuthoritativeInventoryEnabled() ? inventoryManager.getNextItemStackId() : super.getNextItemStackId();
	}

	@Override
	public int getNextDynamicRecipeId() {
		return isServerAuthoritativeInventoryEnabled() ? inventoryManager.getNextDynamicRecipeId() : super.getNextDynamicRecipeId();
	}

	@Override
	public int getNextDynamicContainerId() {
		return isServerAuthoritativeInventoryEnabled() ? inventoryManager.getBundleRegistry().getNextContainerId() : super.getNextDynamicContainerId();
	}
}
