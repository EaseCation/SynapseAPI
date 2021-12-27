package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockDragonEgg;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityMinecartAbstract;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.utils.Binary;
import org.itxtech.synapseapi.event.player.SynapsePlayerInputModeChangeEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket.PlayerBlockAction;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SynapsePlayer116 extends SynapsePlayer113 {

	protected boolean inventoryOpen;

	/**
	 * Server Authoritative Movement is required.
	 */
	protected boolean serverAuthoritativeBlockBreaking;
	protected BlockFace breakingBlockFace;

	public SynapsePlayer116(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = false;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket116 startGamePacket = new StartGamePacket116();
		startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
		startGamePacket.netease = this.isNetEaseClient();
		startGamePacket.entityUniqueId = Long.MAX_VALUE;
		startGamePacket.entityRuntimeId = Long.MAX_VALUE;
		startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.x = (float) this.x;
		startGamePacket.y = (float) this.y;
		startGamePacket.z = (float) this.z;
		startGamePacket.yaw = (float) this.yaw;
		startGamePacket.pitch = (float) this.pitch;
		startGamePacket.seed = -1;
		startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
		startGamePacket.isMovementServerAuthoritative = this.isNetEaseClient;
		startGamePacket.currentTick = this.server.getTick();
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
				InteractPacket interactPacket = (InteractPacket) packet;
				if (interactPacket.action == InteractPacket.ACTION_OPEN_INVENTORY && interactPacket.target == Long.MAX_VALUE && !this.inventoryOpen) {
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

				if (this.windowIndex.containsKey(containerClosePacket.windowId)) {
					this.server.getPluginManager().callEvent(new InventoryCloseEvent(this.windowIndex.get(containerClosePacket.windowId), this));

					if (containerClosePacket.windowId == ContainerIds.INVENTORY) this.inventoryOpen = false;

					if (this instanceof SynapsePlayer116100) ((SynapsePlayer116100) this).removeWindow(this.windowIndex.get(containerClosePacket.windowId), true);
					else this.removeWindow(this.windowIndex.get(containerClosePacket.windowId));
				} else {
					this.getServer().getLogger().debug("Unopened window: " + containerClosePacket.windowId);
				}

				if (containerClosePacket.windowId == -1) {
					this.craftingType = CRAFTING_SMALL;
					this.resetCraftingGridType();
					this.addWindow(this.craftingGrid, ContainerIds.NONE);

					ContainerClosePacket pk = new ContainerClosePacket();
					pk.windowId = -1;
					this.dataPacket(pk);
				}
				break;
			case ProtocolInfo.INVENTORY_TRANSACTION_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				InventoryTransactionPacket116 transactionPacket = (InventoryTransactionPacket116) packet;

				Item item;
				Block block;

				List<InventoryAction> actions = new ArrayList<>();
				for (NetworkInventoryAction networkInventoryAction : transactionPacket.actions) {
					InventoryAction a = networkInventoryAction.createInventoryAction(this);

					if (a == null) {
						this.getServer().getLogger().debug("Unmatched inventory action from " + this.getName() + ": " + networkInventoryAction);
						this.sendAllInventories();
						break packetswitch;
					}

					actions.add(a);
				}

				if (transactionPacket.isCraftingPart) {
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
					}

					return;
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
					}
					return;
				} else if (this.craftingTransaction != null) {
					if (craftingTransaction.checkForCraftingPart(actions)) {
						for (InventoryAction action : actions) {
							craftingTransaction.addAction(action);
						}
						return;
					} else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete crafting transaction from " + this.getName() + ", refusing to execute crafting");
						this.removeAllWindows(false);
						this.sendAllInventories();
						this.craftingTransaction = null;
					}
				} else if (this.enchantTransaction != null) {
					if (enchantTransaction.checkForEnchantPart(actions)) {
						for (InventoryAction action : actions) {
							enchantTransaction.addAction(action);
						}
						return;
					} else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete enchanting transaction from " + this.getName() + ", refusing to execute enchant " + transactionPacket.toString());
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
					} else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete repair item transaction from " + this.getName() + ", refusing to execute repair item " + transactionPacket.toString());
						this.removeAllWindows(false);
						this.sendAllInventories();
						this.repairItemTransaction = null;
					}
				}
				switch (transactionPacket.transactionType) {
					case InventoryTransactionPacket116.TYPE_NORMAL:
						InventoryTransaction transaction = new InventoryTransaction(this, actions);

						if (!transaction.execute()) {
							this.server.getLogger().debug("Failed to execute inventory transaction from " + this.getName() + " with actions: " + Arrays.toString(transactionPacket.actions));
							break packetswitch; //oops!
						}

						//TODO: fix achievement for getting iron from furnace

						break packetswitch;
					case InventoryTransactionPacket116.TYPE_MISMATCH:
						if (transactionPacket.actions.length > 0) {
							this.server.getLogger().debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.length + ", " + Arrays.toString(transactionPacket.actions));
						}
						this.sendAllInventories();

						break packetswitch;
					case InventoryTransactionPacket116.TYPE_USE_ITEM:
						UseItemData useItemData = (UseItemData) transactionPacket.transactionData;

						BlockVector3 blockVector = useItemData.blockPos;
						BlockFace face = useItemData.face;

						int type = useItemData.actionType;
						switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:
								// Remove if client bug is ever fixed
								boolean spamBug = (lastRightClickPos != null && System.currentTimeMillis() - lastRightClickTime < 100.0 && blockVector.distanceSquared(lastRightClickPos) < 0.00001);
								lastRightClickPos = blockVector.asVector3();
								lastRightClickTime = System.currentTimeMillis();
								if (spamBug && !(this.getInventory().getItemInHand() instanceof ItemBlock)) {
									return;
								}

								this.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, false);

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? 13 : 7)) {
									if (this.isCreative()) {
										Item i = inventory.getItemInHand();
										if (this.level.useItemOn(blockVector.asVector3(), i, face, useItemData.clickPos.x, useItemData.clickPos.y, useItemData.clickPos.z, this) != null) {
											break packetswitch;
										}
									} else if (inventory.getItemInHand().equals(useItemData.itemInHand)) {
										Item i = inventory.getItemInHand();
										Item oldItem = i.clone();
										//TODO: Implement adventure mode checks
										if ((i = this.level.useItemOn(blockVector.asVector3(), i, face, useItemData.clickPos.x, useItemData.clickPos.y, useItemData.clickPos.z, this)) != null) {
											if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
												inventory.setItemInHand(i);
												inventory.sendHeldItem(this.getViewers().values());
											}
											break packetswitch;
										}
									}
								}

								inventory.sendHeldItem(this);

								if (blockVector.distanceSquared(this) > 10000) {
									break packetswitch;
								}

								Block target = this.level.getBlock(blockVector.asVector3());
								block = target.getSide(face);

								this.level.sendBlocks(new Player[]{this}, new Block[]{target, block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

								if (target instanceof BlockDoor) {
									BlockDoor door = (BlockDoor) target;

									Block part;

									if ((door.getDamage() & 0x08) > 0) { //up
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

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? 16 : 8) && (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
									if (this.isSurvival()) {
										this.getFoodData().updateFoodExpLevel(0.025);
										if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
											inventory.setItemInHand(i);
											inventory.sendHeldItem(this.getViewers().values());
										}
									}
									break packetswitch;
								}

								inventory.sendContents(this);
								target = this.level.getBlock(blockVector.asVector3());
								BlockEntity blockEntity = this.level.getBlockEntity(blockVector.asVector3());

								this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

								inventory.sendHeldItem(this);

								if (blockEntity instanceof BlockEntitySpawnable) {
									((BlockEntitySpawnable) blockEntity).spawnTo(this);
								}

								break packetswitch;
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR:
								Vector3 directionVector = this.getDirectionVector();

								if (this.isCreative()) {
									item = this.inventory.getItemInHand();
								} else if (!this.inventory.getItemInHand().equals(useItemData.itemInHand)) {
									this.inventory.sendHeldItem(this);
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
									if (this.isSurvival()) {
										this.inventory.setItemInHand(item);
									}

									if (!this.isUsingItem()) {
										this.setUsingItem(true);
										break packetswitch;
									}

									// Used item
									int ticksUsed = this.server.getTick() - this.startAction;
									this.setUsingItem(false);

									if (!item.onUse(this, ticksUsed)) {
										this.inventory.sendContents(this);
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
								PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(this, target, item, useItemOnEntityData.clickPos);
								if (this.isSpectator()) playerInteractEntityEvent.setCancelled();
								getServer().getPluginManager().callEvent(playerInteractEntityEvent);

								if (playerInteractEntityEvent.isCancelled()) {
									break;
								}
								if (target.onInteract(this, item, useItemOnEntityData.clickPos) && this.isSurvival()) {
									if (item.isTool()) {
										if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
											item = new ItemBlock(new BlockAir());
										}
									} else {
										if (item.count > 1) {
											item.count--;
										} else {
											item = new ItemBlock(new BlockAir());
										}
									}

									this.inventory.setItemInHand(item);
								}
								break;
							case InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK:
								float itemDamage = item.getAttackDamage();

								for (Enchantment enchantment : item.getEnchantments()) {
									itemDamage += enchantment.getDamageBonus(target);
								}

								Map<EntityDamageEvent.DamageModifier, Float> damage = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
								damage.put(EntityDamageEvent.DamageModifier.BASE, itemDamage);

								if (!this.canInteract(target, isCreative() ? 8 : 5)) {
									break;
								} else if (target instanceof Player) {
									if ((((Player) target).getGamemode() & 0x01) > 0) {
										break;
									} else if (!this.server.getPropertyBoolean("pvp") || this.server.getDifficulty() == 0) {
										break;
									}
								}

								EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(this, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage);
								if (this.isSpectator()) entityDamageByEntityEvent.setCancelled();
								if (!target.attack(entityDamageByEntityEvent)) {
									if (item.isTool() && this.isSurvival()) {
										this.inventory.sendContents(this);
									}
									break;
								}

								for (Enchantment enchantment : item.getEnchantments()) {
									enchantment.doPostAttack(this, target);
								}

								if (item.isTool() && this.isSurvival()) {
									if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
										this.inventory.setItemInHand(new ItemBlock(new BlockAir()));
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

										int ticksUsed = this.server.getTick() - this.startAction;
										if (!item.onRelease(this, ticksUsed)) {
											this.inventory.sendContents(this);
										}

										this.setUsingItem(false);
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
			case ProtocolInfo.ENTITY_EVENT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
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
				this.getServer().getLogger().warning("packetId=0x" + Binary.bytesToHexString(new byte[]{(byte) packetViolationWarningPacket.packetId}) + " (" + packetViolationWarningPacket.packetId + ")");
				this.getServer().getLogger().warning("context=" + packetViolationWarningPacket.context);
				break;
			case ProtocolInfo.EMOTE_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (!this.spawned) {
					break;
				}
				EmotePacket116 emotePacket = (EmotePacket116) packet;
				if (emotePacket.runtimeId != this.id) {
					server.getLogger().warning(this.username + " sent EmotePacket with invalid entity id: " + emotePacket.runtimeId + " != " + this.id);
					break;
				}
				for (Player viewer : this.getViewers().values().stream().filter(p -> p.getProtocol() >= AbstractProtocol.PROTOCOL_116.getProtocolStart()).toArray(Player[]::new)) {
					viewer.dataPacket(emotePacket);
				}
				break;
			case ProtocolInfo.PLAYER_AUTH_INPUT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (this.teleportPosition != null) {
					break;
				}

				Vector3 newPos;
				boolean revert;

				IPlayerAuthInputPacket playerAuthInputPacket = (IPlayerAuthInputPacket) packet;

				long inputFlags = playerAuthInputPacket.getInputFlags();
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_SPRINTING)) != 0 && !this.isSprinting()) {
					PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, true);
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
				}
				if ((inputFlags & (1L << PlayerAuthInputPacket116.FLAG_START_GLIDING)) != 0 && !this.isGliding()) {
					PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
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

				PlayerBlockAction[] blockActions = playerAuthInputPacket.getBlockActions();
				if (blockActions != null && blockActions.length != 0) {
					for (PlayerBlockAction blockAction : blockActions) {
						Vector3 pos = new Vector3(blockAction.x, blockAction.y, blockAction.z);
						BlockFace face = null;

						actionswitch:
						switch (blockAction.action) {
							case PlayerActionPacket14.ACTION_BLOCK_CONTINUE_DESTROY: // server
								if (this.isBreakingBlock()) {
									if (pos.equals(this.breakingBlock)) {
										this.breakingBlockFace = BlockFace.fromIndex(blockAction.data);
										break;
									}
									this.level.addLevelEvent(this.breakingBlock, LevelEventPacket.EVENT_BLOCK_STOP_BREAK);
									this.lastBreak = Long.MAX_VALUE;
									this.breakingBlock = null;
									this.breakingBlockFace = null;
								}
							case PlayerActionPacket14.ACTION_START_BREAK: // both
								if (!this.spawned || !this.isAlive() || this.isSpectator() || this.lastBreak != Long.MAX_VALUE || pos.distanceSquared(this) > 100) {
									break;
								}
								face = BlockFace.fromIndex(blockAction.data);
								Block target = this.level.getBlock(pos);
								PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == Block.AIR ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
								this.getServer().getPluginManager().callEvent(playerInteractEvent);
								if (playerInteractEvent.isCancelled()) {
									this.inventory.sendHeldItem(this);
									break;
								}
								switch (target.getId()) {
									case Block.NOTEBLOCK:
										((BlockNoteblock) target).emitSound();
										break actionswitch;
									case Block.DRAGON_EGG:
										if (!this.isCreative()) {
											((BlockDragonEgg) target).teleport();
											break actionswitch;
										}
									case Block.ITEM_FRAME_BLOCK:
										BlockEntity itemFrame = this.level.getBlockEntity(pos);
										if (itemFrame instanceof BlockEntityItemFrame && ((BlockEntityItemFrame) itemFrame).dropItem(this)) {
											break actionswitch;
										}
								}
								block = target.getSide(face);
								if (block.getId() == Block.FIRE) {
									this.level.setBlock(block, Block.get(Block.AIR), true);
									this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
									break;
								}
								if (!this.isCreative()) {
									//improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
									//Done by lmlstarqaq
									double breakTime = Math.ceil(target.getBreakTime(this.inventory.getItemInHand(), this) * 20);
									if (breakTime > 0) {
										this.level.addLevelEvent(pos, LevelEventPacket.EVENT_BLOCK_START_BREAK, (int) (65535 / breakTime));
									}
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
								if (!this.spawned || !this.isAlive()) {
									break;
								}
								if (face == null) {
									face = BlockFace.fromIndex(blockAction.data);
								}
								this.resetCraftingGridType();

								item = this.getInventory().getItemInHand();
								Item oldItem = item.clone();
								if (this.canInteract(pos.add(0.5, 0.5, 0.5), this.isCreative() ? 16 : 8) && (item = this.level.useBreakOn(pos, face, item, this, true)) != null) {
									// success
									if (this.isSurvival()) {
										this.getFoodData().updateFoodExpLevel(0.025);
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

									BlockEntity blockEntity = this.level.getBlockEntity(pos);
									if (blockEntity instanceof BlockEntitySpawnable) {
										((BlockEntitySpawnable) blockEntity).spawnTo(this);
									}
								}
							case PlayerActionPacket14.ACTION_ABORT_BREAK: // both
								if (face == null) {
//									int breakTime = blockAction.data;
								}
								this.level.addLevelEvent(pos, LevelEventPacket.EVENT_BLOCK_STOP_BREAK);
								this.lastBreak = Long.MAX_VALUE;
								this.breakingBlock = null;
								this.breakingBlockFace = null;
								break;
							case PlayerActionPacket14.ACTION_CONTINUE_BREAK: // client
								if (this.isBreakingBlock()) {
									this.breakingBlockFace = BlockFace.fromIndex(blockAction.data);
									block = this.level.getBlock(pos);
									this.level.addParticle(new PunchBlockParticle(pos, block, this.breakingBlockFace));
								}
								break;
						}
					}

					this.startAction = -1;
					this.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, false);
				}

				newPos = new Vector3(playerAuthInputPacket.getX(), playerAuthInputPacket.getY() - this.getEyeHeight(), playerAuthInputPacket.getZ());
				double dis = newPos.distanceSquared(this);

				if (dis == 0 && playerAuthInputPacket.getYaw() % 360 == this.yaw && playerAuthInputPacket.getPitch() % 360 == this.pitch) {
					break;
				}

//				if (dis > 100) {
//					this.sendPosition(this, playerAuthInputPacket.getYaw(), playerAuthInputPacket.getPitch(), MovePlayerPacket.MODE_RESET);
//					break;
//				}

				revert = false;
				if (!this.isAlive() || !this.spawned) {
					revert = true;
					this.forceMovement = new Vector3(this.x, this.y, this.z);
				}

				if (this.forceMovement != null && (newPos.distanceSquared(this.forceMovement) > 0.1 || revert)) {
					this.sendPosition(this.forceMovement, this.yaw, this.pitch, MovePlayerPacket.MODE_RESET);
				} else {
					playerAuthInputPacket.setYaw(playerAuthInputPacket.getYaw() % 360);
					playerAuthInputPacket.setPitch(playerAuthInputPacket.getPitch() % 360);

					if (playerAuthInputPacket.getYaw() < 0) {
						playerAuthInputPacket.setYaw(playerAuthInputPacket.getYaw() + 360);
					}

					this.setRotation(playerAuthInputPacket.getYaw(), playerAuthInputPacket.getPitch());
					this.newPosition = newPos;
					this.forceMovement = null;
				}

				if (this.riding != null) {
					if (this.riding instanceof EntityMinecartAbstract) {
						((EntityMinecartAbstract) this.riding).setCurrentSpeed(playerAuthInputPacket.getMoveVecZ());
					} else if (this.riding instanceof EntityRideable && !(this.riding instanceof EntityBoat)) {
						((EntityRideable) riding).onPlayerInput(this, playerAuthInputPacket.getMoveVecX(), playerAuthInputPacket.getMoveVecZ());
						Vector3f offset = riding.getMountedOffset(this);
						((EntityRideable) riding).onPlayerRiding(this.temporalVector.setComponents(playerAuthInputPacket.getX() - offset.x, playerAuthInputPacket.getY() - offset.y, playerAuthInputPacket.getZ() - offset.z), (playerAuthInputPacket.getHeadYaw() + 90) % 360, 0);
					}
				}

				if (this.getLoginChainData().getCurrentInputMode() != playerAuthInputPacket.getInputMode()) {
					int from = this.getLoginChainData().getCurrentInputMode();
					this.getLoginChainData().setCurrentInputMode(playerAuthInputPacket.getInputMode());
					this.getServer().getPluginManager().callEvent(new SynapsePlayerInputModeChangeEvent(this, from, playerAuthInputPacket.getInputMode()));
				}

				break;
			default:
				super.handleDataPacket(packet);
				break;
		}

	}

	@Override
	public int addWindow(Inventory inventory, Integer forceId, boolean isPermanent, boolean alwaysOpen) {
		if (this.windows.containsKey(inventory)) {
			return this.windows.get(inventory);
		}
		int cnt;
		if (forceId == null) {
			this.windowCnt = cnt = Math.max(4, ++this.windowCnt % 99);
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
		CreativeContentPacket116 pk = new CreativeContentPacket116();
		pk.setHelper(AbstractProtocol.fromRealProtocol(this.protocol).getHelper());
		pk.neteaseMode = this.isNetEaseClient;
		if (!this.isSpectator()) { //fill it for all gamemodes except spectator
			pk.entries = this.getCreativeItems().toArray(new Item[0]);
		}
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
		this.dataPacket(pk);
	}

	@Override
	public boolean onUpdate(int currentTick) {
		int tickDiff = currentTick - this.lastUpdate;
		if (tickDiff > 0) {
			this.updateSynapsePlayerTiming.startTiming();
			if (this.serverAuthoritativeBlockBreaking && this.breakingBlockFace != null && this.isBreakingBlock() && this.spawned && this.isAlive()) {
				this.level.addParticle(new PunchBlockParticle(this.breakingBlock, this.breakingBlock, this.breakingBlockFace));
			}
			this.updateSynapsePlayerTiming.stopTiming();
		}
		return super.onUpdate(currentTick);
	}
}
