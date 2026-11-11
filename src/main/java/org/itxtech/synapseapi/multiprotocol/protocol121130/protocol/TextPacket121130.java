package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.Utils;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class TextPacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.TEXT_PACKET;

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
    public static final byte TYPE_OBJECT_ANNOUNCEMENT = 11;

    public static final byte BODY_TYPE_MESSAGE_ONLY = 0;
    public static final byte BODY_TYPE_AUTHOR_AND_MESSAGE = 1;
    public static final byte BODY_TYPE_MESSAGE_AND_PARAMS = 2;

    private static final int MAX_PARAMETER = 4;
    private static final int MAX_PARAMETER_NETEASE = 12;

    public boolean isLocalized;

    public byte type;
    public String message = "";
    public String[] parameters = new String[0]; // max: 4 || 12 (netease)
    public String primaryName = "";

    public String sendersXUID = "";
    public String platformIdString = "";
    @Nullable
    public String filteredMessage;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.isLocalized = this.getBoolean();

        int bodyType = this.getByte();
        switch (bodyType) {
            case BODY_TYPE_MESSAGE_ONLY:
                this.skip(BODY_MAGIC_MESSAGE_ONLY.length);
                this.type = (byte) this.getByte();
                this.message = this.getString(true);
                break;
            case BODY_TYPE_AUTHOR_AND_MESSAGE:
                this.skip(BODY_MAGIC_AUTHOR_AND_MESSAGE.length);
                this.type = (byte) this.getByte();
                this.primaryName = this.getString();
                this.message = this.getString(true);

                if (neteaseMode) {
                    this.parameters = this.getArray(new String[0], BinaryStream::getString, MAX_PARAMETER_NETEASE);
                }

                break;
            case BODY_TYPE_MESSAGE_AND_PARAMS:
                this.skip(BODY_MAGIC_MESSAGE_AND_PARAMS.length);
                this.type = (byte) this.getByte();
                this.message = this.getString(true);
                this.parameters = this.getArray(new String[0], BinaryStream::getString, neteaseMode ? MAX_PARAMETER_NETEASE : MAX_PARAMETER);
                break;
        }

        this.sendersXUID = this.getString();
        this.platformIdString = this.getString();
        this.filteredMessage = this.getOptional(BinaryStream::getString);
    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.isLocalized);

        String message = this.message;
        if (message.isEmpty()) {
            message = " ";
        }
        switch (this.type) {
            case TYPE_RAW:
            case TYPE_TIP:
            case TYPE_SYSTEM:
            case TYPE_OBJECT:
            case TYPE_OBJECT_WHISPER:
            case TYPE_OBJECT_ANNOUNCEMENT: {
                this.putByte(BODY_TYPE_MESSAGE_ONLY);
                this.put(BODY_MAGIC_MESSAGE_ONLY);
                this.putByte(this.type);
                this.putString(message);
                break;
            }
            case TYPE_CHAT:
            case TYPE_WHISPER:
            case TYPE_ANNOUNCEMENT: {
                this.putByte(BODY_TYPE_AUTHOR_AND_MESSAGE);
                this.put(BODY_MAGIC_AUTHOR_AND_MESSAGE);
                this.putByte(this.type);
                this.putString(this.primaryName);
                this.putString(message);

                if (neteaseMode) {
                    this.putArray(this.parameters, BinaryStream::putString, MAX_PARAMETER_NETEASE);
                }

                break;
            }
            case TYPE_TRANSLATION:
            case TYPE_POPUP:
            case TYPE_JUKEBOX_POPUP: {
                this.putByte(BODY_TYPE_MESSAGE_AND_PARAMS);
                this.put(BODY_MAGIC_MESSAGE_AND_PARAMS);
                this.putByte(this.type);
                this.putString(message);
                this.putArray(this.parameters, BinaryStream::putString, neteaseMode ? MAX_PARAMETER_NETEASE : MAX_PARAMETER);
                break;
            }
        }

        this.putString(this.sendersXUID);
        this.putString(this.platformIdString);
        this.putOptional(this.filteredMessage, BinaryStream::putString);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
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

    // WTH???
    private static final byte[] BODY_MAGIC_MESSAGE_ONLY = Utils.make(() -> {
        BinaryStream stream = new BinaryStream();
        stream.putString("raw");
        stream.putString("tip");
        stream.putString("systemmessage");
        stream.putString("textobjectwhisper");
        stream.putString("textobjectannouncement");
        stream.putString("textobject");
        return stream.getBuffer();
    });
    private static final byte[] BODY_MAGIC_AUTHOR_AND_MESSAGE = Utils.make(() -> {
        BinaryStream stream = new BinaryStream();
        stream.putString("chat");
        stream.putString("whisper");
        stream.putString("announcement");
        return stream.getBuffer();
    });
    private static final byte[] BODY_MAGIC_MESSAGE_AND_PARAMS = Utils.make(() -> {
        BinaryStream stream = new BinaryStream();
        stream.putString("translate");
        stream.putString("popup");
        stream.putString("jukeboxpopup");
        return stream.getBuffer();
    });
}
