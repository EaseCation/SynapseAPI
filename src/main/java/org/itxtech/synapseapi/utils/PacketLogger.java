package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.CompatibilityPacket16;

import java.util.Arrays;

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
            return;
        }

        log.trace("[Serverbound] {} - {} {}", player.getName(), packet, player.getServer().getTick());
    }

    static {
        CLIENTBOUND_FILTER[ProtocolInfo.LEVEL_CHUNK_PACKET] = true;
        CLIENTBOUND_FILTER[ProtocolInfo.SUB_CHUNK_PACKET] = true;
        CLIENTBOUND_FILTER[ProtocolInfo.CLIENT_CACHE_MISS_RESPONSE_PACKET] = true;
        CLIENTBOUND_FILTER[ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET] = true;
        CLIENTBOUND_FILTER[ProtocolInfo.NETWORK_STACK_LATENCY_PACKET] = true;

        SERVERBOUND_FILTER[ProtocolInfo.MOVE_PLAYER_PACKET] = true;
        SERVERBOUND_FILTER[ProtocolInfo.PLAYER_AUTH_INPUT_PACKET] = true;
        SERVERBOUND_FILTER[ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET] = true;
        SERVERBOUND_FILTER[ProtocolInfo.SUB_CHUNK_REQUEST_PACKET] = true;
        SERVERBOUND_FILTER[ProtocolInfo.ANIMATE_PACKET] = true;
        SERVERBOUND_FILTER[ProtocolInfo.NETWORK_STACK_LATENCY_PACKET] = true;

        if (false) {
//            Arrays.fill(CLIENTBOUND_FILTER, true);
            CLIENTBOUND_FILTER[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = false;

//            Arrays.fill(SERVERBOUND_FILTER, true);
            SERVERBOUND_FILTER[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = false;
        }
    }
}
