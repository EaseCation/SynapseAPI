package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent from the client to the server IF ProfilerLite is enabled AND the creator toggle for
 */
@ToString
public class ServerboundDiagnosticsPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_DIAGNOSTICS_PACKET;

    public float avgFps;
    public float avgServerSimTickTimeMs;
    public float avgClientSimTickTimeMs;
    public float avgBeginFrameTimeMs;
    public float avgInputTimeMs;
    public float avgRenderTimeMs;
    public float avgEndFrameTimeMs;
    public float avgRemainderTimePercent;
    public float avgUnaccountedTimePercent;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        avgFps = getLFloat();
        avgServerSimTickTimeMs = getLFloat();
        avgClientSimTickTimeMs = getLFloat();
        avgBeginFrameTimeMs = getLFloat();
        avgInputTimeMs = getLFloat();
        avgRenderTimeMs = getLFloat();
        avgEndFrameTimeMs = getLFloat();
        avgRemainderTimePercent = getLFloat();
        avgUnaccountedTimePercent = getLFloat();
    }

    @Override
    public void encode() {
    }
}
