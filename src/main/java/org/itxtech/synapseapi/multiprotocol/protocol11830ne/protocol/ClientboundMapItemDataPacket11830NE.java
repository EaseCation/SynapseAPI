package org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol;

import cn.nukkit.network.protocol.ClientboundMapItemDataPacket;
import cn.nukkit.network.protocol.ClientboundMapItemDataPacket.MapDecorator;
import cn.nukkit.network.protocol.ClientboundMapItemDataPacket.MapTrackedObject;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Utils;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.awt.image.BufferedImage;

@ToString
public class ClientboundMapItemDataPacket11830NE extends Packet11830NE {

    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET;

    public static final int TEXTURE_UPDATE = 0x2;
    public static final int DECORATIONS_UPDATE = 0x4;
    public static final int CREATION = 0x8;

    public long mapId;
    public int type;
    public byte dimensionId;
    public boolean isLocked;
    public int originX;
    public int originY;
    public int originZ;

    public long[] parentMapIds = new long[0];
    public byte scale;

    public MapTrackedObject[] trackedEntities = new MapTrackedObject[0];
    public MapDecorator[] decorators = new MapDecorator[0];

    public int width;
    public int height;
    public int offsetX;
    public int offsetZ;
    public int[] colors = new int[0];
    public BufferedImage image;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(mapId);

        int update = 0;
        if (parentMapIds.length > 0) {
            update |= CREATION;
        }
        if (decorators.length > 0) {
            update |= DECORATIONS_UPDATE;
        }

        if (image != null || colors.length > 0) {
            update |= TEXTURE_UPDATE;
        }

        this.putUnsignedVarInt(update);
        this.putByte(this.dimensionId);
        this.putBoolean(this.isLocked);

        if (neteaseMode) {
            this.putSignedBlockPosition(this.originX, this.originY, this.originZ);
        }

        if ((update & CREATION) != 0) {
            this.putUnsignedVarInt(parentMapIds.length);
            for (long parentMapId : parentMapIds) {
                this.putEntityUniqueId(parentMapId);
            }
        }
        if ((update & (CREATION | TEXTURE_UPDATE | DECORATIONS_UPDATE)) != 0) {
            this.putByte(this.scale);
        }

        if ((update & DECORATIONS_UPDATE) != 0) {
            this.putUnsignedVarInt(trackedEntities.length);
            for (MapTrackedObject object : trackedEntities) {
                this.putLInt(object.type);
                if (object.type == MapTrackedObject.TYPE_BLOCK) {
                    this.putBlockVector3(object.x, object.y, object.z);
                } else if (object.type == MapTrackedObject.TYPE_ENTITY) {
                    this.putEntityUniqueId(object.entityUniqueId);
                } else {
                    throw new IllegalArgumentException("Unknown map object type " + object.type);
                }
            }

            this.putUnsignedVarInt(decorators.length);
            for (MapDecorator decorator : decorators) {
                this.putByte(decorator.img);
                this.putByte(decorator.rot);
                this.putByte(decorator.offsetX);
                this.putByte(decorator.offsetZ);
                this.putString(decorator.label == null ? "" : decorator.label);
                this.putUnsignedVarInt(decorator.color.getRGB());
            }
        }

        if ((update & TEXTURE_UPDATE) != 0) {
            this.putVarInt(width);
            this.putVarInt(height);
            this.putVarInt(offsetX);
            this.putVarInt(offsetZ);

            this.putUnsignedVarInt((long) width * height);

            if (image != null) {
                for (int y = 0; y < width; y++) {
                    for (int x = 0; x < height; x++) {
                        this.putUnsignedVarInt(Utils.toABGR(this.image.getRGB(x, y)));
                    }
                }

                image.flush();
            } else if (colors.length > 0) {
                for (int color : colors) {
                    this.putUnsignedVarInt(color);
                }
            }
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, ClientboundMapItemDataPacket.class);

        ClientboundMapItemDataPacket packet = (ClientboundMapItemDataPacket) pk;
        this.mapId = packet.mapId;
        this.type = packet.update;
        this.dimensionId = packet.dimensionId;

        this.parentMapIds = packet.parentMapIds;
        this.scale = packet.scale;

        this.trackedEntities = packet.trackedEntities;
        this.decorators = packet.decorators;

        this.width = packet.width;
        this.height = packet.height;
        this.offsetX = packet.offsetX;
        this.offsetZ = packet.offsetZ;
        this.colors = packet.colors;
        this.image = packet.image;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ClientboundMapItemDataPacket.class;
    }
}
