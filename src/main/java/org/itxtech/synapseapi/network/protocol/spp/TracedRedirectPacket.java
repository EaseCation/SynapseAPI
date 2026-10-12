package org.itxtech.synapseapi.network.protocol.spp;

import com.nukkitx.network.util.LatencyTrace;
import javax.annotation.Nullable;

/** 仅在追踪开启时创建，普通协议包不增加任何诊断字段。 */
public final class TracedRedirectPacket extends RedirectPacket {
    @Nullable
    private LatencyTrace.Stamp stamp;

    @Override
    public String getLatencyTraceKey() {
        return stamp == null ? "" : stamp.key();
    }

    public void traceEnqueue(String stage, String connection) {
        if (LatencyTrace.enabled()) {
            byte[] bytes = getBuffer();
            stamp = new LatencyTrace.Stamp(LatencyTrace.key(bytes), System.nanoTime());
            LatencyTrace.record(stage, connection, stamp.key(), "", bytes.length, -1);
        }
    }

    public void traceDequeue(String stage, String connection) {
        if (stamp != null && LatencyTrace.enabled()) {
            LatencyTrace.elapsed(stage, connection, stamp.key(), getCount(), stamp.nanoTime());
        }
    }

    @Override
    @Nullable
    public TracedRedirectPacket clone() {
        TracedRedirectPacket copy = (TracedRedirectPacket) super.clone();
        if (copy != null) copy.stamp = null;
        return copy;
    }

    @Override
    public SynapseDataPacket clean() {
        stamp = null;
        return super.clean();
    }
}
