package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.utils.TextFormat;
import co.aikar.timings.Timings;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.RespawnPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.SettingsCommandPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.StartGamePacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.TickSyncPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SynapsePlayer113 extends SynapsePlayer112 {

	public SynapsePlayer113(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		// this.levelChangeLoadScreen = false;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket113 startGamePacket = new StartGamePacket113();
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
			case ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
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
							if (resourcePack.getPackType().equals("resources")) {
								dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_RESOURCE;
							}
							else if (resourcePack.getPackType().equals("data")) {
								dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_BEHAVIOR;
							}
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
						if (this.preLoginEventTask.isFinished()) {
							this.completeLoginSequence();
						} else {
							this.shouldLogin = true;
						}
						break;
				}
				break;
			case ProtocolInfo.RESPAWN_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
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
				if (!this.callPacketReceiveEvent(((PlayerActionPacket14) packet).toDefault())) break;

				if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN /*&& playerActionPacket.action != PlayerActionPacket14.ACTION_DIMENSION_CHANGE_REQUEST*/)) {
					break;
				}

				playerActionPacket.entityId = this.id;
				BlockFace face;

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

						this.setDataProperty(new ShortEntityData(Player.DATA_AIR, 300), false);
						this.deadTicks = 0;
						this.noDamageTicks = 60;

						this.removeAllEffects();

						SetHealthPacket healthPacket = new SetHealthPacket();
						healthPacket.health = getMaxHealth();
						this.dataPacket(healthPacket);

						this.setHealth(this.getMaxHealth());
						this.getFoodData().setLevel(20, 20);

						this.sendData(this);
						this.sendData(this.getViewers().values().toArray(new Player[0]));

						this.setMovementSpeed(DEFAULT_SPEED);

						this.getAdventureSettings().update();
						this.inventory.sendContents(this);
						this.inventory.sendArmorContents(this);
						this.offhandInventory.sendContents(this);

						this.spawnToAll();
						this.scheduleUpdate();

						this.setUsingItem(false);
						break;
					default:
						super.handleDataPacket(packet);
						break;
				}
				break;
			case ProtocolInfo.INVENTORY_TRANSACTION_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				InventoryTransactionPacket transactionPacket = (InventoryTransactionPacket) packet;

				Item item;
				Block block;

				boolean skipped = false;
				List<InventoryAction> actions = new ArrayList<>(transactionPacket.actions.length);
				for (NetworkInventoryAction networkInventoryAction : transactionPacket.actions) {
					InventoryAction a = networkInventoryAction.createInventoryActionLegacy(this);

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
//						this.craftingTransaction = null;
//					}
				} else if (this.repairItemTransaction != null) {
					if (RepairItemTransaction.checkForRepairItemPart(actions)) {
						for (InventoryAction action : actions) {
							this.repairItemTransaction.addAction(action);
						}
						return;
					} else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete repair item transaction from " + this.getName() + ", refusing to execute repair item " + transactionPacket);
						this.repairItemTransaction = null;
					}
				}

				switch (transactionPacket.transactionType) {
					case InventoryTransactionPacket.TYPE_NORMAL:
						InventoryTransaction transaction = new InventoryTransaction(this, actions);

						if (!transaction.execute()) {
							this.server.getLogger().debug("Failed to execute inventory transaction from " + this.getName() + " with actions: " + Arrays.toString(transactionPacket.actions));
							break packetswitch; //oops!
						}

						break packetswitch;
					case InventoryTransactionPacket.TYPE_MISMATCH:
						if (transactionPacket.actions.length > 0) {
							this.server.getLogger().debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.length + ", " + Arrays.toString(transactionPacket.actions));
						}
						this.sendAllInventories();

						break packetswitch;
					case InventoryTransactionPacket.TYPE_USE_ITEM:
						UseItemData useItemData = (UseItemData) transactionPacket.transactionData;

						Vector3f clickPos = useItemData.clickPos;
						BlockVector3 blockVector = useItemData.blockPos;
						face = useItemData.face;
						int type = useItemData.actionType;

						if (face == null && type != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR) {
							break packetswitch;
						}

						switch (type) {
							case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK:
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

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? 13 : 7)) {
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
											if (i.getId() != 10000) {  // Hack
												if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
													inventory.setItemInHand(i);
													inventory.sendHeldItem(this.getViewers().values());
												}
												break packetswitch;
											}
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

									if (door.isTop()) {
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

								if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? 16 : 8) && (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
									if (this.isSurvival()) {
										this.getFoodData().updateFoodExpLevel(0.005);
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
											item = Items.air();
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
							case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK:
								if (!this.canInteract(target, isCreative() ? 8 : 5)) {
									break;
								} else if (target instanceof Player) {
									if ((((Player) target).getGamemode() & 0x01) > 0) {
										break;
									} else if (!this.server.getConfiguration().isPvp() || this.server.getDifficulty() == 0) {
										break;
									}
								}

								Enchantment[] enchantments = item.getEnchantments();

								float itemDamage = item.getAttackDamage();
								for (Enchantment enchantment : enchantments) {
									itemDamage += enchantment.getDamageBonus(target);
								}

								Map<EntityDamageEvent.DamageModifier, Float> damage = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
								damage.put(EntityDamageEvent.DamageModifier.BASE, itemDamage);

								float knockBack = 0.29f;
								Enchantment knockBackEnchantment = item.getEnchantment(Enchantment.KNOCKBACK);
								if (knockBackEnchantment != null) {
									knockBack += knockBackEnchantment.getLevel() * 0.1f;
								}

								EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(this, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, knockBack, knockBack, enchantments);
								if (this.isSpectator()) entityDamageByEntityEvent.setCancelled();
								if ((target instanceof Player) && !this.level.getGameRules().getBoolean(GameRule.PVP)) {
									entityDamageByEntityEvent.setCancelled();
								}

								if (!target.attack(entityDamageByEntityEvent)) {
									if (item.isTool() && this.isSurvival()) {
										this.inventory.sendContents(this);
									}
									break;
								}

								for (Enchantment enchantment : item.getEnchantments()) {
									enchantment.doPostAttack(this, target, null);
								}

								if (item.isTool() && this.isSurvivalLike()) {
									if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
										this.inventory.setItemInHand(Item.get(0));
									} else {
										if (item.getId() == 0 || this.inventory.getItemInHand().getId() == item.getId()) {
											this.inventory.setItemInHand(item);
										} else {
											server.getLogger().debug("Tried to set item " + item.getId() + " but " + this.username + " had item " + this.inventory.getItemInHand().getId() + " in their hand slot");
										}
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

										//int ticksUsed = this.server.getTick() - this.startAction;
										int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

										if (!item.onRelease(this, ticksUsed)) {
											this.inventory.sendContents(this);
										}
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
			case ProtocolInfo.TICK_SYNC_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				TickSyncPacket113 tickSyncRequest = (TickSyncPacket113) packet;

				TickSyncPacket113 tickSyncResponse = new TickSyncPacket113();
				tickSyncResponse.clientSendTime = tickSyncRequest.clientSendTime;
				tickSyncResponse.serverReceiveTime = this.server.getTick();
				this.dataPacket(tickSyncResponse);
				break;
			case ProtocolInfo.SETTINGS_COMMAND_PACKET:
				this.violation += 5;

				if (!callPacketReceiveEvent(packet)) {
					break;
				}

				if (!this.spawned || !this.isAlive()) {
					break;
				}

				this.resetCraftingGridType();
				this.craftingType = CRAFTING_SMALL;

				if (this.messageCounter <= 0) {
					break;
				}

				SettingsCommandPacket113 settingsCommandPacket = (SettingsCommandPacket113) packet;
				if (settingsCommandPacket.command.length() > 512) {
					break;
				}

				this.messageCounter--;

				String command = settingsCommandPacket.command;
				if (this.removeFormat) {
					command = TextFormat.clean(command, true);
				}

				PlayerCommandPreprocessEvent playerCommandPreprocessEvent = new PlayerCommandPreprocessEvent(this, command);
				this.server.getPluginManager().callEvent(playerCommandPreprocessEvent);
				if (playerCommandPreprocessEvent.isCancelled()) {
					break;
				}

				Timings.playerCommandTimer.startTiming();
				this.server.dispatchCommand(playerCommandPreprocessEvent.getPlayer(), playerCommandPreprocessEvent.getMessage().substring(1));
				Timings.playerCommandTimer.stopTiming();
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}

	}

	@Override
	protected void firstRespawn(Position pos) {
		RespawnPacket respawnPacket0 = new RespawnPacket();
		respawnPacket0.x = (float) pos.x;
		respawnPacket0.y = (float) pos.y + this.getEyeHeight();
		respawnPacket0.z = (float) pos.z;
		respawnPacket0.respawnState = RespawnPacket.STATE_SEARCHING_FOR_SPAWN;

		RespawnPacket respawnPacket1 = new RespawnPacket();
		respawnPacket1.x = respawnPacket0.x;
		respawnPacket1.y = respawnPacket0.y;
		respawnPacket1.z = respawnPacket0.z;
		respawnPacket1.respawnState = RespawnPacket.STATE_READY_TO_SPAWN;

		this.dataPacket(respawnPacket0);
		this.dataPacket(respawnPacket1);
	}

	@Override
	public void sendNetworkSettings() {
		/*NetworkSettingsPacket113 pk = new NetworkSettingsPacket113();
		//pk.compressionThreshold = NetworkSettingsPacket113.COMPRESS_NOTHING;
		this.dataPacket(pk);*/
	}
}
