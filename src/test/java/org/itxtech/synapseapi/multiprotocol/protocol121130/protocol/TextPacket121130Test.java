package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.utils.BinaryStream;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextPacket121130Test {
    // 取自客户端实测接受的完整文字包，额外追加空字符串会立即断线。
    private static final byte[] VANILLA_POPUP = HexFormat.of().parseHex(
            "090002097472616e736c61746505706f7075700c6a756b65626f78706f70757003154c6f63616c20636f756e74646f776e2070726f626500000000");

    private static boolean localVanillaText() {
        return Boolean.getBoolean("synapseapi.force-netease-player")
                && Boolean.getBoolean("synapseapi.local-vanilla-text");
    }

    @Test
    void internationalPopupMatchesAcceptedWireBytes() {
        TextPacket121130 packet = popup(false);
        assertArrayEquals(VANILLA_POPUP, packet.getBuffer());
    }

    @Test
    void neteasePopupPreservesProductionSuffixUnlessLocalOverrideIsExplicit() {
        TextPacket121130 packet = popup(true);
        byte[] expected = localVanillaText() ? VANILLA_POPUP
                : Arrays.copyOf(VANILLA_POPUP, VANILLA_POPUP.length + 1);
        assertArrayEquals(expected, packet.getBuffer());
    }

    @Test
    void incomingChatConsumesExactlyTheSelectedWireFormat() {
        BinaryStream wire = new BinaryStream();
        wire.putBoolean(false);
        wire.putByte(TextPacket121130.BODY_TYPE_AUTHOR_AND_MESSAGE);
        wire.putString("chat");
        wire.putString("whisper");
        wire.putString("announcement");
        wire.putByte(TextPacket121130.TYPE_CHAT);
        wire.putString("LocalDev");
        wire.putString("chat probe");
        wire.putString("");
        wire.putString("");
        wire.putBoolean(false);
        if (!localVanillaText()) wire.putString("netease-extension");
        TextPacket121130 decoded = new TextPacket121130();
        decoded.neteaseMode = true;
        decoded.setBuffer(wire.getBuffer());
        decoded.decode();
        assertEquals("LocalDev", decoded.primaryName);
        assertEquals("chat probe", decoded.message);
        assertEquals(localVanillaText() ? "" : "netease-extension", decoded.unknownNE);
        assertNull(decoded.filteredMessage);
        assertEquals(wire.getBuffer().length, decoded.getOffset());
    }

    @Test
    void rawAndTipMessagesKeepTheirExistingWireFormat() {
        for (byte type : new byte[]{TextPacket121130.TYPE_RAW, TextPacket121130.TYPE_TIP}) {
            TextPacket121130 international = new TextPacket121130();
            international.type = type;
            international.message = "short message";
            international.encode();
            TextPacket121130 netease = new TextPacket121130();
            netease.type = type;
            netease.message = international.message;
            netease.neteaseMode = true;
            netease.encode();
            assertArrayEquals(international.getBuffer(), netease.getBuffer());
        }
    }

    private static TextPacket121130 popup(boolean netease) {
        TextPacket121130 packet = new TextPacket121130();
        packet.type = TextPacket121130.TYPE_POPUP;
        packet.message = "Local countdown probe";
        packet.neteaseMode = netease;
        packet.encode();
        return packet;
    }
}
