package org.itxtech.synapseapi;

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
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityFullNames;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.passive.EntityAbstractHorse;
import cn.nukkit.entity.passive.EntityCamel;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.ItemAttackDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.TakeResultAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.*;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.AnimatePacket.Action;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.ClientChainData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.event.player.SynapsePlayerInputModeChangeEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket.PlayerBlockAction;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.SharedConstants.EXPERIMENTAL_COMBAT_KNOCKBACK_TEST;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class SynapsePlayer116 extends SynapsePlayer113 {

	protected boolean inventoryOpen;

	protected boolean serverAuthoritativeMovement = SERVER_AUTHORITATIVE_MOVEMENT;
	private long clientTick = -1;
	private long clientTickDiff;
	private int clientTickTooFastCount;
	private int clientTickCheckCd;

	/**
	 * Server Authoritative Movement is required.
	 */
	protected boolean serverAuthoritativeBlockBreaking = SERVER_AUTHORITATIVE_BLOCK_BREAKING;
	protected BlockFace breakingBlockFace;

	private int currentTickAttackPacketCount = 0;

	private boolean emotedCurrentTick;

	public SynapsePlayer116(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
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
		startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
		startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.difficulty = this.server.getDifficulty();
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
		startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
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
						&& (interactPacket.target == getLocalEntityId() || isRiding() && interactPacket.target == riding.getId() && (!riding.getDataFlag(DATA_FLAG_TAMED) || riding.getNetworkId() == EntityID.SKELETON_HORSE))
						&& !this.inventoryOpen && !isSpectator()) {
//					this.openInventory();
					this.inventory.open(this);
					this.inventoryOpen = true;
				}
				super.handleDataPacket(packet);
				break;
			case ProtocolInfo.CONTAINER_CLOSE_PACKET:
				ContainerClosePacket containerClosePacket = (ContainerClosePacket) packet;
				if (!this.spawned || containerClosePacket.windowId == ContainerIds.INVENTORY && !inventoryOpen) {
					break;
				}

				Inventory windowInventory = this.windowIndex.get(containerClosePacket.windowId);
				if (windowInventory != null) {
					this.server.getPluginManager().callEvent(new InventoryCloseEvent(windowInventory, this));

					if (containerClosePacket.windowId == ContainerIds.INVENTORY) this.inventoryOpen = false;

					if (this instanceof SynapsePlayer116100) ((SynapsePlayer116100) this).removeWindow(this.windowIndex.get(containerClosePacket.windowId), true);
					else this.removeWindow(this.windowIndex.get(containerClosePacket.windowId));
				} else {
					this.getServer().getLogger().debug(getName() + " unopened window: " + containerClosePacket.windowId);
				}

				if (containerClosePacket.windowId == -1) {
					this.craftingType = CRAFTING_SMALL;
					this.resetCraftingGridType();
					this.addWindow(this.craftingGrid, ContainerIds.NONE);

					ContainerClosePacket pk = new ContainerClosePacket();
					pk.windowId = -1;
					this.dataPacket(pk);
				} else { // Close bugged inventory
					ContainerClosePacket pk = new ContainerClosePacket();
					pk.windowId = containerClosePacket.windowId;
					this.dataPacket(pk);

					for (Inventory open : new ArrayList<>(this.windows.keySet())) {
						if (open instanceof ContainerInventory) {
							this.removeWindow(open);
						}
					}
				}
				break;
			case ProtocolInfo.INVENTORY_TRANSACTION_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				InventoryTransactionPacket116 transactionPacket = (InventoryTransactionPacket116) packet;

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
					}

					if ((this.craftingType >> 3) != 2) {
						break;
					}
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
					case InventoryTransactionPacket116.TYPE_USE_ITEM:
						UseItemData useItemData = (UseItemData) transactionPacket.transactionData;

						Vector3f clickPos = useItemData.clickPos;
						BlockVector3 blockVector = useItemData.blockPos;
						BlockFace face = useItemData.face;
						int type = useItemData.actionType;

						if (face == null && type != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR) {
							break packetswitch;
						}

						switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:
								// Remove if client bug is ever fixed
								boolean spamBug = lastRightClickData != null && System.currentTimeMillis() - lastRightClickTime < 100.0 &&
										lastRightClickData.face == face &&
										lastRightClickData.playerPos.distanceSquared(useItemData.playerPos) < Mth.EPSILON &&
										lastRightClickData.blockPos.equalsVec(blockVector) &&
										lastRightClickData.clickPos.distanceSquared(clickPos) < Mth.EPSILON; // signature spam bug has 0 distance, but allow some error
								if (spamBug /*&& !(useItemData.itemInHand instanceof ItemBlock)*/) {
									return;
								}
								lastRightClickData = useItemData;
								lastRightClickTime = System.currentTimeMillis();

								this.setDataFlag(DATA_FLAG_ACTION, false);

								// 从useItemData中设置玩家坐标，用于最精准的碰撞箱判断
								this.newPosition = useItemData.playerPos.subtract(0, this.getBaseOffset(), 0);
								boolean entityInBlock = false;

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
									if (this.isCreative()) {
										Item i = inventory.getItemInHand();
										if (this.level.useItemOn(blockVector.asVector3(), i, face, clickPos.x, clickPos.y, clickPos.z, this) != null) {
											break packetswitch;
										}
									} else if (inventory.getItemInHand().equals(useItemData.itemInHand)) {
										Item i = inventory.getItemInHand();
										Item oldItem = i.clone();
										//TODO: Implement adventure mode checks
										if ((i = this.level.useItemOn(blockVector.asVector3(), i, face, clickPos.x, clickPos.y, clickPos.z, this)) != null) {
											if (i.getId() == 10000) {  // Hack!
												entityInBlock = true;
											} else {
												if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
													inventory.setItemInHand(i);
													inventory.sendHeldItem(this.getViewers().values());
												}
												break packetswitch;
											}
										}
									}
								}

								// 解决卡物品栏问题（只发送物品正确的物品栏）
                                if (inventory.getItemInHand().getId() == useItemData.itemInHand.getId() && inventory.getItemInHand().getCount() != useItemData.itemInHand.getCount()) {
									inventory.sendHeldItem(this);
								}

								if (blockVector.distanceSquared(this) > 10000) {
									break packetswitch;
								}

								Runnable blockSend = () -> {
									Block target = this.level.getBlock(blockVector.asVector3());
									Block block0 = target.getSide(face);

									this.level.sendBlocks(new Player[]{SynapsePlayer116.this}, new Block[]{target, block0}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
								};

								// 如果与玩家较近，则延迟发送
								if (entityInBlock && blockVector.add(0.5, 0.5, 0.5).distanceSquared(this) < 4) {
									this.server.getScheduler().scheduleDelayedTask(SynapseAPI.getInstance(), blockSend, 20);
								} else {
									blockSend.run();
								}

								Block target = level.getBlock(blockVector.asVector3());

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

								this.resetCraftingGridType();

								Item i = this.getInventory().getItemInHand();

								Item oldItem = i.clone();

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL) && (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
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

									BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(blockVector);
									if (blockEntity instanceof BlockEntitySpawnable) {
										((BlockEntitySpawnable) blockEntity).spawnTo(this);
									}
								}

								break packetswitch;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR:
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

								PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);

								this.server.getPluginManager().callEvent(interactEvent);

								if (interactEvent.isCancelled()) {
									this.inventory.sendHeldItem(this);
									break packetswitch;
								}

								if (item.onClickAir(this, directionVector)) {
									if (this.isSurvivalLike()) {
										this.inventory.setItemInHand(item);
									}

									if (!this.isUsingItem()) {
										this.setUsingItem(item.canRelease());
										break packetswitch;
									}

									// Used item
									//int ticksUsed = this.server.getTick() - this.startAction;
									int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

									this.setUsingItem(false);

									if (!item.onUse(this, ticksUsed)) {
										this.inventory.sendContents(this);
									}

									if (item.canRelease()) {
										this.setUsingItem(true);
									}
								}

								break packetswitch;
							default:
								//unknown
								break;
						}
						break;
					case InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY:
						UseItemOnEntityData useItemOnEntityData = (UseItemOnEntityData) transactionPacket.transactionData;

						Entity target = this.level.getEntity(useItemOnEntityData.entityRuntimeId);
						if (target == null) {
							item = this.inventory.getItemInHand();
							PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, this.getDirectionVector(), BlockFace.UP, PlayerInteractEvent.Action.CLICK_UNKNOWN_ENTITY).setUnkownEntityId(useItemOnEntityData.entityRuntimeId);
							this.server.getPluginManager().callEvent(interactEvent);
							return;
						}

						type = useItemOnEntityData.actionType;

						if (!useItemOnEntityData.itemInHand.equalsExact(this.inventory.getItemInHand())) {
							this.inventory.sendHeldItem(this);
						}

						item = this.inventory.getItemInHand();

						switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_INTERACT:
								if (!this.canInteract(target, isCreative() ? MAX_REACH_DISTANCE_ENTITY_INTERACTION : 5)) {
									break;
								}

								PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(this, target, item, useItemOnEntityData.clickPos);
								if (this.isSpectator()) playerInteractEntityEvent.setCancelled();
								getServer().getPluginManager().callEvent(playerInteractEntityEvent);

								if (playerInteractEntityEvent.isCancelled()) {
									break;
								}
								if (target.onInteract(this, item, useItemOnEntityData.clickPos) && this.isSurvival()) {
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
								if (++currentTickAttackPacketCount >= 2) {
									return;
								}

								// 命中边缘时有极小概率没有看着实体 (InteractPacket::InteractUpdate-4)
/*
								if (this.lookAtEntity != target) {
									this.violation += 6;
									break;
								}
*/
								if (!this.canInteract(target, isCreative() ? MAX_REACH_DISTANCE_ENTITY_INTERACTION : 5)) {
									break;
								}

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

								float knockBackH = EntityDamageByEntityEvent.GLOBAL_KNOCKBACK_H;
								float knockBackV = EntityDamageByEntityEvent.GLOBAL_KNOCKBACK_V;
								Enchantment knockBackEnchantment = !item.is(Item.ENCHANTED_BOOK) ? item.getEnchantment(Enchantment.KNOCKBACK) : null;
								if (knockBackEnchantment != null) {
									knockBackH += knockBackEnchantment.getLevel() * 0.1f;
									knockBackV += knockBackEnchantment.getLevel() * 0.1f;
								}

								if (target instanceof Player) {
									if ((((Player) target).getGamemode() & 0x01) > 0) {
										break;
									} else if (!this.server.getConfiguration().isPvp() || this.server.getDifficulty() == 0) {
										break;
									}
								}

								EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(this, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, knockBackH, knockBackV, enchantments);
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

						break;
					case InventoryTransactionPacket116.TYPE_RELEASE_ITEM:
						if (!callPacketReceiveEvent(packet)) break;
						if (this.isSpectator()) {
							this.sendAllInventories();
							break packetswitch;
						}
						ReleaseItemData releaseItemData = (ReleaseItemData) transactionPacket.transactionData;

						try {
							type = releaseItemData.actionType;
							switch (type) {
								case InventoryTransactionPacket116.RELEASE_ITEM_ACTION_RELEASE:
									if (this.isUsingItem()) {
										item = this.inventory.getItemInHand();

										//int ticksUsed = this.server.getTick() - this.startAction;
										int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

										if (!item.onRelease(this, ticksUsed)) {
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
						break;
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

				if (!isServerAuthoritativeMovementEnabled()) {
					onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "auth_input");
					return;
				}

				IPlayerAuthInputPacket playerAuthInputPacket = (IPlayerAuthInputPacket) packet;
				if (!validateCoordinate(playerAuthInputPacket.getX()) || !validateCoordinate(playerAuthInputPacket.getY()) || !validateCoordinate(playerAuthInputPacket.getZ())
						|| !validateFloat(playerAuthInputPacket.getPitch()) || !validateFloat(playerAuthInputPacket.getYaw()) || !validateFloat(playerAuthInputPacket.getHeadYaw())
						|| !validateFloat(playerAuthInputPacket.getDeltaX()) || !validateFloat(playerAuthInputPacket.getDeltaY()) || !validateFloat(playerAuthInputPacket.getDeltaZ())
						|| !validateFloat(playerAuthInputPacket.getVrGazeDirectionX()) || !validateFloat(playerAuthInputPacket.getVrGazeDirectionY()) || !validateFloat(playerAuthInputPacket.getVrGazeDirectionZ())
						|| !validateFloat(playerAuthInputPacket.getAnalogMoveVecX()) || !validateFloat(playerAuthInputPacket.getAnalogMoveVecZ())) {
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

				if (this.getLoginChainData().getCurrentInputMode() != playerAuthInputPacket.getInputMode()) {
					int from = this.getLoginChainData().getCurrentInputMode();
					this.getLoginChainData().setCurrentInputMode(playerAuthInputPacket.getInputMode());
					this.getServer().getPluginManager().callEvent(new SynapsePlayerInputModeChangeEvent(this, from, playerAuthInputPacket.getInputMode()));
				}

				long inputFlags = playerAuthInputPacket.getInputFlags();
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_SPRINTING)) != 0 && !this.isSprinting()) {
					PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, true);
					if (hasEffect(Effect.BLINDNESS)) {
						playerToggleSprintEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleSprintEvent);
					if (playerToggleSprintEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSprinting(true);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_STOP_SPRINTING)) != 0 && this.isSprinting()) {
					PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleSprintEvent);
					if (playerToggleSprintEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSprinting(false);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_SNEAKING)) != 0 && !this.isSneaking()) {
					PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, true);
					this.server.getPluginManager().callEvent(playerToggleSneakEvent);
					if (playerToggleSneakEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSneaking(true);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_STOP_SNEAKING)) != 0 && this.isSneaking()) {
					PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleSneakEvent);
					if (playerToggleSneakEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSneaking(false);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_SWIMMING)) != 0 && !this.isSwimming()) {
					PlayerToggleSwimEvent playerToggleSwimEvent = new PlayerToggleSwimEvent(this, true);
					this.server.getPluginManager().callEvent(playerToggleSwimEvent);
					if (playerToggleSwimEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSwimming(true);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_STOP_SWIMMING)) != 0 && this.isSwimming()) {
					PlayerToggleSwimEvent playerToggleSwimEvent = new PlayerToggleSwimEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleSwimEvent);
					if (playerToggleSwimEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setSwimming(false);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_JUMPING)) != 0) {
					this.server.getPluginManager().callEvent(new PlayerJumpEvent(this));

					if (EXPERIMENTAL_COMBAT_KNOCKBACK_TEST && this.isSprinting()) {
						this.motionX = playerAuthInputPacket.getDeltaX();
						this.motionY = playerAuthInputPacket.getDeltaY();
						this.motionZ = playerAuthInputPacket.getDeltaZ();
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_GLIDING)) != 0 && !this.isGliding()) {
					PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
					Item chestplate = getArmorInventory().getChestplate();
					if (chestplate.getId() != Item.ELYTRA || chestplate.getDamage() >= chestplate.getMaxDurability() - 1) {
						playerToggleGlideEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleGlideEvent);
					if (playerToggleGlideEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setGliding(true);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_STOP_GLIDING)) != 0 && this.isGliding()) {
					PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleGlideEvent);
					if (playerToggleGlideEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setGliding(false);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputFlags.START_CRAWLING)) != 0 && !this.isCrawling()) {
					PlayerToggleCrawlEvent playerToggleCrawlEvent = new PlayerToggleCrawlEvent(this, true);
					this.server.getPluginManager().callEvent(playerToggleCrawlEvent);
					if (playerToggleCrawlEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setCrawling(true);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputFlags.STOP_CRAWLING)) != 0 && this.isCrawling()) {
					PlayerToggleCrawlEvent playerToggleCrawlEvent = new PlayerToggleCrawlEvent(this, false);
					this.server.getPluginManager().callEvent(playerToggleCrawlEvent);
					if (playerToggleCrawlEvent.isCancelled()) {
						this.sendData(this);
					} else {
						this.setCrawling(false);
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputFlags.START_FLYING)) != 0 && !getAdventureSettings().get(Type.FLYING)) {
					if (!server.getAllowFlight() && !getAdventureSettings().get(Type.ALLOW_FLIGHT)) {
						kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server");
						break;
					}

					PlayerToggleFlightEvent playerToggleFlightEvent = new PlayerToggleFlightEvent(this, true);
					if (isSpectator()) {
						playerToggleFlightEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleFlightEvent);
					if (playerToggleFlightEvent.isCancelled()) {
						this.sendAbilities(this, this.getAdventureSettings());
					} else {
						this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputFlags.STOP_FLYING)) != 0 && getAdventureSettings().get(Type.FLYING)) {
					PlayerToggleFlightEvent playerToggleFlightEvent = new PlayerToggleFlightEvent(this, false);
					if (isSpectator()) {
						playerToggleFlightEvent.setCancelled();
					}
					this.server.getPluginManager().callEvent(playerToggleFlightEvent);
					if (playerToggleFlightEvent.isCancelled()) {
						this.sendAbilities(this, this.getAdventureSettings());
					} else {
						this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
					}
				}
				if ((inputFlags & (1L << PlayerAuthInputFlags.START_SPIN_ATTACK)) != 0) {
					if (isSpectator()) {
						setDataFlag(DATA_FLAG_SPIN_ATTACK, false);
					} else {
						Item trident = getInventory().getItemInHand();
						if (trident.is(Item.TRIDENT)) {
							int riptide = trident.getEnchantmentLevel(Enchantment.RIPTIDE);
							if (riptide > 0) {
								//TODO: check water/rain
								if (setDataFlag(DATA_FLAG_SPIN_ATTACK, true)) {
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
				}
				if ((inputFlags & (1L << PlayerAuthInputFlags.STOP_SPIN_ATTACK)) != 0) {
					damageNearbyMobsTick = 0;
					setDataFlag(DATA_FLAG_SPIN_ATTACK, false);
				}

				emoting = (inputFlags & (1L << PlayerAuthInputFlags.EMOTING)) != 0;

				if ((inputFlags & (1L << PlayerAuthInputFlags.HANDLED_TELEPORT)) != 0) {
					//TODO
				}

				if ((inputFlags & (1L << PlayerAuthInputFlags.MISSED_SWING)) != 0 && isServerAuthoritativeSoundEnabled() && !isSpectator()) {
					level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE, EntityFullNames.PLAYER);

					// touch bug: https://bugs.mojang.com/browse/MCPE-107865
					if (getLoginChainData().getCurrentInputMode() == ClientChainData.INPUT_TOUCH) {
						AnimatePacket pk = new AnimatePacket();
						pk.eid = getId();
						pk.action = Action.SWING_ARM;
						dataPacket(pk);

						AnimatePacket pkBroadcast = new AnimatePacket();
						pkBroadcast.eid = getId();
						pkBroadcast.action = Action.SWING_ARM;
						Server.broadcastPacket(getViewers().values(), pkBroadcast);
					}
				}

				if (isRiding()) {
					if (riding instanceof EntityAbstractHorse horse) {
						horse.updatePlayerJump((inputFlags & (1L << PlayerAuthInputFlags.JUMPING)) != 0);
					} else if (riding instanceof EntityCamel camel && camel.isControlling(this)) {
						camel.updatePlayerJump(this, (inputFlags & (1L << PlayerAuthInputFlags.JUMPING)) != 0);
					}
				}

				boolean predictedInVehicle = (inputFlags & (1L << PlayerAuthInputFlags.IN_CLIENT_PREDICTED_IN_VEHICLE)) != 0;
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

						this.setRotation(yaw, pitch);
						this.newPosition = newPos;
						this.forceMovement = null;
					}
				}

				PlayerBlockAction[] blockActions = playerAuthInputPacket.getBlockActions();
				if (blockActions != null && blockActions.length != 0) {
					for (PlayerBlockAction blockAction : blockActions) {
						Vector3 pos = new Vector3(blockAction.x, blockAction.y, blockAction.z);
						BlockFace face = null;
						boolean distanceChecked = false;

						actionswitch:
						switch (blockAction.action) {
							case PlayerActionPacket14.ACTION_BLOCK_CONTINUE_DESTROY: // server
								if (this.isBreakingBlock() && pos.distanceSquared(this) < 100) {
									if (pos.equals(this.breakingBlock)) {
										this.breakingBlockFace = BlockFace.fromIndex(blockAction.data);
										break;
									}
									distanceChecked = true;
									this.level.addLevelEvent(this.breakingBlock, LevelEventPacket.EVENT_BLOCK_STOP_BREAK);
									this.lastBreak = -1;
									this.breakingBlock = null;
									this.breakingBlockFace = null;
								}
							case PlayerActionPacket14.ACTION_START_BREAK: // both
								if (!this.spawned || !this.isAlive() || this.isSpectator() || this.lastBreak != -1 || !distanceChecked && pos.distanceSquared(this) > 100) {
									break;
								}
								face = BlockFace.fromIndex(blockAction.data);
								Block target = this.level.getBlock(pos, false);
								PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == Block.AIR ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
								this.getServer().getPluginManager().callEvent(playerInteractEvent);
								if (playerInteractEvent.isCancelled()) {
									this.inventory.sendHeldItem(this);
									break;
								}
								if (/*!isAdventure()*/true) {
									switch (target.getId()) {
										case Block.NOTEBLOCK:
											((BlockNoteblock) target).emitSound();
											break actionswitch;
										case Block.DRAGON_EGG:
											if (!this.isCreative()) {
												((BlockDragonEgg) target).teleport();
												break actionswitch;
											}
											break;
										case Block.BLOCK_FRAME:
										case Block.BLOCK_GLOW_FRAME:
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
								if (this.breakingBlock == null || this.breakingBlockFace == null) {
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
								this.resetCraftingGridType();

								item = this.getInventory().getItemInHand();
								Item oldItem = item.clone();
								if (this.canInteract(pos.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL) && (item = this.level.useBreakOn(pos, face, item, this, true)) != null) {
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
								this.lastBreak = -1;
								this.breakingBlock = null;
								this.breakingBlockFace = null;
								break;
							case PlayerActionPacket14.ACTION_CONTINUE_BREAK: // client
								if (this.isBreakingBlock() && pos.distanceSquared(this) < 100) {
									this.breakingBlockFace = BlockFace.fromIndex(blockAction.data);
									block = this.level.getBlock(pos, false);
									this.level.addParticle(new PunchBlockParticle(pos, block, this.breakingBlockFace));
								}
								break;
						}
					}

					this.setUsingItem(false);
				}

				NetworkInventoryAction[] inventoryActions = playerAuthInputPacket.getInventoryActions();
				if (inventoryActions != null && inventoryActions.length != 0) {
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

					if (resolve) {
						// 正常情况只有UseItemTransaction的action, 不会触发crafting类事务
						boolean slotChangeTransaction = true;
/*
						boolean incomplete = true;

						if (playerAuthInputPacket.isCraftingPart()) {
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
									incomplete = false;
								}

								if ((craftingType >> 3) == 0 || craftingType == CRAFTING_STONECUTTER) {
									incomplete = false;
								}
							} catch (Exception e) {
								this.getServer().getLogger().logException(e);
								this.craftingTransaction = null;
								this.getUIInventory().sendContents(this);
							}
						} else if (playerAuthInputPacket.isEnchantingPart()) {
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

							incomplete = false;
						} else if (playerAuthInputPacket.isRepairItemPart()) {
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
								incomplete = false;
							}

							if ((this.craftingType >> 3) != 2) {
								incomplete = false;
							}
						}

						if (incomplete) {
							if (this.craftingTransaction != null) {
								if (CraftingTransaction.checkForCraftingPart(actions)) {
									for (InventoryAction action : actions) {
										craftingTransaction.addAction(action);
									}

									slotChangeTransaction = false;
								}
//					            else {
//						            this.server.getLogger().debug("Got unexpected normal inventory action with incomplete crafting transaction (PlayerAuthInputPacket) from " + this.getName() + ", refusing to execute crafting");
//						            this.removeAllWindows(false);
//						            this.sendAllInventories();
//						            this.craftingTransaction = null;
//					            }
							} else if (this.enchantTransaction != null) {
								if (enchantTransaction.checkForEnchantPart(actions)) {
									for (InventoryAction action : actions) {
										enchantTransaction.addAction(action);
									}

									slotChangeTransaction = false;
								} else {
									this.server.getLogger().debug("Got unexpected normal inventory action with incomplete enchanting transaction (PlayerAuthInputPacket) from " + this.getName() + ", refusing to execute enchant " + playerAuthInputPacket);
									this.removeAllWindows(false);
									this.sendAllInventories();
									this.enchantTransaction = null;
								}
							} else if (this.repairItemTransaction != null) {
								if (RepairItemTransaction.checkForRepairItemPart(actions)) {
									for (InventoryAction action : actions) {
										this.repairItemTransaction.addAction(action);
									}

									slotChangeTransaction = false;
								} else {
									this.server.getLogger().debug("Got unexpected normal inventory action with incomplete repair item transaction (PlayerAuthInputPacket) from " + this.getName() + ", refusing to execute repair item " + playerAuthInputPacket);
									this.removeAllWindows(false);
									this.sendAllInventories();
									this.repairItemTransaction = null;
								}
							}
						}
*/
						if (slotChangeTransaction) {
//							InventoryTransaction transaction = new InventoryTransaction(this, actions);
//							if (!transaction.execute()) {
//								this.server.getLogger().debug("Failed to execute inventory transaction (PlayerAuthInputPacket) from " + this.getName() + " with actions: " + Arrays.toString(inventoryActions));
//							}
						}
					}
				}

				UseItemData useItemData = playerAuthInputPacket.getUseItemData();
				if (useItemData != null) {
					Vector3f clickPos = useItemData.clickPos;
					BlockVector3 blockVector = useItemData.blockPos;
					BlockFace face = useItemData.face;
					int type = useItemData.actionType;

					if (face != null || type == InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR) {
						switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:
								// Remove if client bug is ever fixed
								boolean spamBug = lastRightClickData != null && System.currentTimeMillis() - lastRightClickTime < 100.0 &&
										lastRightClickData.playerPos.distanceSquared(useItemData.playerPos) < 0.00001 &&
										lastRightClickData.blockPos.equalsVec(blockVector) &&
										lastRightClickData.clickPos.distanceSquared(clickPos) < 0.00001;
								lastRightClickData = useItemData;
								lastRightClickTime = System.currentTimeMillis();
								if (spamBug /*&& !(useItemData.itemInHand instanceof ItemBlock)*/) {
									break;
								}

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
									if (this.isCreative()) {
										Item i = inventory.getItemInHand();
										if (this.level.useItemOn(blockVector.asVector3(), i, face, clickPos.x, clickPos.y, clickPos.z, this) != null) {
											break;
										}
									} else if (inventory.getItemInHand().equals(useItemData.itemInHand)) {
										Item i = inventory.getItemInHand();
										Item oldItem = i.clone();
										//TODO: Implement adventure mode checks
										if ((i = this.level.useItemOn(blockVector.asVector3(), i, face, clickPos.x, clickPos.y, clickPos.z, this)) != null) {
											if (i.getId() != 10000) {  // Hack
												if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
													inventory.setItemInHand(i);
													inventory.sendHeldItem(this.getViewers().values());
												}
												break;
											}
										}
									}
								}

								inventory.sendHeldItem(this);

								if (blockVector.distanceSquared(this) > 10000) {
									break;
								}

								Block target = this.level.getBlock(blockVector.asVector3());
								block = target.getSide(face);

								this.level.sendBlocks(new Player[]{this}, new Block[]{target, block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

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

								Item i = this.getInventory().getItemInHand();
								Item oldItem = i.clone();

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)
										&& (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
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

									BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(blockVector);
									if (blockEntity instanceof BlockEntitySpawnable) {
										((BlockEntitySpawnable) blockEntity).spawnTo(this);
									}
								}

								break;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR:
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

								PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
								this.server.getPluginManager().callEvent(interactEvent);
								if (interactEvent.isCancelled()) {
									this.inventory.sendHeldItem(this);
									break;
								}

								if (item.onClickAir(this, directionVector)) {
									if (this.isSurvivalLike()) {
										this.inventory.setItemInHand(item);
									}

									if (!this.isUsingItem()) {
										this.setUsingItem(item.canRelease());
										break;
									}

									// Used item
									//int ticksUsed = this.server.getTick() - this.startAction;
									int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

									this.setUsingItem(false);

									if (!item.onUse(this, ticksUsed)) {
										this.inventory.sendContents(this);
									}
								}

								break;
						}
					}
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

							float left = 0;
							float right = 0;
							if ((inputFlags & (1L << PlayerAuthInputFlags.UP)) != 0) {
								left = 0.04f;
								right = 0.04f;
								if ((inputFlags & ((1L << PlayerAuthInputFlags.PADDLE_LEFT) | 1L << PlayerAuthInputFlags.PADDLE_RIGHT)) != ((1L << PlayerAuthInputFlags.PADDLE_LEFT) | 1L << PlayerAuthInputFlags.PADDLE_RIGHT)) {
									if ((inputFlags & (1L << PlayerAuthInputFlags.PADDLE_LEFT)) != 0) {
										left = 0.05f;
										right = 0.02f;
									} else if ((inputFlags & (1L << PlayerAuthInputFlags.PADDLE_RIGHT)) != 0) {
										left = 0.02f;
										right = 0.05f;
									}
								}
							} else if ((inputFlags & (1L << PlayerAuthInputFlags.DOWN)) != 0) {
								left = -0.004f;
								right = -0.004f;
								if ((inputFlags & ((1L << PlayerAuthInputFlags.PADDLE_LEFT) | 1L << PlayerAuthInputFlags.PADDLE_RIGHT)) != ((1L << PlayerAuthInputFlags.PADDLE_LEFT) | 1L << PlayerAuthInputFlags.PADDLE_RIGHT)) {
									if ((inputFlags & (1L << PlayerAuthInputFlags.PADDLE_LEFT)) != 0) {
										left = -0.005f;
										right = -0.002f;
									} else if ((inputFlags & (1L << PlayerAuthInputFlags.PADDLE_RIGHT)) != 0) {
										left = -0.002f;
										right = -0.005f;
									}
								}
							} else if ((inputFlags & ((1L << PlayerAuthInputFlags.PADDLE_LEFT) | 1L << PlayerAuthInputFlags.PADDLE_RIGHT)) != ((1L << PlayerAuthInputFlags.PADDLE_LEFT) | 1L << PlayerAuthInputFlags.PADDLE_RIGHT)) {
								if ((inputFlags & (1L << PlayerAuthInputFlags.PADDLE_LEFT)) != 0) {
									left = 0.04f;
								} else if ((inputFlags & (1L << PlayerAuthInputFlags.PADDLE_RIGHT)) != 0) {
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
				} else if (this.riding != null && (moveVecX != 0 || moveVecY != 0) && riding.isControlling(this)) {
					moveVecX = Mth.clamp(moveVecX, -1, 1);
					moveVecY = Mth.clamp(moveVecY, -1, 1);

					if (this.riding instanceof EntityRideable rideable && !(this.riding instanceof EntityBoat)) {
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
			default:
				super.handleDataPacket(packet);
				break;
		}

	}

	@Override
	public int addWindow(Inventory inventory, Integer forceId, boolean isPermanent, boolean alwaysOpen) {
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
		inventory.close(this);
		if (!this.permanentWindows.contains(this.getWindowId(inventory)))
			this.windows.remove(inventory);
	}

	@Override
	public void onInventoryOpen() {
		this.openInventory();
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
		ContainerOpenPacket pk = new ContainerOpenPacket();
		pk.windowId = this.getWindowId(this.inventory);
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
	public boolean onUpdate(int currentTick) {
		int tickDiff = currentTick - this.lastUpdate;
		if (tickDiff > 0) {
			if (this.serverAuthoritativeBlockBreaking && this.breakingBlockFace != null && this.isBreakingBlock() && this.spawned && this.isAlive()) {
				this.level.addParticle(new PunchBlockParticle(this.breakingBlock, this.breakingBlock, this.breakingBlockFace));
			}

			this.emotedCurrentTick = false;
			this.currentTickAttackPacketCount = 0;

			if (clientTickCheckCd > 0) {
				clientTickCheckCd--;
			}
			if ((currentTick & INPUT_TICK_DESYNC_FORGET_CD) == 0 && clientTickTooFastCount > 0) {
				clientTickTooFastCount--;
			}
		}
		return super.onUpdate(currentTick);
	}

	@Override
	protected void onClientTickUpdated(long tick) {
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
}
