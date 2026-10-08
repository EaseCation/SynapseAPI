package org.itxtech.synapseapi;

/** 同一真实服务器 tick 的所有输入阶段共享原攻击尝试额度。 */
final class AttackAttempts {
    private final int rejectAt;
    private int tick = Integer.MIN_VALUE;
    private int count;

    AttackAttempts(int rejectAt) {
        if (rejectAt <= 1) {
            throw new IllegalArgumentException("Attack rejection threshold must exceed one");
        }
        this.rejectAt = rejectAt;
    }

    boolean claim(int serverTick) {
        this.rotate(serverTick);
        // 到达拒绝值后保持拒绝，不能让无效流量把计数器溢出后重新放行。
        if (this.count < this.rejectAt) {
            this.count++;
        }
        return this.count < this.rejectAt;
    }

    private void rotate(int serverTick) {
        if (this.tick != serverTick) {
            this.tick = serverTick;
            this.count = 0;
        }
    }

    int tick() {
        return this.tick;
    }

    int count(int serverTick) {
        return this.tick == serverTick ? this.count : 0;
    }
}
