package org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BlockColor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
public class MapInfoRequestPacket11830NE extends Packet11830NE {

    public static final int NETWORK_ID = ProtocolInfo.MAP_INFO_REQUEST_PACKET;

    public long mapId;
    public PixelEntry[] clientPixels;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        mapId = getEntityUniqueId();

        if (!neteaseMode) {
            return;
        }

        clientPixels = getArrayLInt(PixelEntry.class, stream -> new PixelEntry(stream.getLInt(), stream.getLShort()));
    }

    @Override
    public void encode() {
    }

    @ToString
    @AllArgsConstructor
    public static class PixelEntry {
        public static final int Y_INDEX_MULTIPLIER = 128;

        public int color;
        public int index;

        public BlockColor getColor() {
            return new BlockColor(color, true);
        }

        public int getX() {
            return index % Y_INDEX_MULTIPLIER;
        }

        public int getY() {
            return Math.floorDiv(index, Y_INDEX_MULTIPLIER);
        }
    }
}
