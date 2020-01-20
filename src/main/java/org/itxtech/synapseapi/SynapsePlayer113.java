package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
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
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.resourcepacks.ResourcePack;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.RespawnPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.StartGamePacket113;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

import java.net.InetSocketAddress;
import java.util.*;

public class SynapsePlayer113 extends SynapsePlayer112 {

	public SynapsePlayer113(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = false;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket113 startGamePacket = new StartGamePacket113();
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
			case ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET:
				ResourcePackClientResponsePacket16 responsePacket = (ResourcePackClientResponsePacket16) packet;
				switch (responsePacket.responseStatus) {
					case ResourcePackClientResponsePacket.STATUS_REFUSED:
						this.close("", "disconnectionScreen.noReason");
						break;
					case ResourcePackClientResponsePacket.STATUS_SEND_PACKS:
						for (ResourcePackClientResponsePacket16.Entry entry : responsePacket.packEntries) {
							ResourcePack resourcePack = this.resourcePacks.getOrDefault(entry.uuid, this.behaviourPacks.get(entry.uuid));
							if (resourcePack == null) {
								this.close("", "disconnectionScreen.resourcePack");
								break;
							}

							ResourcePackDataInfoPacket dataInfoPacket = new ResourcePackDataInfoPacket();
							dataInfoPacket.packId = resourcePack.getPackId();
							dataInfoPacket.maxChunkSize = 1048576; //megabyte
							dataInfoPacket.chunkCount = resourcePack.getPackSize() / dataInfoPacket.maxChunkSize;
							dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
							dataInfoPacket.sha256 = resourcePack.getSha256();
							this.dataPacket(dataInfoPacket);
						}
						break;
					case ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS:
						ResourcePackStackPacket113 stackPacket = new ResourcePackStackPacket113();
						stackPacket.mustAccept = this.forceResources;
						stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
						stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
						this.dataPacket(stackPacket);
						break;
					case ResourcePackClientResponsePacket.STATUS_COMPLETED:
						this.completeLoginSequence();
						break;
				}
				break;
			case ProtocolInfo.RESPAWN_PACKET:
				if (this.isAlive()) {
					break;
				}
				RespawnPacket113 respawnPacket = (RespawnPacket113) packet;
				if (respawnPacket.respawnState == RespawnPacket.STATE_CLIENT_READY_TO_SPAWN) {
					RespawnPacket113 respawn1 = new RespawnPacket113();
					respawn1.x = (float) this.getX();
					respawn1.y = (float) this.getY();
					respawn1.z = (float) this.getZ();
					respawn1.respawnState = RespawnPacket.STATE_READY_TO_SPAWN;
					this.dataPacket(respawn1);
				}
				break;
			case ProtocolInfo.PLAYER_ACTION_PACKET:
				PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;
				if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN && playerActionPacket.action != PlayerActionPacket14.ACTION_DIMENSION_CHANGE_REQUEST)) {
					break;
				}

				playerActionPacket.entityId = this.id;
				Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
				BlockFace face = BlockFace.fromIndex(playerActionPacket.face);

				switch (playerActionPacket.action) {
					case PlayerActionPacket14.ACTION_RESPAWN:
						if (!this.spawned || this.isAlive() || !this.isOnline()) {
							break;
						}

						if (this.server.isHardcore()) {
							this.setBanned(true);
							break;
						}

						this.craftingType = CRAFTING_SMALL;
						this.resetCraftingGridType();

						PlayerRespawnEvent playerRespawnEvent = new PlayerRespawnEvent(this, this.getSpawn());
						this.server.getPluginManager().callEvent(playerRespawnEvent);

						Position respawnPos = playerRespawnEvent.getRespawnPosition();

						this.teleport(respawnPos, null);

						this.setSprinting(false);
						this.setSneaking(false);

						this.setDataProperty(new ShortEntityData(Player.DATA_AIR, 400), false);
						this.deadTicks = 0;
						this.noDamageTicks = 60;

						this.removeAllEffects();
						this.setHealth(this.getMaxHealth());
						this.getFoodData().setLevel(20, 20);

						this.sendData(this);

						this.setMovementSpeed(DEFAULT_SPEED);

						this.getAdventureSettings().update();
						this.inventory.sendContents(this);
						this.inventory.sendArmorContents(this);

						this.spawnToAll();
						this.scheduleUpdate();

						this.startAction = -1;
						this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
						break;
					default:
						super.handleDataPacket(packet);
						break;
				}
				break;
			case ProtocolInfo.INVENTORY_TRANSACTION_PACKET:
				if (this.isSpectator()) {
					this.sendAllInventories();
					break;
				}

				InventoryTransactionPacket transactionPacket = (InventoryTransactionPacket) packet;

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
					case InventoryTransactionPacket.TYPE_NORMAL:
						InventoryTransaction transaction = new InventoryTransaction(this, actions);

						if (!transaction.execute()) {
							this.server.getLogger().debug("Failed to execute inventory transaction from " + this.getName() + " with actions: " + Arrays.toString(transactionPacket.actions));
							break packetswitch; //oops!
						}

						//TODO: fix achievement for getting iron from furnace

						break packetswitch;
					case InventoryTransactionPacket.TYPE_MISMATCH:
						if (transactionPacket.actions.length > 0) {
							this.server.getLogger().debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.length + ", " + Arrays.toString(transactionPacket.actions));
						}
						this.sendAllInventories();

						break packetswitch;
					case InventoryTransactionPacket.TYPE_USE_ITEM:
						UseItemData useItemData = (UseItemData) transactionPacket.transactionData;

						BlockVector3 blockVector = useItemData.blockPos;
						face = useItemData.face;

						int type = useItemData.actionType;
						switch (type) {
							case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK:
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
							case InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK:
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
							case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR:
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
					case InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY:
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
							case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT:
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
							case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK:
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
					case InventoryTransactionPacket.TYPE_RELEASE_ITEM:
						if (this.isSpectator()) {
							this.sendAllInventories();
							break packetswitch;
						}
						ReleaseItemData releaseItemData = (ReleaseItemData) transactionPacket.transactionData;

						try {
							type = releaseItemData.actionType;
							switch (type) {
								case InventoryTransactionPacket.RELEASE_ITEM_ACTION_RELEASE:
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
								case InventoryTransactionPacket.RELEASE_ITEM_ACTION_CONSUME:
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
			default:
				super.handleDataPacket(packet);
		}

	}

}
