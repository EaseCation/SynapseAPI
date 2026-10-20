package org.itxtech.synapseapi;

import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ResourcePackChunkRequestPacket;
import cn.nukkit.network.protocol.TextPacket;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceRequestAdmissionTest {
    private final SynapsePlayer player = mock(SynapsePlayer.class);
    private final ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
            32, 1024, 32, 1024, exception -> fail(exception));
    private final ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
    private final List<DataPacket> consumed = new ArrayList<>();

    private void offer(DataPacket packet) {
        assertEquals(ServerInputDispatcher.OfferResult.ACCEPTED, dispatcher.offer(session, new ServerInputTask() {
            @Override
            public boolean canRunBetweenTicks() {
                return BetweenTickPackets.canRunBetweenTicks(player, packet);
            }

            @Override
            public void run(InboundContext context) {
                consumed.add(packet);
            }
        }, 1, BetweenTickPackets.supports(packet)));
    }

    @Test
    void downloadBeforeSpawnDoesNotWaitForPhysicalInputReadiness() {
        ResourcePackChunkRequestPacket request = new ResourcePackChunkRequestPacket();
        offer(request);
        offer(new NetworkStackLatencyPacket19());
        assertEquals(2, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertSame(request, consumed.getFirst());
        verify(player, never()).canProcessInputStateBetweenTicks();
    }

    @Test
    void resourceRequestDoesNotBypassAnEarlierWorldTickBarrier() {
        TextPacket barrier = new TextPacket();
        ResourcePackChunkRequestPacket request = new ResourcePackChunkRequestPacket();
        offer(barrier);
        offer(request);
        offer(new NetworkStackLatencyPacket19());
        assertEquals(0, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertEquals(3, dispatcher.drainWorldTick(8));
        assertSame(barrier, consumed.get(0));
        assertSame(request, consumed.get(1));
    }

    @Test
    void retiredDownloadCannotHoldUpAnotherSession() {
        offer(new ResourcePackChunkRequestPacket());
        dispatcher.invalidate(session);
        List<Boolean> marker = new ArrayList<>();
        dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> marker.add(true), 1, true);
        dispatcher.drain(true, 7, Long.MAX_VALUE);
        assertTrue(consumed.isEmpty());
        assertEquals(List.of(true), marker);
        assertEquals(1, dispatcher.snapshot().discardedTasks());
    }
}
