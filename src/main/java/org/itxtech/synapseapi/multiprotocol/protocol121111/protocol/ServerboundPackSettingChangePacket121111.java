package org.itxtech.synapseapi.multiprotocol.protocol121111.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

import java.util.UUID;

@ToString
public class ServerboundPackSettingChangePacket121111 extends Packet121111 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_PACK_SETTING_CHANGE_PACKET;

    public static final int TYPE_FLOAT = 0;
    public static final int TYPE_BOOL = 1;
    public static final int TYPE_STRING = 2;

    public UUID packId;
    public String settingName;

    public int valueType;
    public float floatValue;
    public boolean boolValue;
    public String stringValue;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        packId = getUUID();
        settingName = getString();

        valueType = (int) getUnsignedVarInt();
        switch (valueType) {
            case TYPE_FLOAT:
                floatValue = getLFloat();
                break;
            case TYPE_BOOL:
                boolValue = getBoolean();
                break;
            case TYPE_STRING:
                stringValue = getString();
                break;
        }
    }

    @Override
    public void encode() {
    }
}
