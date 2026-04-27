package org.itxtech.synapseapi.network.latency;

import cn.nukkit.Player;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.network.protocol.spp.RedirectTraceData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 聚合 NetworkStackLatency 链路分段耗时，避免高频日志影响网络线程。
 */
public final class NetworkLatencyTraceManager {
    public static final String[] SEGMENT_NAMES = {
            "nukkit_down_prepare",
            "nukkit_to_nemisys_tcp",
            "nemisys_down_queue",
            "nemisys_raknet_flush_wait",
            "client_roundtrip_unknown",
            "nemisys_up_queue",
            "nemisys_to_nukkit_tcp",
            "nukkit_async_decode",
            "nukkit_main_tick_wait",
            "total_rtt"
    };

    private static final int GLOBAL_CAPACITY = 4096;
    private static final int PLAYER_CAPACITY = 128;
    private static final long TIMEOUT_NS = 10_000_000_000L;
    private static final AtomicLong NEXT_TRACE_ID = new AtomicLong(1);

    private final boolean enabled;
    private final Map<UUID, PlayerWindow> players = new ConcurrentHashMap<>();
    private final TraceSample[] globalSamples = new TraceSample[GLOBAL_CAPACITY];
    private int globalIndex;
    private int globalSize;
    private long incompleteCount;
    private long completedCount;

    public NetworkLatencyTraceManager(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public RedirectTraceData createDownstreamTrace(Player player, long timestamp, long sendTimeNs) {
        if (!enabled) {
            return null;
        }
        cleanup();
        RedirectTraceData traceData = new RedirectTraceData();
        traceData.traceId = NEXT_TRACE_ID.getAndIncrement();
        traceData.clientTimestamp = timestamp;
        traceData.mark(RedirectTraceData.NUKKIT_DOWN_SEND, sendTimeNs);
        players.computeIfAbsent(getTracePlayerId(player), key -> new PlayerWindow()).putPending(traceData);
        return traceData.copy();
    }

    public void markEncodeStart(Player player, RedirectTraceData traceData, long timeNs) {
        if (!enabled || traceData == null) {
            return;
        }
        traceData.mark(RedirectTraceData.NUKKIT_DOWN_ENCODE_START, timeNs);
        updatePending(getTracePlayerId(player), traceData);
    }

    public void markEncodeEnd(Player player, RedirectTraceData traceData, long timeNs) {
        if (!enabled || traceData == null) {
            return;
        }
        traceData.mark(RedirectTraceData.NUKKIT_DOWN_ENCODE_END, timeNs);
        updatePending(getTracePlayerId(player), traceData);
    }

    public void markUpstreamReceive(RedirectTraceData traceData, long timeNs) {
        if (!enabled || traceData == null) {
            return;
        }
        traceData.mark(RedirectTraceData.NUKKIT_UP_RECEIVE, timeNs);
    }

    public void markAsyncDecode(RedirectTraceData traceData, long timeNs) {
        if (!enabled || traceData == null) {
            return;
        }
        traceData.mark(RedirectTraceData.NUKKIT_UP_DECODE, timeNs);
    }

    public void markMainHandle(Player player, RedirectTraceData traceData, long timeNs) {
        if (!enabled || traceData == null || player == null) {
            return;
        }
        traceData.mark(RedirectTraceData.NUKKIT_UP_MAIN_HANDLE, timeNs);
        updatePending(getTracePlayerId(player), traceData);
    }

    public void complete(Player player, long timestamp, long latencyNs) {
        if (!enabled || player == null) {
            return;
        }
        PlayerWindow window = players.computeIfAbsent(getTracePlayerId(player), key -> new PlayerWindow());
        RedirectTraceData traceData = window.removePending(timestamp);
        if (traceData == null) {
            incompleteCount++;
            return;
        }
        TraceSample sample = TraceSample.from(player.getName(), getTracePlayerId(player), latencyNs, traceData);
        if (sample == null) {
            incompleteCount++;
            return;
        }
        addSample(window, sample);
        completedCount++;
    }

    public void reset() {
        players.clear();
        Arrays.fill(globalSamples, null);
        globalIndex = 0;
        globalSize = 0;
        incompleteCount = 0;
        completedCount = 0;
    }

    public Summary getGlobalSummary() {
        return summarize(snapshotGlobal());
    }

    public Summary getPlayerSummary(UUID playerId) {
        PlayerWindow window = players.get(playerId);
        if (window == null) {
            return Summary.empty();
        }
        return summarize(window.snapshotSamples());
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public long getIncompleteCount() {
        return incompleteCount;
    }

    public UUID getTracePlayerId(Player player) {
        if (player instanceof SynapsePlayer synapsePlayer && synapsePlayer.getSessionId() != null) {
            return synapsePlayer.getSessionId();
        }
        return player.getUniqueId();
    }

    private void updatePending(UUID playerId, RedirectTraceData traceData) {
        PlayerWindow window = players.get(playerId);
        if (window != null) {
            window.putPending(traceData);
        }
    }

    private void addSample(PlayerWindow window, TraceSample sample) {
        globalSamples[globalIndex] = sample;
        globalIndex = (globalIndex + 1) % GLOBAL_CAPACITY;
        if (globalSize < GLOBAL_CAPACITY) {
            globalSize++;
        }
        window.addSample(sample);
    }

    private List<TraceSample> snapshotGlobal() {
        if (globalSize == 0) {
            return Collections.emptyList();
        }
        List<TraceSample> samples = new ArrayList<>(globalSize);
        for (TraceSample sample : globalSamples) {
            if (sample != null) {
                samples.add(sample);
            }
        }
        return samples;
    }

    private Summary summarize(List<TraceSample> samples) {
        if (samples.isEmpty()) {
            return Summary.empty();
        }
        SegmentStats[] stats = new SegmentStats[SEGMENT_NAMES.length];
        for (int i = 0; i < stats.length; i++) {
            long[] values = new long[samples.size()];
            int count = 0;
            long sum = 0;
            long max = 0;
            for (TraceSample sample : samples) {
                long value = sample.segments[i];
                if (value <= 0) {
                    continue;
                }
                values[count++] = value;
                sum += value;
                if (value > max) {
                    max = value;
                }
            }
            if (count == 0) {
                stats[i] = SegmentStats.empty();
                continue;
            }
            Arrays.sort(values, 0, count);
            stats[i] = new SegmentStats(count, sum / count, percentile(values, count, 0.50), percentile(values, count, 0.95), max);
        }
        return new Summary(samples.size(), stats);
    }

    private void cleanup() {
        long expireBefore = System.nanoTime() - TIMEOUT_NS;
        for (PlayerWindow window : players.values()) {
            incompleteCount += window.removeExpired(expireBefore);
        }
    }

    private static long percentile(long[] values, int count, double percentile) {
        int index = (int) Math.ceil(count * percentile) - 1;
        if (index < 0) {
            index = 0;
        } else if (index >= count) {
            index = count - 1;
        }
        return values[index];
    }

    public static long toMs(long ns) {
        return ns / 1_000_000L;
    }

    private static final class PlayerWindow {
        private final Map<Long, RedirectTraceData> pending = new LinkedHashMap<>();
        private final TraceSample[] samples = new TraceSample[PLAYER_CAPACITY];
        private int sampleIndex;
        private int sampleSize;

        synchronized void putPending(RedirectTraceData traceData) {
            pending.put(traceData.clientTimestamp, traceData.copy());
        }

        synchronized RedirectTraceData removePending(long timestamp) {
            return pending.remove(timestamp);
        }

        synchronized long removeExpired(long expireBefore) {
            long removed = 0;
            Iterator<RedirectTraceData> iterator = pending.values().iterator();
            while (iterator.hasNext()) {
                RedirectTraceData data = iterator.next();
                long created = data.times[RedirectTraceData.NUKKIT_DOWN_SEND];
                if (created > 0 && created < expireBefore) {
                    iterator.remove();
                    removed++;
                }
            }
            return removed;
        }

        synchronized void addSample(TraceSample sample) {
            samples[sampleIndex] = sample;
            sampleIndex = (sampleIndex + 1) % PLAYER_CAPACITY;
            if (sampleSize < PLAYER_CAPACITY) {
                sampleSize++;
            }
        }

        synchronized List<TraceSample> snapshotSamples() {
            if (sampleSize == 0) {
                return Collections.emptyList();
            }
            List<TraceSample> result = new ArrayList<>(sampleSize);
            for (TraceSample sample : samples) {
                if (sample != null) {
                    result.add(sample);
                }
            }
            return result;
        }
    }

    private record TraceSample(String playerName, UUID playerId, long[] segments) {
        static TraceSample from(String playerName, UUID playerId, long latencyNs, RedirectTraceData data) {
            long[] t = data.times;
            long[] s = new long[SEGMENT_NAMES.length];
            s[0] = delta(t[RedirectTraceData.NUKKIT_DOWN_SEND], t[RedirectTraceData.NUKKIT_DOWN_ENCODE_END]);
            s[1] = wallDelta(data.wallTimes[RedirectTraceData.NUKKIT_DOWN_ENCODE_END], data.wallTimes[RedirectTraceData.NEMISYS_DOWN_RECEIVE]);
            s[2] = delta(t[RedirectTraceData.NEMISYS_DOWN_RECEIVE], t[RedirectTraceData.NEMISYS_DOWN_QUEUE]);
            s[3] = delta(t[RedirectTraceData.NEMISYS_DOWN_QUEUE], t[RedirectTraceData.NEMISYS_DOWN_FLUSH]);
            s[5] = delta(t[RedirectTraceData.NEMISYS_UP_RECEIVE], t[RedirectTraceData.NEMISYS_UP_PROCESS]);
            s[6] = wallDelta(data.wallTimes[RedirectTraceData.NEMISYS_UP_SEND], data.wallTimes[RedirectTraceData.NUKKIT_UP_RECEIVE]);
            s[7] = delta(t[RedirectTraceData.NUKKIT_UP_RECEIVE], t[RedirectTraceData.NUKKIT_UP_DECODE]);
            s[8] = delta(t[RedirectTraceData.NUKKIT_UP_DECODE], t[RedirectTraceData.NUKKIT_UP_MAIN_HANDLE]);
            s[9] = latencyNs;

            long known = 0;
            for (int i = 0; i < s.length - 1; i++) {
                if (i == 4) {
                    continue;
                }
                known += Math.max(0, s[i]);
            }
            s[4] = Math.max(0, latencyNs - known);
            return new TraceSample(playerName, playerId, s);
        }

        private static long delta(long start, long end) {
            if (start <= 0 || end <= 0 || end < start) {
                return 0;
            }
            return end - start;
        }

        private static long wallDelta(long startMs, long endMs) {
            if (startMs <= 0 || endMs <= 0 || endMs < startMs) {
                return 0;
            }
            return (endMs - startMs) * 1_000_000L;
        }
    }

    public record Summary(int sampleCount, SegmentStats[] stats) {
        public static Summary empty() {
            SegmentStats[] stats = new SegmentStats[SEGMENT_NAMES.length];
            Arrays.fill(stats, SegmentStats.empty());
            return new Summary(0, stats);
        }
    }

    public record SegmentStats(int count, long avgNs, long p50Ns, long p95Ns, long maxNs) {
        public static SegmentStats empty() {
            return new SegmentStats(0, 0, 0, 0, 0);
        }
    }
}
