package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent to update sound data.
 */
@ToString
public class ClientboundUpdateSoundDataPacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_UPDATE_SOUND_DATA_PACKET;

    public long serverSoundHandle;
    public SoundDataEvent soundEvent = SoundDataEvent.STOP;

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
        putLLong(serverSoundHandle);
        putEnum(soundEvent, SoundDataEvent::getName);
    }

    public enum SoundDataEvent {
        STOP("stop"),
        ;

        private final String name;

        SoundDataEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
