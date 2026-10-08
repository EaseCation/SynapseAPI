package org.itxtech.synapseapi;

import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.input.InboundContext;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InputConnectionBindingTest {
    @Test
    void closedTcpRejectsAlreadyQueuedInputEvenBeforeSessionInvalidation() {
        SynapsePlayer player = mock(SynapsePlayer.class, CALLS_REAL_METHODS);
        SynapseEntry entry = mock(SynapseEntry.class);
        player.synapseEntry = entry;
        Channel channel = mock(Channel.class);
        when(entry.isCurrentInputConnection(channel)).thenReturn(true);
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        player.bindInputSession(session, channel);
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024,
                exception -> fail(exception));
        AtomicBoolean ran = new AtomicBoolean();
        dispatcher.offer(session, new ServerInputTask() {
            @Override public boolean isValid() { return player.isAcceptingInputPackets(); }
            @Override public void run(InboundContext context) { ran.set(true); }
        }, 1, true);
        when(entry.isCurrentInputConnection(channel)).thenReturn(false);
        assertTrue(session.isActive());
        assertFalse(player.isInputSessionActive());
        dispatcher.drain(true, 1, Long.MAX_VALUE);
        assertFalse(ran.get());
        assertEquals(0, dispatcher.snapshot().queuedTasks());
    }

    @Test
    void replacementConnectionDoesNotReactivateTheOldPlayerBinding() {
        SynapsePlayer player = mock(SynapsePlayer.class, CALLS_REAL_METHODS);
        SynapseEntry entry = mock(SynapseEntry.class);
        player.synapseEntry = entry;
        Channel old = mock(Channel.class);
        Channel current = mock(Channel.class);
        when(entry.isCurrentInputConnection(old)).thenReturn(false);
        when(entry.isCurrentInputConnection(current)).thenReturn(true);
        player.bindInputSession(new ServerInputDispatcher.Session(UUID.randomUUID()), old);
        assertFalse(player.isAcceptingInputPackets());
        player.bindInputSession(new ServerInputDispatcher.Session(UUID.randomUUID()), current);
        assertTrue(player.isAcceptingInputPackets());
        player.bindInputSession(null);
        assertFalse(player.isAcceptingInputPackets());
    }

    @Test
    void explicitInternalBindingKeepsItsNonTransportContract() {
        SynapsePlayer player = mock(SynapsePlayer.class, CALLS_REAL_METHODS);
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        player.bindInputSession(session);
        assertTrue(player.isInputSessionActive());
    }
}
