package org.itxtech.synapseapi.multiprotocol.protocol111.protocol;

import cn.nukkit.network.protocol.ClientboundMapItemDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Utils;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.Packet15;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by CreeperFace on 5.3.2017.
 */
public class ClientboundMapItemDataPacket111 extends Packet111 {

    public long[] eids = new long[0];

    public long mapId;
    public int update;
    public byte scale;
    public boolean isLocked;
    public int width;
    public int height;
    public int offsetX;
    public int offsetZ;

    public byte dimensionId;

    public ClientboundMapItemDataPacket.MapDecorator[] decorators = new ClientboundMapItemDataPacket.MapDecorator[0];

    public ClientboundMapItemDataPacket.MapTrackedObject[] trackedEntities = new ClientboundMapItemDataPacket.MapTrackedObject[0];

    public int[] colors = new int[0];
    public BufferedImage image = null;

    //update
    public static final int TEXTURE_UPDATE = 2;
    public static final int DECORATIONS_UPDATE = 4;
    public static final int ENTITIES_UPDATE = 8;

    @Override
    public int pid() {
        return ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(mapId);

        int update = 0;
        if (eids.length > 0) {
            update |= 0x08;
        }
        if (decorators.length > 0 || trackedEntities.length > 0) {
            update |= DECORATIONS_UPDATE;
        }

        if (image != null || colors.length > 0) {
            update |= TEXTURE_UPDATE;
        }

        this.putUnsignedVarInt(update);
        this.putByte(this.dimensionId);
        this.putBoolean(this.isLocked);

        if ((update & 0x08) != 0) { //TODO: find out what these are for
            this.putUnsignedVarInt(eids.length);
            for (long eid : eids) {
                this.putEntityUniqueId(eid);
            }
        }
        if ((update & (TEXTURE_UPDATE | DECORATIONS_UPDATE)) != 0) {
            this.putByte(this.scale);
        }

        if ((update & DECORATIONS_UPDATE) != 0) {
            this.putUnsignedVarInt(trackedEntities.length);
            for (ClientboundMapItemDataPacket.MapTrackedObject object : this.trackedEntities) {
                this.putLInt(object.type);
                if (object.type == ClientboundMapItemDataPacket.MapTrackedObject.TYPE_BLOCK) {
                    this.putBlockVector3(object.z, object.y, object.z);
                } else if (object.type == ClientboundMapItemDataPacket.MapTrackedObject.TYPE_ENTITY)
                    this.putEntityUniqueId(object.entityUniqueId);
            }

            this.putUnsignedVarInt(decorators.length);

            for (ClientboundMapItemDataPacket.MapDecorator decorator : decorators) {
                this.putByte(decorator.img);
                this.putByte(decorator.rot);
                this.putByte(decorator.offsetX);
                this.putByte(decorator.offsetZ);
                this.putString(decorator.label == null ? "" : decorator.label);

                byte red = (byte) decorator.color.getRed();
                byte green = (byte) decorator.color.getGreen();
                byte blue = (byte) decorator.color.getBlue();

                this.putUnsignedVarInt(Utils.toRGB(red, green, blue, (byte) 0xff));
                //this.putUnsignedVarInt(decorator.color.getRGB());
            }
        }

        if ((update & TEXTURE_UPDATE) != 0) {
            this.putVarInt(width);
            this.putVarInt(height);
            this.putVarInt(offsetX);
            this.putVarInt(offsetZ);

            this.putUnsignedVarInt(width * height);

            if (image != null) {
                for (int y = 0; y < width; y++) {
                    for (int x = 0; x < height; x++) {
                        Color color = new Color(image.getRGB(x, y), true);
                        byte red = (byte) color.getRed();
                        byte green = (byte) color.getGreen();
                        byte blue = (byte) color.getBlue();

                        putUnsignedVarInt(Utils.toRGB(red, green, blue, (byte) 0xff));
                    }
                }

                image.flush();
            } else if (colors.length > 0) {
                for (int color : colors) {
                    putUnsignedVarInt(color);
                }
            }
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, ClientboundMapItemDataPacket.class);

        ClientboundMapItemDataPacket packet = (ClientboundMapItemDataPacket) pk;
        this.mapId = packet.mapId;
        this.update = packet.update;
        this.scale = packet.scale;
        this.width = packet.width;
        this.height = packet.height;
        this.offsetX = packet.offsetX;
        this.offsetZ = packet.offsetZ;
        this.dimensionId = packet.dimensionId;
        this.eids = packet.eids;
        this.decorators = packet.decorators;
        this.trackedEntities = packet.trackedEntities;
        this.colors = packet.colors;
        this.image = packet.image;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ClientboundMapItemDataPacket.class;
    }
}
