package org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class TextPacket116100NE extends Packet116100NE {

    public static final int NETWORK_ID = ProtocolInfo.TEXT_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static final byte TYPE_RAW = 0;
    public static final byte TYPE_CHAT = 1;
    public static final byte TYPE_TRANSLATION = 2;
    public static final byte TYPE_POPUP = 3;
    public static final byte TYPE_JUKEBOX_POPUP = 4;
    public static final byte TYPE_TIP = 5;
    public static final byte TYPE_SYSTEM = 6;
    public static final byte TYPE_WHISPER = 7;
    public static final byte TYPE_ANNOUNCEMENT = 8;
    public static final byte TYPE_OBJECT = 9;
    public static final byte TYPE_OBJECT_WHISPER = 10;

    public byte type;
    public boolean isLocalized = false;

    public String message = "";
    public String[] parameters = new String[0];
    public String primaryName = "";

    public String sendersXUID = "";
    public String platformIdString = "";

    @Override
    public void decode() {
        this.type = (byte) getByte();
        this.isLocalized = this.getBoolean();

        switch (this.type) {
            case TYPE_CHAT:
            case TYPE_WHISPER:
            case TYPE_ANNOUNCEMENT:
                this.primaryName = this.getString();
            case TYPE_RAW:
            case TYPE_TIP:
            case TYPE_SYSTEM:
            case TYPE_OBJECT:
            case TYPE_OBJECT_WHISPER:
                this.message = this.getString();
                break;
            case TYPE_TRANSLATION:
            case TYPE_POPUP:
            case TYPE_JUKEBOX_POPUP:
                this.message = this.getString();
                this.parameters = this.getArray(String.class, BinaryStream::getString);
                break;
            default:
                break;
        }
        this.sendersXUID = this.getString();
        this.platformIdString = this.getString();

        if (this.type == TYPE_CHAT || this.type == TYPE_POPUP) {
            this.getString(); //wtf netease?
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte(this.type);
        this.putBoolean(this.isLocalized);
        switch (this.type) {
            case TYPE_CHAT:
            case TYPE_WHISPER:
            case TYPE_ANNOUNCEMENT:
                this.putString(this.primaryName);
            case TYPE_RAW:
            case TYPE_TIP:
            case TYPE_SYSTEM:
            case TYPE_OBJECT:
            case TYPE_OBJECT_WHISPER:
                this.putString(this.message);
                break;
            case TYPE_TRANSLATION:
            case TYPE_POPUP:
            case TYPE_JUKEBOX_POPUP:
                this.putString(this.message);
                this.putUnsignedVarInt(this.parameters.length);
                for (String parameter : this.parameters) {
                    this.putString(parameter);
                }
                break;
            default:
                break;
        }
        this.putString(this.sendersXUID);
        this.putString(this.platformIdString);

        if (this.type == TYPE_CHAT || this.type == TYPE_POPUP) {
            this.putString(""); // Biggest wtf
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, TextPacket.class);
        TextPacket packet = (TextPacket) pk;
        this.type = packet.type;
        this.isLocalized = packet.isLocalized;

        this.message = packet.message;
        this.parameters = packet.parameters;
        this.primaryName = packet.primaryName;

        this.sendersXUID = packet.sendersXUID;
        this.platformIdString = packet.platformIdString;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return TextPacket.class;
    }
}
