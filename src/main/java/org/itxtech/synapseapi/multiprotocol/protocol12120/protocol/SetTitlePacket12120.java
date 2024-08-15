package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetTitlePacket;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class SetTitlePacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.SET_TITLE_PACKET;

    public static final int TYPE_CLEAR = 0;
    public static final int TYPE_RESET = 1;
    public static final int TYPE_TITLE = 2;
    public static final int TYPE_SUBTITLE = 3;
    public static final int TYPE_ACTION_BAR = 4;
    public static final int TYPE_ANIMATION_TIMES = 5;

    public int type;
    public String text = "";
    public int fadeInTime = 0;
    public int stayTime = 0;
    public int fadeOutTime = 0;
    public String xuid = "";
    public String platformOnlineId = "";
    public String filteredText = "";

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
        this.putVarInt(this.type);
        this.putString(this.text);
        this.putVarInt(this.fadeInTime);
        this.putVarInt(this.stayTime);
        this.putVarInt(this.fadeOutTime);
        this.putString(this.xuid);
        this.putString(this.platformOnlineId);
        this.putString(this.filteredText);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, SetTitlePacket.class);

        SetTitlePacket packet = (SetTitlePacket) pk;
        this.type = packet.type;
        this.text = packet.text;
        this.fadeInTime = packet.fadeInTime;
        this.stayTime = packet.stayTime;
        this.fadeOutTime = packet.fadeOutTime;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetTitlePacket.class;
    }
}

