package org.itxtech.synapseapi.network.synlib;

import cn.nukkit.utils.Logger;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.protocol.spp.SynapseDataPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PacketQueueWakeupTest {
    @Test void inboundOfferWakesTheParkedExistingConsumer() throws Exception { check(false, true, true); }
    @Test void inboundNotificationBeforeParkKeepsItsPermitAndFifo() throws Exception { check(false, true, false); }
    @Test void outboundOfferWakesTheOriginalClientThread() throws Exception { check(true, true, true); }
    @Test void outboundNotificationBeforeParkKeepsItsPermitAndFifo() throws Exception { check(true, true, false); }
    @Test void disabledInboundDoesNotNotifyTheConsumer() throws Exception { check(false, false, true); }
    @Test void disabledOutboundDoesNotNotifyTheClientThread() throws Exception { check(true, false, true); }

    private void check(boolean outbound, boolean enabled, boolean afterPark) throws Exception {
        LocalClient client = new LocalClient(enabled);
        CountDownLatch beforePark = new CountDownLatch(1);
        AtomicBoolean mayPark = new AtomicBoolean();
        CountDownLatch consumed = new CountDownLatch(1);
        List<SynapseDataPacket> received = new ArrayList<>();
        Object blocker = new Object();
        Runnable body = () -> {
            beforePark.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            // 不使用另一个会消费 LockSupport 许可的等待器制造非生产竞争。
            while (!mayPark.get() && System.nanoTime() < deadline) Thread.onSpinWait();
            if (!mayPark.get()) return;
            LockSupport.park(blocker);
            SynapseDataPacket packet;
            while ((packet = outbound ? client.readMainToThreadPacket() : client.readThreadToMainPacket()) != null) {
                received.add(packet);
            }
            consumed.countDown();
        };
        Thread reader;
        if (outbound) {
            reader = client;
            client.startOwned(body);
        } else {
            reader = new Thread(body, "Owned inbound queue fixture");
            client.setInboundConsumerThread(reader);
            reader.start();
        }
        RedirectPacket first = packet(1);
        RedirectPacket second = packet(2);
        try {
            assertTrue(beforePark.await(2, TimeUnit.SECONDS));
            if (afterPark) {
                mayPark.set(true);
                awaitBlocker(reader, blocker);
                offer(client, outbound, first);
                assertEquals(0, outbound ? client.getExternalQueue().size() : client.getInternalQueueSize());
                if (enabled) assertTrue(consumed.await(2, TimeUnit.SECONDS));
                else assertFalse(consumed.await(50, TimeUnit.MILLISECONDS));
                if (!enabled) LockSupport.unpark(reader);
                assertTrue(consumed.await(2, TimeUnit.SECONDS));
                reader.join(2000);
                assertEquals(List.of(first), received);
            } else {
                offer(client, outbound, first);
                offer(client, outbound, second);
                mayPark.set(true);
                assertTrue(consumed.await(2, TimeUnit.SECONDS));
                reader.join(2000);
                assertEquals(List.of(first, second), received);
            }
            assertFalse(reader.isAlive());
        } finally {
            mayPark.set(true);
            LockSupport.unpark(reader);
            reader.interrupt();
            reader.join(2000);
            client.shutdown();
        }
    }

    @Test
    void realSessionPumpKeepsOrderAcrossItsByteBudget() throws Exception {
        LocalClient client = new LocalClient(true);
        List<RedirectPacket> expected = List.of(packet(1), packet(70_000), packet(2), packet(3));
        for (RedirectPacket packet : expected) client.pushMainToThreadPacket(packet);
        client.startOwned(() -> {
            client.sessionPump = true;
            client.getSession().run();
        });
        try {
            assertTrue(client.pumpRead.await(2, TimeUnit.SECONDS));
            client.join(2000);
            assertFalse(client.isAlive());
            assertEquals(expected, client.pumped);
            assertEquals(0, client.getInternalQueueSize());
        } finally {
            client.shutdown();
            client.interrupt();
            client.join(2000);
        }
    }

    private static void awaitBlocker(Thread thread, Object blocker) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (LockSupport.getBlocker(thread) != blocker && thread.isAlive() && System.nanoTime() < deadline) {
            Thread.yield();
        }
        assertSame(blocker, LockSupport.getBlocker(thread));
    }

    private static void offer(LocalClient client, boolean outbound, RedirectPacket packet) {
        if (outbound) client.pushMainToThreadPacket(packet);
        else client.pushThreadToMainPacket(packet);
    }

    private static RedirectPacket packet(int length) {
        RedirectPacket packet = new RedirectPacket();
        packet.sessionId = UUID.randomUUID();
        packet.mcpeBuffer = new byte[length];
        packet.encode();
        return packet;
    }

    static class LocalClient extends SynapseClient {
        private Runnable ownedBody;
        boolean sessionPump;
        final List<SynapseDataPacket> pumped = new ArrayList<>();
        final CountDownLatch pumpRead = new CountDownLatch(1);

        LocalClient(boolean enabled) { super(mock(Logger.class), 10325, "127.0.0.1", enabled); }
        // 构造器不启动真实网络；测试显式启动原 Thread 对象。
        @Override public synchronized void start() { }
        void startOwned(Runnable body) { this.ownedBody = body; super.start(); }
        @Override public void run() { this.ownedBody.run(); }
        @Override public SynapseDataPacket readMainToThreadPacket() {
            SynapseDataPacket packet = super.readMainToThreadPacket();
            if (this.sessionPump && packet != null) {
                this.pumped.add(packet);
                if (this.pumped.size() == 4) {
                    this.shutdown();
                    this.pumpRead.countDown();
                }
            }
            return packet;
        }
    }
}
