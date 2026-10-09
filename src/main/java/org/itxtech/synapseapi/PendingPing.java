package org.itxtech.synapseapi;

/** 单个未完成的服务端延迟请求；回包只用于关联，耗时取服务端单调时钟。 */
final class PendingPing {
    private long startedNanos;
    private boolean pending;

    boolean begin(long nowNanos) {
        if (this.pending) {
            return false;
        }
        this.startedNanos = nowNanos;
        this.pending = true;
        return true;
    }

    long acknowledge(long token, long nowNanos) {
        if (!this.pending || (token != this.startedNanos && token != this.startedNanos * 1000
                && token != this.startedNanos * 1000000)) {
            return -1;
        }
        long elapsed = nowNanos - this.startedNanos;
        if (elapsed < 0) {
            return -1;
        }
        this.pending = false;
        return elapsed;
    }

    boolean isPending() {
        return this.pending;
    }

    void expire() {
        this.pending = false;
    }
}
