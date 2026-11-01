package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.block.VoxelShape;

/**
 * Syncs client with server voxel shape data on world join.
 * This packet contains a copy of all behavior pack voxel shapes data.
 * Sends the serializable voxel shapes data to the client as it's needed on both the client and server.
 */
@ToString
public class VoxelShapesPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.VOXEL_SHAPES_PACKET;

    public static final VoxelShape[] VANILLA_SHAPES = {
            VoxelShape.builder()
                    .xSize(0)
                    .ySize(0)
                    .zSize(0)
                    .storage(new byte[0])
                    .xCoordinates(new float[]{0})
                    .yCoordinates(new float[]{0})
                    .zCoordinates(new float[]{0})
                    .build(),
            VoxelShape.builder()
                    .xSize(1)
                    .ySize(1)
                    .zSize(1)
                    .storage(new byte[]{1})
                    .xCoordinates(new float[]{0, 1})
                    .yCoordinates(new float[]{0, 1})
                    .zCoordinates(new float[]{0, 1})
                    .build(),
    };
    public static final ObjectIntPair<String>[] VANILLA_NAME_MAP = new ObjectIntPair[]{
            ObjectIntPair.of("minecraft:empty", 0),
            ObjectIntPair.of("minecraft:unit_cube", 1),
    };

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
            stream.putByte(shape.xSize, 127, 0);
            stream.putByte(shape.ySize, 127, 0);
            stream.putByte(shape.zSize, 127, 0);
            stream.putByteArray(shape.storage, 256048);

            putCoordinates(stream, shape.xCoordinates);
            putCoordinates(stream, shape.yCoordinates);
            putCoordinates(stream, shape.zCoordinates);
        });

        putArray(nameMap, (stream, entry) -> {
            stream.putString(entry.left());
            stream.putLShort(entry.rightInt());
        });
    }

    private static void putCoordinates(BinaryStream stream, float[] coordinates) {
        if (coordinates.length == 0) {
            throw new IllegalArgumentException("Coordinates cannot be empty");
        }
        stream.putArray(coordinates, BinaryStream::putLFloat, 128);
    }
}
