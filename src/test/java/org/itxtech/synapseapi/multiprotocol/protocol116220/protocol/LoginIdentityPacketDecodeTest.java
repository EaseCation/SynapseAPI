package org.itxtech.synapseapi.multiprotocol.protocol116220.protocol;

import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginIdentityPacketDecodeTest {
    // 真实转服首帧的原始PAI，不含认证凭据；固定为合法布局的反例。
    private static final byte[] ORIGINAL = HexFormat.of().parseHex("900152c4ab4064fe244335d700431c8569c2bbd60143000000000000000064fe2443808080808080c00201020052c4ab4064fe2443aacf20000000002e90a0bd00000000000000000000000001fd83bebd95bfbd772f76bf0000000000000000");

    @Test
    void originalJavaFrameDecodesCompletelyWithItsActualProtocolLayout() {
        PlayerAuthInputPacket116220 packet = packet(false);
        packet.decode();
        assertTrue(packet.feof());
        assertEquals(534442L, packet.getTick());
        assertEquals(0, packet.getMoveVecX());
        assertEquals(0, packet.getMoveVecZ());
    }

    @Test
    void prematureNeteaseClassificationCorruptsTheSameUnmodifiedJavaFrame() {
        PlayerAuthInputPacket116220 packet = packet(true);
        assertThrows(ArrayIndexOutOfBoundsException.class, packet::decode);
    }

    private PlayerAuthInputPacket116220 packet(boolean netease) {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol protocol = AbstractProtocol.fromRealProtocol(975);
            AbstractProtocol.PacketHeadData head = protocol.tryDecodePacketHead(ORIGINAL, false);
            assertEquals(144, head.getPid());
            PlayerAuthInputPacket116220 packet = new PlayerAuthInputPacket116220();
            packet.setHelper(protocol.getHelper());
            packet.setBuffer(ORIGINAL.clone(), head.getStartOffset());
            packet.neteaseMode = netease;
            return packet;
        }
    }
}
