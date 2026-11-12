package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.CompatibilityPacket16;

import java.util.Arrays;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public final class PacketLogger {
    public static final boolean[] CLIENTBOUND_FILTER = new boolean[ProtocolInfo.COUNT];
    public static final boolean[] SERVERBOUND_FILTER = new boolean[ProtocolInfo.COUNT];

    public static void handleClientboundPacket(Player player, DataPacket packet) {
        if (packet instanceof CompatibilityPacket16) {
            packet = ((CompatibilityPacket16) packet).origin;
        }

        int id = packet.pid();

        if (id > 0 && id < ProtocolInfo.COUNT && CLIENTBOUND_FILTER[id]) {
            return;
        }

        log.trace("[Clientbound] {} - {} {}", player.getName(), packet, player.getServer().getTick());
    }

    public static void handleServerboundPacket(Player player, DataPacket packet) {
        if (packet instanceof CompatibilityPacket16) {
            packet = ((CompatibilityPacket16) packet).origin;
        }

        int id = packet.pid();

        if (id > 0 && id < ProtocolInfo.COUNT && SERVERBOUND_FILTER[id]) {
            if (id == ProtocolInfo.PLAYER_AUTH_INPUT_PACKET && packet instanceof IPlayerAuthInputPacket inputPacket
                    && (inputPacket.hasFlag(PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION)
                    || inputPacket.hasFlag(PlayerAuthInputFlags.PERFORM_BLOCK_ACTIONS)
                    || inputPacket.hasFlag(PlayerAuthInputFlags.PERFORM_ITEM_STACK_REQUEST))) {
            } else {
                return;
            }
        }

        log.trace("[Serverbound] {} - {} {}", player.getName(), packet, player.getServer().getTick());
    }

    static {
        boolean debugChunk = false;
        boolean debugPlayer = false;

        CLIENTBOUND_FILTER[ProtocolInfo.LEVEL_CHUNK_PACKET] = !debugChunk;
        CLIENTBOUND_FILTER[ProtocolInfo.SUB_CHUNK_PACKET] = !debugChunk;
        CLIENTBOUND_FILTER[ProtocolInfo.CLIENT_CACHE_MISS_RESPONSE_PACKET] = !debugChunk;
        CLIENTBOUND_FILTER[ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET] = !debugChunk;
        CLIENTBOUND_FILTER[ProtocolInfo.NETWORK_STACK_LATENCY_PACKET] = true;

        if (SERVER_AUTHORITATIVE_INVENTORY_DEBUG) {
            CLIENTBOUND_FILTER[ProtocolInfo.MOVE_PLAYER_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.MOVE_ACTOR_ABSOLUTE_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.MOVE_ACTOR_DELTA_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.AVAILABLE_COMMANDS_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.SET_TIME_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.UPDATE_ATTRIBUTES_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.SET_ACTOR_DATA_PACKET] = true;
            CLIENTBOUND_FILTER[ProtocolInfo.BLOCK_ACTOR_DATA_PACKET] = true;
        }

        SERVERBOUND_FILTER[ProtocolInfo.MOVE_PLAYER_PACKET] = !debugPlayer;
        SERVERBOUND_FILTER[ProtocolInfo.PLAYER_AUTH_INPUT_PACKET] = !debugPlayer;
        SERVERBOUND_FILTER[ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET] = !debugChunk;
        SERVERBOUND_FILTER[ProtocolInfo.SUB_CHUNK_REQUEST_PACKET] = !debugChunk;
        SERVERBOUND_FILTER[ProtocolInfo.ANIMATE_PACKET] = !debugPlayer;
        SERVERBOUND_FILTER[ProtocolInfo.NETWORK_STACK_LATENCY_PACKET] = true;

        if (false) {
//            Arrays.fill(CLIENTBOUND_FILTER, true);
            CLIENTBOUND_FILTER[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = false;

//            Arrays.fill(SERVERBOUND_FILTER, true);
            SERVERBOUND_FILTER[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = false;
        }
    }
}
