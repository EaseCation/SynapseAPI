package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Syncs client with server voxel shape data on world join.
 * This packet contains a copy of all behavior pack voxel shapes data.
 * Sends the serializable voxel shapes data to the client as it's needed on both the client and server.
 */
@ToString
public class VoxelShapesPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.VOXEL_SHAPES_PACKET;

    public VoxelShape[] shapes = new VoxelShape[0];
    /// Pair<Name, RegistryHandle>[]
    public ObjectIntPair<String>[] nameMap = new ObjectIntPair[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();

        putArray(shapes, (stream, shape) -> {
            stream.putByte(shape.xSize);
            stream.putByte(shape.ySize);
            stream.putByte(shape.zSize);
            stream.putByteArray(shape.storage);

            stream.putUnsignedVarInt(shape.xCoordinates.length);
            for (float value : shape.xCoordinates) {
                stream.putLFloat(value);
            }

            stream.putUnsignedVarInt(shape.yCoordinates.length);
            for (float value : shape.yCoordinates) {
                stream.putLFloat(value);
            }

            stream.putUnsignedVarInt(shape.zCoordinates.length);
            for (float value : shape.zCoordinates) {
                stream.putLFloat(value);
            }
        });

        putArray(nameMap, (stream, entry) -> {
            stream.putString(entry.left());
            stream.putLShort(entry.rightInt());
        });
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class VoxelShape {
        /**
         * Number of cells along the X axis.
         */
        public int xSize;
        /**
         * Number of cells along the Y axis.
         */
        public int ySize;
        /**
         * Number of cells along the Z axis.
         */
        public int zSize;
        /**
         * Solid/empty state per cell.
         */
        public byte[] storage;

        /**
         * Cell boundaries along the X axis.
         */
        public float[] xCoordinates;
        /**
         * Cell boundaries along the Y axis.
         */
        public float[] yCoordinates;
        /**
         * Cell boundaries along the Z axis.
         */
        public float[] zCoordinates;
    }
}
