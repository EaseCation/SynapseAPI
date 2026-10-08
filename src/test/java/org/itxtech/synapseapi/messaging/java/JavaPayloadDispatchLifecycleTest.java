package org.itxtech.synapseapi.messaging.java;

import cn.nukkit.plugin.PluginBase;
import org.itxtech.synapseapi.SynapsePlayer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaPayloadDispatchLifecycleTest {
    @Test
    void retiringDuringFirstCallbackStopsRemainingUnorderedRegistrations() {
        verifyTwoListeners(true, true, 1);
    }

    @Test
    void disabledModePreservesAllCallbacksAfterRetirement() {
        verifyTwoListeners(false, true, 2);
    }

    @Test
    void everyNormalListenerSeesItsOwnPayloadCopy() {
        verifyTwoListeners(true, false, 2);
    }

    @Test
    void alreadyRetiredSourceDoesNotDispatchCallbacks() {
        StandardJavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        SynapsePlayer player = player(true);
        doReturn(false).when(player).isAcceptingInputPackets();
        TestPlugin plugin = plugin();
        AtomicInteger calls = new AtomicInteger();
        messenger.registerIncomingPluginChannel(plugin, "networklab:payload", (sender, channel, payload) -> calls.incrementAndGet());
        messenger.dispatchIncomingMessage(player, "networklab:payload", new byte[]{1});
        assertEquals(0, calls.get());
    }

    @Test
    void retiredControlAnnouncementCannotRecreateOldPlayerChannelState() {
        StandardJavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        SynapsePlayer player = player(true);
        doReturn(false).when(player).isAcceptingInputPackets();
        messenger.dispatchIncomingMessage(player, JavaCustomPayloadMessenger.REGISTER_CHANNEL,
                "networklab:payload".getBytes(StandardCharsets.UTF_8));
        assertFalse(messenger.getListeningChannels(player).contains("networklab:payload"));
    }

    private void verifyTwoListeners(boolean inputMode, boolean retire, int expectedCalls) {
        StandardJavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        SynapsePlayer player = player(inputMode);
        TestPlugin plugin = plugin();
        AtomicInteger calls = new AtomicInteger();
        byte[] original = {1, 2, 3};
        for (int index = 0; index < 2; index++) {
            messenger.registerIncomingPluginChannel(plugin, "networklab:payload", (sender, channel, payload) -> {
                assertArrayEquals(original, payload);
                payload[0] = 99;
                calls.incrementAndGet();
                if (retire) doReturn(false).when(player).isAcceptingInputPackets();
            });
        }
        messenger.dispatchIncomingMessage(player, "networklab:payload", original);
        assertEquals(expectedCalls, calls.get());
        assertArrayEquals(new byte[]{1, 2, 3}, original);
    }

    private SynapsePlayer player(boolean inputMode) {
        SynapsePlayer player = mock(SynapsePlayer.class);
        doReturn(inputMode).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isAcceptingInputPackets();
        return player;
    }

    private TestPlugin plugin() {
        TestPlugin plugin = new TestPlugin();
        plugin.setEnabled(true);
        return plugin;
    }

    private static final class TestPlugin extends PluginBase {}
}
