package org.itxtech.synapseapi.java;

import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ScriptMessagePacket11810;

import java.util.Objects;
import java.util.Optional;

/**
 * Java 客户端自定义载荷的双向桥接入口。
 */
public final class JavaCustomPayloadBridge {

    private JavaCustomPayloadBridge() {
    }

    public static boolean send(final SynapsePlayer player, final String channel, final byte[] payload) {
        Objects.requireNonNull(player, "player");
        if (!player.isOnline() || !player.isJavaClient()) {
            return false;
        }
        final Optional<String> encoded = JavaCustomPayloadEnvelope.encode(channel, payload);
        if (encoded.isEmpty()) {
            return false;
        }
        final ScriptMessagePacket11810 packet = new ScriptMessagePacket11810();
        packet.messageId = JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID;
        packet.value = encoded.get();
        return player.dataPacket(packet);
    }
}
