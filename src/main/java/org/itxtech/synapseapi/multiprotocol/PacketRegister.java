package org.itxtech.synapseapi.multiprotocol;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.AvailableCommandsPacket110;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.LecternUpdatePacket110;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.VideoStreamConnectPacket110;
import org.itxtech.synapseapi.multiprotocol.protocol111.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.AddEntityPacket15;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.ClientboundMapItemDataPacket15;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityAbsolutePacket15;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.*;
import org.itxtech.synapseapi.multiprotocol.utils.CraftingPacketManager;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author CreeperFace
 */
public class PacketRegister {

    private static Map<AbstractProtocol, Class<? extends DataPacket>[]> packetPool = new Hashtable<>();

    /**
     * nukkit packet -> multi protocols packet
     */
    private static Map<AbstractProtocol, Map<Class<? extends DataPacket>, Class<? extends IterationProtocolPacket>>> replacements = new HashMap<>();

    private static Map<AbstractProtocol, boolean[]> neteaseSpecial = new HashMap<>();

    public static void init() {
        registerPacket(AbstractProtocol.PROTOCOL_12, ProtocolInfo.LOGIN_PACKET, org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket.class);

        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LOGIN_PACKET, LoginPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.TEXT_PACKET, TextPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.START_GAME_PACKET, StartGamePacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.SET_ENTITY_DATA_PACKET, SetEntityDataPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.ADD_ENTITY_PACKET, AddEntityPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.ADD_ITEM_ENTITY_PACKET, AddItemEntityPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET, LevelSoundEventPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.UPDATE_BLOCK_PACKET, UpdateBlockPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.PLAYER_ACTION_PACKET, PlayerActionPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket14.class);
        registerPacket(AbstractProtocol.PROTOCOL_14, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket14.class);

        registerPacket(AbstractProtocol.PROTOCOL_15, ProtocolInfo.ADD_ENTITY_PACKET, AddEntityPacket15.class);
        registerPacket(AbstractProtocol.PROTOCOL_15, ProtocolInfo.MOVE_ENTITY_PACKET, MoveEntityAbsolutePacket15.class);
        registerPacket(AbstractProtocol.PROTOCOL_15, ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET, ClientboundMapItemDataPacket15.class);

        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.ADD_ENTITY_PACKET, AddEntityPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.ADD_ITEM_ENTITY_PACKET, AddItemEntityPacket16.class);
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
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SET_ENTITY_DATA_PACKET, SetEntityDataPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.UPDATE_BLOCK_PACKET, UpdateBlockPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET, ResourcePackClientResponsePacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.RESOURCE_PACK_STACK_PACKET, ResourcePackStackPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.SHOW_STORE_OFFER_PACKET, ShowStoreOfferPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.PACKET_STORE_BUY_SUCC, NEStoreBuySuccPacket16.class);
        registerPacket(AbstractProtocol.PROTOCOL_16, ProtocolInfo.PACKET_NETEASE_JSON, NENetEaseJsonPacket16.class);

        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.ADD_ENTITY_PACKET, AddEntityPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.ADD_ITEM_ENTITY_PACKET, AddItemEntityPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.SCRIPT_CUSTOM_EVENT_PACKET, ScriptCustomEventPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.SET_ENTITY_DATA_PACKET, SetEntityDataPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.START_GAME_PACKET, StartGamePacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.TEXT_PACKET, TextPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.UPDATE_BLOCK_PACKET, UpdateBlockPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket17.class);
        registerPacket(AbstractProtocol.PROTOCOL_17, ProtocolInfo.SET_SCORE_PACKET, SetScorePacket17.class);

        registerPacket(AbstractProtocol.PROTOCOL_18, ProtocolInfo.ADD_ENTITY_PACKET, AddEntityPacket18.class);
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

        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.ADD_PAINTING_PACKET, AddPaintingPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.CLIENT_CACHE_STATUS_PACKET, ClientCacheStatusPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET, LevelEventGenericPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.RESOURCE_PACK_DATA_INFO_PACKET, ResourcePackDataInfoPacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.START_GAME_PACKET, StartGamePacket112.class);
        registerPacket(AbstractProtocol.PROTOCOL_112, ProtocolInfo.LEVEL_EVENT_PACKET, LevelEventPacket112.class);

        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.PLAYER_LIST_PACKET, PlayerListPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESOURCE_PACK_CHUNK_DATA_PACKET, ResourcePackChunkDataPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESOURCE_PACK_DATA_INFO_PACKET, ResourcePackDataInfoPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESOURCE_PACK_STACK_PACKET, ResourcePackStackPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.RESPAWN_PACKET, RespawnPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.START_GAME_PACKET, StartGamePacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.CRAFTING_DATA_PACKET, CraftingDataPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.AVAILABLE_COMMANDS_PACKET, AvailableCommandsPacket113.class);
        registerPacket(AbstractProtocol.PROTOCOL_113, ProtocolInfo.ADD_PLAYER_PACKET, AddPlayerPacket113.class);

        checkNeteaseSpecialExtend();
        CraftingPacketManager.rebuildPacket();
    }

    public static void registerPacket(AbstractProtocol protocol, int id, Class<? extends IterationProtocolPacket> clazz) {
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
                    Class c = (Class<? extends DataPacket>) method.invoke(null);
                    if (c != null) {
                        if (!replacements.containsKey(protocol)) replacements.put(protocol, new HashMap<>());
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
                if (neteaseSpecial.containsKey(next)) {
                    boolean[] array = neteaseSpecial.get(next);
                    for (int i = 0; i < 1024; i++) {
                        if (data[i]) array[i] = true;
                    }
                }
            }
        }));
    }

    public static DataPacket getPacket(int id, int protocol) {
        AbstractProtocol ptl = AbstractProtocol.fromRealProtocol(protocol);
        if (ptl != null && packetPool.containsKey(ptl)) {
            try {
                Class<? extends DataPacket> clazz = packetPool.get(ptl)[id];
                if (clazz != null) {
                    return clazz.newInstance();
                }/* else if (ptl == AbstractProtocol.PROTOCOL_112) {
                    return getPacket(id, 354); //按照1.11匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_111) {
                    return getPacket(id, 340); //按照1.10匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_110) {
                    return getPacket(id, 332); //按照1.9匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_19) {
                    return getPacket(id, 312); //按照1.8匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_18) {
                    return getPacket(id, 290); //按照1.7匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_17) {
                    return getPacket(id, 282); //按照1.6匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_16) {
                    return getPacket(id, 270); //按照1.5匹配
                } else if (ptl == AbstractProtocol.PROTOCOL_15) {
                    return getPacket(id, 261); //按照1.4匹配
                }*/else if (ptl == AbstractProtocol.PROTOCOL_12) {
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
        AbstractProtocol ptl = AbstractProtocol.fromRealProtocol(protocol);
        AbstractProtocol.PacketHeadData head = ptl.tryDecodePacketHead(data, true);

        if (head != null) {
            DataPacket pk = getPacket(head.getPid(), protocol);
            if(pk == null) {
                Server.getInstance().getLogger().notice("C -> S null packet with PID: " + head.getPid());
                return null;
            }

            pk.setBuffer(data, head.getStartOffset());
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
        if(packet.pid() == BatchPacket.NETWORK_ID) {
            return packet;
        }

        //如果是nk原始数据包
        if (AbstractProtocol.isPacketOriginal(packet)) {
            //如果目标协议高于原始，则从目标协议层层逐层往下搜索
            if (endpointProtocol.ordinal() >= AbstractProtocol.PROTOCOL_12.ordinal()) {
                AbstractProtocol index = endpointProtocol;
                do {
                    Class<? extends IterationProtocolPacket> clazz = replacements.get(index).get(packet.getClass());
                    if(clazz != null) {
                        try {
                            IterationProtocolPacket replaced = clazz.newInstance();
                            return check16ProtocolCompatible(replaced.fromDefault(packet, endpointProtocol, netease), endpointProtocol);
                        } catch (Exception e) {
                            MainLogger.getLogger().logException(e);
                        }
                    }
                    //如果未找到匹配，开始下一个循环，向下搜索协议
                } while ((index = index.previous()) != null);
                return check16ProtocolCompatible(packet, endpointProtocol);
            }
        } else if (endpointProtocol.getPacketClass() != packet.getClass()) {
            //版本不对应
            if (packet instanceof IterationProtocolPacket && ((IterationProtocolPacket) packet).getAbstractProtocol().ordinal() <= endpointProtocol.ordinal()) {
                //向上兼容，如：发出为1.4，目标为1.5。直接发送1.4的内容（各个迭代
                // 协议之间暂无转换方法）
                return check16ProtocolCompatible(packet, endpointProtocol);
            } else {
                Server.getInstance().getLogger().debug("[SynapseAPI] PacketRegister::getCompatiblePacket 版本不对应：" + packet.getClass().getName() + " => " + endpointProtocol.name());
                return null;
            }
        }

        return check16ProtocolCompatible(packet, endpointProtocol);
    }

    /**
     * 一个黑科技用于解决1.6的包头大改
     * 通过byte数组分割，把包id重新编码
     * @param packet 用于检查的数据包
     * @param endpointProtocol 目标客户端版本
     * @return 检查，兼容后的数据包
     */
    private static DataPacket check16ProtocolCompatible(DataPacket packet, AbstractProtocol endpointProtocol) {
        if (endpointProtocol.ordinal() >= AbstractProtocol.PROTOCOL_16.ordinal() && (!(packet instanceof IterationProtocolPacket) || !((IterationProtocolPacket) packet).is16Newer())) {
            CompatibilityPacket16 cp = new CompatibilityPacket16();
            cp.origin = packet;
            return cp;
        } else {
            return packet;
        }
    }

    public static boolean isNetEaseSpecial(AbstractProtocol protocol, int pid) {
        if (neteaseSpecial.containsKey(protocol)) {
            return neteaseSpecial.get(protocol)[pid];
        }
        return false;
    }

    public static DataPacket[] decodeBatch(BatchPacket batchPacket) {
        /*byte[][] payload = new byte[packets.length * 2][];
        for (int i = 0; i < packets.length; i++) {
            DataPacket p = packets[i];
            if (!p.isEncoded) {
                p.encode();
            }
            byte[] buf = p.getBuffer();
            payload[i * 2] = Binary.writeUnsignedVarInt(buf.length);
            payload[i * 2 + 1] = buf;
        }
        byte[] data;
        data = Binary.appendBytes(payload);*/

        List<DataPacket> packets = new ArrayList<>();

        byte[] payload = batchPacket.payload;
        BinaryStream stream = new BinaryStream(payload);
        while (!stream.feof()) {
            int len = (int) stream.getUnsignedVarInt();
            byte[] buffer = stream.get(len);

            DataPacket pk = getFullPacket(buffer, 113);
            packets.add(pk);
        }

        return packets.stream().toArray(DataPacket[]::new);
    }
}
