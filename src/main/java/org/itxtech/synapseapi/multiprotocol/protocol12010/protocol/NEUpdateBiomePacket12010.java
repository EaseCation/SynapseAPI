package org.itxtech.synapseapi.multiprotocol.protocol12010.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEUpdateBiomePacket12010 extends Packet12010 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_UPDATE_BIOME;

	public int subChunkX;
	public int subChunkY;
	public int subChunkZ;
	public Entry[] entries;

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
		putBlockVector3(subChunkX, subChunkY, subChunkZ);
		putArray(entries, (stream, entry) -> {
			stream.putByte(entry.type);
			stream.putVarInt(entry.dimension);
			stream.putString(entry.biome);
			putBlockVector3(entry.minX, entry.minY, entry.minZ);
			if (entry.type == Entry.TYPE_RANGE) {
				putBlockVector3(entry.maxX, entry.maxY, entry.maxZ);
			}
		});
	}

	public record Entry(
			int type,
			int dimension,
			String biome,
			int minX,
			int minY,
			int minZ,
			int maxX,
			int maxY,
			int maxZ
	) {
		public static final int TYPE_SINGLE = 0;
		public static final int TYPE_RANGE = 1;

		public Entry(int dimension, String biome, int x, int y, int z) {
			this(TYPE_SINGLE, dimension, biome, x, y, z, x, y, z);
		}

		public Entry(int dimension, String biome, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
			this(TYPE_RANGE, dimension, biome, minX, minY, minZ, maxX, maxY, maxZ);
		}
	}
}
