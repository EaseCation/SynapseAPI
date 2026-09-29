package org.itxtech.synapseapi.network.synlib;

import cn.nukkit.utils.Logger;
import com.nukkitx.network.util.LatencyTrace;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.protocol.spp.SynapseDataPacket;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SessionWakeupTest {
    @Test
    void disabledKeepsLegacyWritesEvenWhenChannelIsNotWritable() throws Exception {
        runQueue(false);
    }

    @Test
    void enabledResumesInOrderAfterBackpressureAndStopsCleanly() throws Exception {
        runQueue(true);
    }

    private static void runQueue(boolean enabled) throws Exception {
        assertFalse(LatencyTrace.enabled());
        TestClient client = new TestClient(enabled);
        Channel channel = mock(Channel.class);
        AtomicBoolean writable = new AtomicBoolean(false);
        when(channel.isWritable()).thenAnswer(call -> writable.get());
        Queue<SynapseDataPacket> sent = new ConcurrentLinkedQueue<>();
        CountDownLatch written = new CountDownLatch(3);
        when(channel.writeAndFlush(any())).thenAnswer(call -> {
            sent.add(call.getArgument(0));
            written.countDown();
            return new DefaultChannelPromise(channel, ImmediateEventExecutor.INSTANCE).setSuccess();
        });
        Session session = client.getSession();
        session.channel = channel;
        session.updateAddress(new InetSocketAddress("127.0.0.1", 19132));
        List<SynapseDataPacket> expected = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            RedirectPacket packet = new RedirectPacket();
            packet.sessionId = new UUID(0, 1);
            packet.mcpeBuffer = new byte[]{(byte) index};
            packet.encode();
            expected.add(packet);
            client.pushMainToThreadPacket(packet);
        }
        client.startWorker();
        try {
            if (enabled) {
                assertFalse(written.await(50, TimeUnit.MILLISECONDS));
                assertEquals(3, client.getInternalQueueSize());
                writable.set(true);
                client.wakeWriter();
            }
            assertTrue(written.await(2, TimeUnit.SECONDS));
            assertEquals(expected, new ArrayList<>(sent));
            if (!enabled) verify(channel, never()).isWritable();
            client.interrupt();
        } finally {
            client.shutdown();
            client.join(2000);
        }
        assertFalse(client.isAlive());
    }

    private static final class TestClient extends SynapseClient {
        private TestClient(boolean enabled) {
            super(mock(Logger.class), 19132, "127.0.0.1", enabled);
        }

        @Override
        public synchronized void start() {
            // 构造后先安装测试通道，再启动实际队列线程。
        }

        private void startWorker() {
            super.start();
        }

        @Override
        public void run() {
            getSession().run();
        }
    }
}
