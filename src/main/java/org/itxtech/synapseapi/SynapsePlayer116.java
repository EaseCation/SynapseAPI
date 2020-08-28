package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.utils.Binary;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.CreativeContentPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.PacketViolationWarningPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.StartGamePacket116;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SynapsePlayer116 extends SynapsePlayer113 {

	protected boolean inventoryOpen;

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
				if (interactPacket.action == InteractPacket.ACTION_OPEN_INVENTORY) { //TODO: WIP
					//if (/*interactPacket.target == this.getId() &&*/ !this.inventoryOpen) {
//						this.openInventory();
						this.inventory.open(this);
						this.inventoryOpen = true;
					//}
				}
				super.handleDataPacket(packet);
				break;
			case ProtocolInfo.CONTAINER_CLOSE_PACKET: //TODO: WIP
				ContainerClosePacket containerClosePacket = (ContainerClosePacket) packet;
				if (!this.spawned /*|| containerClosePacket.windowId == ContainerIds.INVENTORY && !inventoryOpen*/) {
					break;
				}

				if (this.windowIndex.containsKey(containerClosePacket.windowId)) {
					this.server.getPluginManager().callEvent(new InventoryCloseEvent(this.windowIndex.get(containerClosePacket.windowId), this));

					if (containerClosePacket.windowId == ContainerIds.INVENTORY) this.inventoryOpen = false;

					this.removeWindow(this.windowIndex.get(containerClosePacket.windowId));
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

					if (this.craftingTransaction.getPrimaryOutput() != null) {
						//we get the actions for this in several packets, so we can't execute it until we get the result

						this.craftingTransaction.execute();
						this.craftingTransaction = null;
					}

					return;
				} else if (this.craftingTransaction != null) {
					this.server.getLogger().debug("Got unexpected normal inventory action with incomplete crafting transaction from " + this.getName() + ", refusing to execute crafting");
					this.craftingTransaction = null;
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
						BlockFace face = face = useItemData.face;

						int type = useItemData.actionType;
						switch (type) {
							case InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK:
								// Remove if client bug is ever fixed
								boolean spamBug = (lastRightClickPos != null && System.currentTimeMillis() - lastRightClickTime < 100.0 && blockVector.distanceSquared(lastRightClickPos) < 0.00001);
								lastRightClickPos = blockVector.asVector3();
								lastRightClickTime = System.currentTimeMillis();
								if (spamBug) {
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

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? 13 : 7) && (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
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
			case ProtocolInfo.PACKET_VIOLATION_WARNING_PACKET:
				PacketViolationWarningPacket116 packetViolationWarningPacket = (PacketViolationWarningPacket116) packet;
				this.getServer().getLogger().warning("Received PacketViolationWarningPacket from " + this.getName());
				this.getServer().getLogger().warning("type=" + packetViolationWarningPacket.type.name());
				this.getServer().getLogger().warning("severity=" + packetViolationWarningPacket.severity.name());
				this.getServer().getLogger().warning("packetId=" + Binary.bytesToHexString(new byte[]{(byte) packetViolationWarningPacket.packetId}));
				this.getServer().getLogger().warning("context=" + packetViolationWarningPacket.context);
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}

	}

	@Override
	protected void doFirstSpawn() {
		super.doFirstSpawn();
		this.sendCreativeContents();
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
		this.windows.put(inventory, cnt);

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

	public void sendCreativeContents() {
		CreativeContentPacket116 pk = new CreativeContentPacket116();
		if (!this.isSpectator()) { //fill it for all gamemodes except spectator
			pk.entries = Item.getCreativeItems().toArray(new Item[0]);
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
		this.dataPacket(pk);
	}
}
