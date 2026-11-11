package org.itxtech.synapseapi;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

/**
 * 仅仲裁观察者显示，不写入验证游标、权威位置或 Nya 状态。
 * 两侧均只尝试锁；主线程从不等待网络发包回调，未收敛显示在下一次原更新重试。
 */
final class ObserverMovementPublication {
    static final AtomicLong failures = new AtomicLong();
    private final ReentrantLock publicationLock = new ReentrantLock();
    private volatile boolean published;
    private volatile boolean reconcilePending;
    private boolean failed;
    private volatile long processedTick = -1;
    private long tick = -1;
    private long epoch;
    private float x;
    private float y;
    private float z;
    private float yaw;
    private float pitch;

    boolean hasPublication() {
        return published;
    }

    void publish(long tick, long epoch, float x, float y, float z, float yaw, float pitch,
                 BooleanSupplier valid, Runnable send) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch)
                || publicationLock.isHeldByCurrentThread() || !publicationLock.tryLock()) return;
        yaw = displayYaw(yaw);
        pitch %= 360;
        try {
            // 显示不承担违规裁决，真正倒序仍由原 FIFO 中的验证入口处理。
            if (failed || reconcilePending || tick <= this.tick || tick <= this.processedTick || !valid.getAsBoolean()) return;
            boolean changed = !published || this.epoch != epoch || this.x != x || this.y != y || this.z != z
                    || this.yaw != yaw || this.pitch != pitch;
            this.tick = tick;
            this.epoch = epoch;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            published = true;
            if (!changed) return;
            try {
                send.run();
            } catch (RuntimeException exception) {
                reconcilePending = true;
                failures.incrementAndGet();
                failed = true;
            }
        } finally {
            publicationLock.unlock();
        }
    }

    private static float displayYaw(float yaw) {
        // 沿用原 handler 的角度归一化；再消除 float 舍入产生的 360 与 0 表示差。
        yaw %= 360;
        if (yaw < 0) yaw += 360;
        return yaw % 360;
    }

    void reconcile(long processedTick, long epoch, float x, float y, float z, float yaw, float pitch,
                   Runnable send) {
        yaw = displayYaw(yaw);
        pitch %= 360;
        this.processedTick = Math.max(this.processedTick, processedTick);
        if (!published) return;
        if (publicationLock.isHeldByCurrentThread() || !publicationLock.tryLock()) {
            reconcilePending = true;
            return;
        }
        try {
            // 同一代际仍有更晚显示时，不让较旧提交覆盖它；代际改变必须收敛。
            if (this.epoch == epoch && processedTick < tick) return;
            if (reconcilePending || this.epoch != epoch || this.x != x || this.y != y || this.z != z
                    || this.yaw != yaw || this.pitch != pitch) {
                // 先消耗当前请求，发送回调重入产生的新请求留给下次更新。
                reconcilePending = false;
                try {
                    send.run();
                } catch (RuntimeException exception) {
                    reconcilePending = true;
                    failures.incrementAndGet();
                    failed = true;
                }
            }
            this.epoch = epoch;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        } finally {
            publicationLock.unlock();
        }
    }
}
