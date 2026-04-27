package org.itxtech.synapseapi.network.protocol.spp;

/**
 * NetworkStackLatency 诊断链路的可选 Synapse sidecar 数据。
 */
public class RedirectTraceData implements Cloneable {
    public static final int MAGIC = 0x4e4c5452; // NLTR
    public static final int VERSION = 1;
    public static final int FIELD_COUNT = 12;

    public static final int NUKKIT_DOWN_SEND = 0;
    public static final int NUKKIT_DOWN_ENCODE_START = 1;
    public static final int NUKKIT_DOWN_ENCODE_END = 2;
    public static final int NEMISYS_DOWN_RECEIVE = 3;
    public static final int NEMISYS_DOWN_QUEUE = 4;
    public static final int NEMISYS_DOWN_FLUSH = 5;
    public static final int NEMISYS_UP_RECEIVE = 6;
    public static final int NEMISYS_UP_PROCESS = 7;
    public static final int NEMISYS_UP_SEND = 8;
    public static final int NUKKIT_UP_RECEIVE = 9;
    public static final int NUKKIT_UP_DECODE = 10;
    public static final int NUKKIT_UP_MAIN_HANDLE = 11;

    public long traceId;
    public long clientTimestamp;
    public long[] times = new long[FIELD_COUNT];
    public long[] wallTimes = new long[FIELD_COUNT];

    public void mark(int index, long timeNs) {
        times[index] = timeNs;
        wallTimes[index] = System.currentTimeMillis();
    }

    public RedirectTraceData copy() {
        RedirectTraceData data = new RedirectTraceData();
        data.traceId = traceId;
        data.clientTimestamp = clientTimestamp;
        data.times = times.clone();
        data.wallTimes = wallTimes.clone();
        return data;
    }
}
