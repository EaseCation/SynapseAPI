package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.BinaryStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PlaySoundPacket12620")
class PlaySoundPacket12620Test {

    /**
     * 跳过 pid（1.26.20 起用无符号 varint 写包头），返回定位在负载起始处的读取流。
     */
    private static BinaryStream payload(PlaySoundPacket12620 packet) {
        packet.encode();
        BinaryStream in = new BinaryStream(packet.getBuffer());
        in.getUnsignedVarInt();
        return in;
    }

    @DisplayName("fromDefault 必须传递 1/8 格精确坐标，否则 1.26.20+ 客户端会静默丢精度")
    @Test
    void fromDefaultCopiesExactPosition() {
        PlaySoundPacket source = new PlaySoundPacket();
        source.name = "note.harp";
        source.setExactPosition(10.25d, 64.5d, -3.125d);
        source.volume = 0.5f;
        source.pitch = 1.5f;
        source.serverSoundHandle = 42L;

        PlaySoundPacket12620 target = new PlaySoundPacket12620();
        target.fromDefault(source);

        assertTrue(target.exactPosition);
        assertEquals(10.25d, target.exactX);
        assertEquals(64.5d, target.exactY);
        assertEquals(-3.125d, target.exactZ);
        assertEquals(42L, target.serverSoundHandle);
        assertEquals(0.5f, target.volume);
        assertEquals(1.5f, target.pitch);
        assertEquals("note.harp", target.name);
        // 整格字段仍保持回写值
        assertEquals(10, target.x);
        assertEquals(64, target.y);
        assertEquals(-4, target.z);

        BinaryStream in = payload(target);
        assertEquals("note.harp", in.getString());
        BlockVector3 pos = in.getBlockVector3();
        assertEquals(82, pos.x);
        assertEquals(516, pos.y);
        assertEquals(-25, pos.z);
        assertEquals(0.5f, in.getLFloat());
        assertEquals(1.5f, in.getLFloat());
        assertTrue(in.getBoolean());
        assertEquals(42L, in.getLLong());
    }

    @DisplayName("未使用精确坐标时保持整格编码与可选句柄语义")
    @Test
    void legacyPathKeepsWholeBlockEncoding() {
        PlaySoundPacket source = new PlaySoundPacket();
        source.name = "note.bd";
        source.x = 10;
        source.y = 64;
        source.z = -3;
        source.volume = 1f;
        source.pitch = 1f;

        PlaySoundPacket12620 target = new PlaySoundPacket12620();
        target.fromDefault(source);

        assertFalse(target.exactPosition);
        assertNull(target.serverSoundHandle);

        BinaryStream in = payload(target);
        assertEquals("note.bd", in.getString());
        BlockVector3 pos = in.getBlockVector3();
        assertEquals(80, pos.x);
        assertEquals(512, pos.y);
        assertEquals(-24, pos.z);
        assertEquals(1f, in.getLFloat());
        assertEquals(1f, in.getLFloat());
        assertFalse(in.getBoolean());
    }
}
