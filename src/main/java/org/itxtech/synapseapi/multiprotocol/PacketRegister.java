package org.itxtech.synapseapi.multiprotocol;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.AvailableCommandsPacket110;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.LecternUpdatePacket110;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.VideoStreamConnectPacket110;
import org.itxtech.synapseapi.multiprotocol.protocol111.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol11460.protocol.PlayerListPacket11460;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.SetEntityDataPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.StartGamePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.StartGamePacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.UpdateAttributesPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol11620.protocol.StartGamePacket11620;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.FilterTextPacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.ResourcePacksInfoPacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116210.protocol.CameraShakePacket116210;
import org.itxtech.synapseapi.multiprotocol.protocol116210.protocol.PlayerAuthInputPacket116210;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.StartGamePacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.CraftingDataPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.InventoryContentPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.InventorySlotPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.GameRulesChangedPacket117;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.StartGamePacket117;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NPCRequestPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NpcDialoguePacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.ResourcePacksInfoPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.SetTitlePacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.AnimateEntityPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.CraftingDataPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.EntityPickRequestPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.StartGamePacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.StartGamePacket118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.UpdateSubChunkBlocksPacket118;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.PlayerStartItemCooldownPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.AddPlayerPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.SpawnParticleEffectPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.StartGamePacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.ClientboundMapItemDataPacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.NetworkChunkPublisherUpdatePacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.StartGamePacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestPermissionsPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.StartGamePacket119;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.AddEntityPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.AddPlayerPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.StartGamePacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAdventureSettingsPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.ClientboundMapItemDataPacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.FeatureRegistryPacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.MapInfoRequestPacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.ModalFormResponsePacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.NetworkChunkPublisherUpdatePacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.StartGamePacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.UpdateAttributesPacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.ToastRequestPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol11940.protocol.AddEntityPacket11940;
import org.itxtech.synapseapi.multiprotocol.protocol11940.protocol.AddPlayerPacket11940;
import org.itxtech.synapseapi.multiprotocol.protocol11940.protocol.SetEntityDataPacket11940;
import org.itxtech.synapseapi.multiprotocol.protocol11950.protocol.UpdateClientInputLocksPacket11950;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.CommandRequestPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.CraftingDataPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.PlayerSkinPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.StartGamePacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11963.protocol.PlayerSkinPacket11963;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.AddEntityPacket15;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.ClientboundMapItemDataPacket15;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityAbsolutePacket15;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.SetLocalPlayerAsInitializedPacket15;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author CreeperFace
 */
@Log4j2
public class PacketRegister {

    private static final Map<AbstractProtocol, Class<? extends DataPacket>[]> packetPool = new EnumMap<>(AbstractProtocol.class);

    /**
     * nukkit packet -> multi protocols packet
     */
    private static final Map<AbstractProtocol, Map<Class<? extends DataPacket>, Class<? extends IterationProtocolPacket>>> replacements = new EnumMap<>(AbstractProtocol.class);

    private static final Map<AbstractProtocol, boolean[]> neteaseSpecial = new EnumMap<>(AbstractProtocol.class);

    public static void init() {
        log.debug("Loading packet registry...");

        registerPacket(AbstractProtocol.PROTOCOL_12, ProtocolInfo.LOGIN_PACKET, org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket.class);

        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LOGIN_PACKET, LoginPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.TEXT_PACKET, TextPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.START_GAME_PACKET, StartGamePacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.SET_ACTOR_DATA_PACKET, SetEntityDataPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.ADD_ITEM_ACTOR_PACKET, AddItemEntityPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET, LevelSoundEventPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.UPDATE_BLOCK_PACKET, UpdateBlockPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.PLAYER_ACTION_PACKET, PlayerActionPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.UPDATE_BLOCK_SYNCED_PACKET, UpdateBlockSyncedPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LAB_TABLE_PACKET, LabTablePacket14.class);

        registerPacket(AbstractProtocol.PROTOCOL_15, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket15.class);
        registerPacket(AbstractProtocol.PROTOCOL_15, ProtocolInfo.MOVE_ACTOR_ABSOLUTE_PACKET, MoveEntityAbsolutePacket15.class);
        registerPacket(AbstractProtocol.PROTOCOL_15, ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET, ClientboundMapItemDataPacket15.class);
        registerPacket(AbstractProtocol.PROTOCOL_15, 0x70, SetLocalPlayerAsInitializedPacket15.class); // Packet ID changed...

        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.ADD_ITEM_ACTOR_PACKET, AddItemEntityPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.START_GAME_PACKET, StartGamePacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.RESOURCE_PACKS_INFO_PACKET, ResourcePacksInfoPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET, LevelSoundEventPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.NETWORK_STACK_LATENCY_PACKET, NetworkStackLatencyPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.UPDATE_SOFT_ENUM_PACKET, UpdateSoftEnumPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.REMOVE_OBJECTIVE_PACKET, RemoveObjectivePacket.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SET_DISPLAY_OBJECTIVE_PACKET, SetDisplayObjectivePacket.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SET_SCORE_PACKET, SetScorePacket.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SET_SCOREBOARD_IDENTITY_PACKET, SetScoreboardIdentityPacket.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SET_ACTOR_DATA_PACKET, SetEntityDataPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.UPDATE_BLOCK_PACKET, UpdateBlockPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET, ResourcePackClientResponsePacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.RESOURCE_PACK_STACK_PACKET, ResourcePackStackPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SHOW_STORE_OFFER_PACKET, ShowStoreOfferPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.PACKET_STORE_BUY_SUCC, NEStoreBuySuccPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.PACKET_NETEASE_JSON, NENetEaseJsonPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.PACKET_PY_RPC, NEPyRpcPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET, SetLocalPlayerAsInitializedPacket16.class);

        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.ADD_ITEM_ACTOR_PACKET, AddItemEntityPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.SCRIPT_CUSTOM_EVENT_PACKET, ScriptCustomEventPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.SET_ACTOR_DATA_PACKET, SetEntityDataPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.START_GAME_PACKET, StartGamePacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.TEXT_PACKET, TextPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.UPDATE_BLOCK_PACKET, UpdateBlockPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.SET_SCORE_PACKET, SetScorePacket17.class);

        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket18.class);
        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET, NetworkChunkPublisherUpdatePacket18.class);
        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.RESOURCE_PACK_STACK_PACKET, ResourcePackStackPacket18.class);
        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.START_GAME_PACKET, StartGamePacket18.class);
        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET, LevelSoundEventPacket18.class);
        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2, LevelSoundEventPacketV218.class);
        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET, SpawnParticleEffectPacket18.class);

        registerPacket(AbstractProtocol.PROTOCOL_19, ProtocolInfo.START_GAME_PACKET, StartGamePacket19.class);
        registerPacket(AbstractProtocol.PROTOCOL_19, ProtocolInfo.RESOURCE_PACKS_INFO_PACKET, ResourcePacksInfoPacket19.class);
        registerPacket(AbstractProtocol.PROTOCOL_19, ProtocolInfo.NETWORK_STACK_LATENCY_PACKET, NetworkStackLatencyPacket19.class);
        registerPacket(AbstractProtocol.PROTOCOL_19, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3, LevelSoundEventPacketV319.class);

        registerPacket(AbstractProtocol.PROTOCOL_110, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket110.class);
        registerPacket(AbstractProtocol.PROTOCOL_110, ProtocolInfo.LECTERN_UPDATE_PACKET, LecternUpdatePacket110.class);
        registerPacket(AbstractProtocol.PROTOCOL_110, ProtocolInfo.VIDEO_STREAM_CONNECT_PACKET, VideoStreamConnectPacket110.class);

        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket111.class);
        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET, ClientboundMapItemDataPacket111.class);
        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket111.class);
        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.UPDATE_TRADE_PACKET, UpdateTradePacket111.class);
        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.MAP_CREATE_LOCKED_COPY_PACKET, MapCreateLockedCopyPacket111.class);
        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.ON_SCREEN_TEXTURE_ANIMATION_PACKET, OnScreenTextureAnimationPacket111.class);
        registerPacket(AbstractProtocol.PROTOCOL_111, ProtocolInfo.LECTERN_UPDATE_PACKET, LecternUpdatePacket111.class);

        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.ADD_PAINTING_PACKET, AddPaintingPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.CLIENT_CACHE_STATUS_PACKET, ClientCacheStatusPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET, LevelEventGenericPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.RESOURCE_PACK_DATA_INFO_PACKET, ResourcePackDataInfoPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.START_GAME_PACKET, StartGamePacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET, ClientCacheBlobStatusPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.CLIENT_CACHE_MISS_RESPONSE_PACKET, ClientCacheMissResponsePacket112.class);

        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESOURCE_PACK_CHUNK_DATA_PACKET, ResourcePackChunkDataPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESOURCE_PACK_DATA_INFO_PACKET, ResourcePackDataInfoPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESOURCE_PACK_STACK_PACKET, ResourcePackStackPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESPAWN_PACKET, RespawnPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.START_GAME_PACKET, StartGamePacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.TICK_SYNC_PACKET, TickSyncPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.SETTINGS_COMMAND_PACKET, SettingsCommandPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.MOVE_ACTOR_DELTA_PACKET, MoveEntityDeltaPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.PACKET_CONFIRM_SKIN, ConfirmSkinPacket113.class);

        registerPacket(AbstractProtocol.PROTOCOL_114_60, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket11460.class);

        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.START_GAME_PACKET, StartGamePacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.PACKET_VIOLATION_WARNING_PACKET, PacketViolationWarningPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.CREATIVE_CONTENT_PACKET, CreativeContentPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.INVENTORY_CONTENT_PACKET, InventoryContentPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.INVENTORY_SLOT_PACKET, InventorySlotPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.INVENTORY_TRANSACTION_PACKET, InventoryTransactionPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.SET_SPAWN_POSITION_PACKET, SetSpawnPositionPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.SET_ACTOR_LINK_PACKET, SetEntityLinkPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.EMOTE_PACKET, EmotePacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.EMOTE_LIST_PACKET, EmoteListPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.PLAYER_AUTH_INPUT_PACKET, PlayerAuthInputPacket116.class);
        registerPacket(AbstractProtocol.PROTOCOL_116, ProtocolInfo.UPDATE_PLAYER_GAME_TYPE_PACKET, UpdatePlayerGameTypePacket116.class);

        registerPacket(AbstractProtocol.PROTOCOL_116_20, ProtocolInfo.START_GAME_PACKET, StartGamePacket11620.class);

        registerPacket(AbstractProtocol.PROTOCOL_116_100_NE, ProtocolInfo.START_GAME_PACKET, StartGamePacket116100NE.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100_NE, ProtocolInfo.MOVE_PLAYER_PACKET, MovePlayerPacket116100NE.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100_NE, ProtocolInfo.SET_ACTOR_DATA_PACKET, SetEntityDataPacket116100NE.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100_NE, ProtocolInfo.UPDATE_ATTRIBUTES_PACKET, UpdateAttributesPacket116100NE.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100_NE, ProtocolInfo.TEXT_PACKET, TextPacket116100NE.class);

        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.TEXT_PACKET, TextPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.START_GAME_PACKET, StartGamePacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.RESOURCE_PACK_STACK_PACKET, ResourcePackStackPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.CONTAINER_CLOSE_PACKET, ContainerClosePacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.MOVE_ACTOR_DELTA_PACKET, MoveEntityDeltaPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.ANIMATE_ENTITY_PACKET, AnimateEntityPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.ITEM_COMPONENT_PACKET, ItemComponentPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.PLAYER_FOG_PACKET, PlayerFogPacket116100.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_100, ProtocolInfo.CAMERA_SHAKE_PACKET, CameraShakePacket116100.class);

        registerPacket(AbstractProtocol.PROTOCOL_116_200, ProtocolInfo.RESOURCE_PACKS_INFO_PACKET, ResourcePacksInfoPacket116200.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_200, ProtocolInfo.FILTER_TEXT_PACKET, FilterTextPacket116200.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_200, ProtocolInfo.START_GAME_PACKET, StartGamePacket116200.class);

        registerPacket(AbstractProtocol.PROTOCOL_116_210, ProtocolInfo.PLAYER_AUTH_INPUT_PACKET, PlayerAuthInputPacket116210.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_210, ProtocolInfo.CAMERA_SHAKE_PACKET, CameraShakePacket116210.class);

        registerPacket(AbstractProtocol.PROTOCOL_116_220, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket116220.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_220, ProtocolInfo.INVENTORY_CONTENT_PACKET, InventoryContentPacket116220.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_220, ProtocolInfo.INVENTORY_SLOT_PACKET, InventorySlotPacket116220.class);
        registerPacket(AbstractProtocol.PROTOCOL_116_220, ProtocolInfo.PLAYER_AUTH_INPUT_PACKET, PlayerAuthInputPacket116220.class);

        registerPacket(AbstractProtocol.PROTOCOL_117, ProtocolInfo.START_GAME_PACKET, StartGamePacket117.class);
        registerPacket(AbstractProtocol.PROTOCOL_117, ProtocolInfo.GAME_RULES_CHANGED_PACKET, GameRulesChangedPacket117.class);

        registerPacket(AbstractProtocol.PROTOCOL_117_10, ProtocolInfo.RESOURCE_PACKS_INFO_PACKET, ResourcePacksInfoPacket11710.class);
        registerPacket(AbstractProtocol.PROTOCOL_117_10, ProtocolInfo.SET_TITLE_PACKET, SetTitlePacket11710.class);
        registerPacket(AbstractProtocol.PROTOCOL_117_10, ProtocolInfo.NPC_REQUEST_PACKET, NPCRequestPacket11710.class);
        registerPacket(AbstractProtocol.PROTOCOL_117_10, ProtocolInfo.NPC_DIALOGUE_PACKET, NpcDialoguePacket11710.class);

        registerPacket(AbstractProtocol.PROTOCOL_117_30, ProtocolInfo.START_GAME_PACKET, StartGamePacket11730.class);
        registerPacket(AbstractProtocol.PROTOCOL_117_30, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket11730.class);
        registerPacket(AbstractProtocol.PROTOCOL_117_30, ProtocolInfo.ANIMATE_ENTITY_PACKET, AnimateEntityPacket11730.class);
        registerPacket(AbstractProtocol.PROTOCOL_117_30, ProtocolInfo.ACTOR_PICK_REQUEST_PACKET, EntityPickRequestPacket11730.class);

        registerPacket(AbstractProtocol.PROTOCOL_118, ProtocolInfo.START_GAME_PACKET, StartGamePacket118.class);
        registerPacket(AbstractProtocol.PROTOCOL_118, ProtocolInfo.SUB_CHUNK_REQUEST_PACKET, SubChunkRequestPacket118.class);
        registerPacket(AbstractProtocol.PROTOCOL_118, ProtocolInfo.UPDATE_SUB_CHUNK_BLOCKS_PACKET, UpdateSubChunkBlocksPacket118.class);
        registerPacket(AbstractProtocol.PROTOCOL_118, ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET, SpawnParticleEffectPacket18.class);

        registerPacket(AbstractProtocol.PROTOCOL_118_10, ProtocolInfo.SUB_CHUNK_REQUEST_PACKET, SubChunkRequestPacket11810.class);
        registerPacket(AbstractProtocol.PROTOCOL_118_10, ProtocolInfo.PLAYER_START_ITEM_COOLDOWN_PACKET, PlayerStartItemCooldownPacket11810.class);

        registerPacket(AbstractProtocol.PROTOCOL_118_30, ProtocolInfo.START_GAME_PACKET, StartGamePacket11830.class);
        registerPacket(AbstractProtocol.PROTOCOL_118_30, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket11830.class);
        registerPacket(AbstractProtocol.PROTOCOL_118_30, ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET, SpawnParticleEffectPacket11830.class);

        registerPacket(AbstractProtocol.PROTOCOL_118_30_NE, ProtocolInfo.START_GAME_PACKET, StartGamePacket11830NE.class);
        registerPacket(AbstractProtocol.PROTOCOL_118_30_NE, ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET, ClientboundMapItemDataPacket11830NE.class);
        registerPacket(AbstractProtocol.PROTOCOL_118_30_NE, ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET, NetworkChunkPublisherUpdatePacket11830NE.class);
//        registerPacket(AbstractProtocol.PROTOCOL_118_30_NE, ProtocolInfo.MAP_INFO_REQUEST_PACKET, MapInfoRequestPacket11830NE.class); // 我们不使用客户端区块生成

        registerPacket(AbstractProtocol.PROTOCOL_119, ProtocolInfo.START_GAME_PACKET, StartGamePacket119.class);
        registerPacket(AbstractProtocol.PROTOCOL_119, ProtocolInfo.PLAYER_ACTION_PACKET, PlayerActionPacket119.class);
        registerPacket(AbstractProtocol.PROTOCOL_119, ProtocolInfo.REQUEST_ABILITY_PACKET, RequestAbilityPacket119.class);
        registerPacket(AbstractProtocol.PROTOCOL_119, ProtocolInfo.REQUEST_PERMISSIONS_PACKET, RequestPermissionsPacket119.class);
        registerPacket(AbstractProtocol.PROTOCOL_119, ProtocolInfo.TOAST_REQUEST_PACKET, ToastRequestPacket119.class);

        registerPacket(AbstractProtocol.PROTOCOL_119_10, ProtocolInfo.START_GAME_PACKET, StartGamePacket11910.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_10, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket11910.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_10, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket11910.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_10, ProtocolInfo.UPDATE_ABILITIES_PACKET, UpdateAbilitiesPacket11910.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_10, ProtocolInfo.UPDATE_ADVENTURE_SETTINGS_PACKET, UpdateAdventureSettingsPacket11910.class);

        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.START_GAME_PACKET, StartGamePacket11920.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET, NetworkChunkPublisherUpdatePacket11920.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.UPDATE_ATTRIBUTES_PACKET, UpdateAttributesPacket11920.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.MAP_INFO_REQUEST_PACKET, MapInfoRequestPacket11920.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET, ClientboundMapItemDataPacket11920.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.MODAL_FORM_RESPONSE_PACKET, ModalFormResponsePacket11920.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_20, ProtocolInfo.FEATURE_REGISTRY_PACKET, FeatureRegistryPacket11920.class);

        registerPacket(AbstractProtocol.PROTOCOL_119_40, ProtocolInfo.ADD_ACTOR_PACKET, AddEntityPacket11940.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_40, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket11940.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_40, ProtocolInfo.SET_ACTOR_DATA_PACKET, SetEntityDataPacket11940.class);

        registerPacket(AbstractProtocol.PROTOCOL_119_50, ProtocolInfo.UPDATE_CLIENT_INPUT_LOCKS_PACKET, UpdateClientInputLocksPacket11950.class);

        registerPacket(AbstractProtocol.PROTOCOL_119_60, ProtocolInfo.START_GAME_PACKET, StartGamePacket11960.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_60, ProtocolInfo.COMMAND_REQUEST_PACKET, CommandRequestPacket11960.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_60, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket11960.class);
        registerPacket(AbstractProtocol.PROTOCOL_119_60, ProtocolInfo.PLAYER_SKIN_PACKET, PlayerSkinPacket11960.class);

        registerPacket(AbstractProtocol.PROTOCOL_119_63, ProtocolInfo.PLAYER_SKIN_PACKET, PlayerSkinPacket11963.class);

        checkNeteaseSpecialExtend();
    }

    static void registerPacket(AbstractProtocol protocol, int id, Class<? extends IterationProtocolPacket> clazz) {
        if (!packetPool.containsKey(protocol)) packetPool.put(protocol, new Class[1024]);
        if (!neteaseSpecial.containsKey(protocol)) neteaseSpecial.put(protocol, new boolean[1024]);
        Class<? extends DataPacket>[] pool = packetPool.get(protocol);
        pool[id & 0xff] = clazz;
        boolean addToReplace = protocol.getPacketClass() != null && protocol.getPacketClass().isAssignableFrom(clazz);
        try {
            Method method;

            try {
                method = clazz.getDeclaredMethod("fromDefault", DataPacket.class, AbstractProtocol.class, boolean.class);
                if (method != null) {
                    neteaseSpecial.get(protocol)[id] = true;
                }
            } catch (NoSuchMethodException ex) {
                //ignore
            }
        } catch (Exception e) {
            MainLogger.getLogger().logException(e);
        }

        if (addToReplace) {
            try {
                Method method;

                try {
                    method = clazz.getDeclaredMethod("getDefaultPacket");
                } catch (NoSuchMethodException ex) {
                    return;
                }

                if(method != null) {
                    method.setAccessible(true);
                    Class c = (Class<? extends DataPacket>) method.invoke(null);
                    if (c != null) {
                        if (!replacements.containsKey(protocol)) replacements.put(protocol, new IdentityHashMap<>());
                        replacements.get(protocol).put(c, clazz);
                    }
                }
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
            }
        }
    }

    private static void checkNeteaseSpecialExtend() {
        neteaseSpecial.forEach(((protocol, data) -> {
            AbstractProtocol next = protocol;
            while ((next = next.next()) != null) {
                boolean[] array = neteaseSpecial.get(next);
                if (array != null) {
                    for (int i = 0; i < 1024; i++) {
                        if (data[i]) array[i] = true;
                    }
                }
            }
        }));
    }

    public static DataPacket getPacket(int id, int protocol) {
        AbstractProtocol ptl = AbstractProtocol.fromRealProtocol(protocol);
        if (ptl != null) {
            try {
                Class<? extends DataPacket>[] classes = packetPool.get(ptl);
                Class<? extends DataPacket> clazz = classes != null ? classes[id] : null;
                if (clazz != null) {
                    return clazz.newInstance();
                } else if (ptl == AbstractProtocol.PROTOCOL_12) {
                    return Server.getInstance().getNetwork().getPacket(id);
                } else {
                    AbstractProtocol previous = ptl.previous();
                    if (previous != null)
                        return getPacket(id, previous.getProtocolStart());
                    else return Server.getInstance().getNetwork().getPacket(id);
                }
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
                return null;
            }
        }

        return Server.getInstance().getNetwork().getPacket(id);
    }

    public static DataPacket getFullPacket(byte[] data, int protocol) {
        return getFullPacket(data, protocol, true);
    }

    public static DataPacket getFullPacket(byte[] data, int protocol, boolean maybeBatch) {
        //Server.getInstance().getLogger().debug(Arrays.toString(data));
        AbstractProtocol ptl = AbstractProtocol.fromRealProtocol(protocol);
        AbstractProtocol.PacketHeadData head = ptl.tryDecodePacketHead(data, maybeBatch);

        if (head != null) {
            int pid = head.getPid();
            if (pid <= 0 || pid > ProtocolInfo.COUNT) {
                // invalid packet
                return null;
            }

            DataPacket pk = getPacket(head.getPid(), protocol);
            if(pk == null) {
                Server.getInstance().getLogger().notice("C -> S null packet with PID: " + head.getPid());
                return null;
            }

            pk.setBuffer(data, head.getStartOffset());
            pk.setHelper(ptl.getHelper());
            try {
                pk.decode();
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
            }

            return pk;
        }

        return null;
    }

    public static DataPacket getCompatiblePacket(DataPacket packet, int protocol, boolean netease) {
        return getCompatiblePacket(packet, AbstractProtocol.fromRealProtocol(protocol), netease);
    }

    /**
     * 仅用于发送数据包！
     * @param packet 原数据包
     * @param endpointProtocol 目标版本的协议号
     * @return 转换后的数据包对象
     */
    public static DataPacket getCompatiblePacket(DataPacket packet, AbstractProtocol endpointProtocol, boolean netease) {
        if (packet.pid() == BatchPacket.NETWORK_ID) {
            return packet;
        }

        //如果是nk原始数据包
        if (AbstractProtocol.isPacketOriginal(packet)) {
            //如果目标协议高于原始，则从目标协议层层逐层往下搜索
            if (endpointProtocol.ordinal() >= AbstractProtocol.PROTOCOL_12.ordinal()) {
                AbstractProtocol index = endpointProtocol;
                do {
                    Class<? extends IterationProtocolPacket> clazz = replacements.getOrDefault(index, Collections.emptyMap()).get(packet.getClass());
                    if(clazz != null) {
                        try {
                            IterationProtocolPacket replaced = clazz.newInstance();
                            return check16ProtocolCompatible(replaced.fromDefault(packet, endpointProtocol, netease), endpointProtocol, netease);
                        } catch (InstantiationException | IllegalAccessException e) {
                            MainLogger.getLogger().logException(e);
                        }
                    }
                    //如果未找到匹配，开始下一个循环，向下搜索协议
                } while ((index = index.previous()) != null);
                return check16ProtocolCompatible(packet, endpointProtocol, netease);
            }
        } else if (endpointProtocol.getPacketClass() != packet.getClass()) {
            //版本不对应
            if (packet instanceof IterationProtocolPacket && ((IterationProtocolPacket) packet).getAbstractProtocol().ordinal() <= endpointProtocol.ordinal()) {
                //向上兼容，如：发出为1.4，目标为1.5。直接发送1.4的内容（各个迭代
                // 协议之间暂无转换方法）
                return check16ProtocolCompatible(packet, endpointProtocol, netease);
            } else {
                Server.getInstance().getLogger().warning("[SynapseAPI] PacketRegister::getCompatiblePacket 版本不对应：" + packet.getClass().getName() + " => " + endpointProtocol.name());
                return null;
            }
        }

        return check16ProtocolCompatible(packet, endpointProtocol, netease);
    }

    /**
     * 一个黑科技用于解决1.6的包头大改
     * 通过byte数组分割，把包id重新编码
     * @param packet 用于检查的数据包
     * @param endpointProtocol 目标客户端版本
     * @return 检查，兼容后的数据包
     */
    private static DataPacket check16ProtocolCompatible(DataPacket packet, AbstractProtocol endpointProtocol, boolean netease) {
        if (!(packet instanceof LevelChunkPacket || packet instanceof SubChunkPacket) && endpointProtocol.ordinal() >= AbstractProtocol.PROTOCOL_16.ordinal() && (!(packet instanceof IterationProtocolPacket) || !((IterationProtocolPacket) packet).is16Newer())) {
            CompatibilityPacket16 cp = new CompatibilityPacket16();
            packet.setHelper(endpointProtocol.getHelper());
            packet.neteaseMode = netease;
            cp.origin = packet;
            return cp;
        } else {
            return packet;
        }
    }

    public static boolean isNetEaseSpecial(AbstractProtocol protocol, int pid) {
        boolean[] special = neteaseSpecial.get(protocol);
        if (special != null) {
            return special[pid];
        }
        return false;
    }

    public static DataPacket[] decodeBatch(BatchPacket batchPacket) {
        /*byte[][] payload = new byte[packets.length * 2][];
        for (int i = 0; i < packets.length; i++) {
            DataPacket p = packets[i];
            int idx = i * 2;
            if (!p.isEncoded) {
                p.encode();
            }
            byte[] buf = p.getBuffer();
            payload[idx] = Binary.writeUnsignedVarInt(buf.length);
            payload[idx + 1] = buf;
        }
        byte[] data;
        data = Binary.appendBytes(payload);*/

        List<DataPacket> packets = new ObjectArrayList<>();

        byte[] payload = batchPacket.payload;
        BinaryStream stream = new BinaryStream(payload);
        while (!stream.feof()) {
            int len = (int) stream.getUnsignedVarInt();
            byte[] buffer = stream.get(len);

            DataPacket pk = getFullPacket(buffer, 113);
            packets.add(pk);
        }

        return packets.toArray(new DataPacket[0]);
    }

}
