package org.itxtech.synapseapi.utils;

import cn.nukkit.network.protocol.ProtocolInfo;

public final class PacketUtil {
    public static final boolean[] CAN_BE_SENT_BEFORE_LOGIN = new boolean[ProtocolInfo.COUNT];

    public static boolean canBeSentBeforeLogin(int packetId) {
        return CAN_BE_SENT_BEFORE_LOGIN[packetId];
    }

    static {
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.BATCH_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.LOGIN_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.CLIENT_TO_SERVER_HANDSHAKE_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.SERVER_TO_CLIENT_HANDSHAKE_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.NETWORK_SETTINGS_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.PLAY_STATUS_PACKET] = true;
        CAN_BE_SENT_BEFORE_LOGIN[ProtocolInfo.DISCONNECT_PACKET] = true;
    }

    private PacketUtil() {
    }
}
