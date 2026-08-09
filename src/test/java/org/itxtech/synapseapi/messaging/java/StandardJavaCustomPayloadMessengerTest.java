package org.itxtech.synapseapi.messaging.java;

import cn.nukkit.plugin.PluginBase;
import org.itxtech.synapseapi.messaging.ChannelNotRegisteredException;
import org.itxtech.synapseapi.messaging.ReservedChannelException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardJavaCustomPayloadMessengerTest {

    @Test
    void requiresAnOutgoingRegistrationBeforeSending() {
        JavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        TestPlugin plugin = createPlugin(true);
        byte[] payload = new byte[]{1, 2};

        assertThrows(ChannelNotRegisteredException.class,
                () -> StandardJavaCustomPayloadMessenger.validatePluginMessage(messenger, plugin, "easecation:test", payload));

        messenger.registerOutgoingPluginChannel(plugin, "easecation:test");

        assertDoesNotThrow(() -> StandardJavaCustomPayloadMessenger.validatePluginMessage(messenger, plugin, "easecation:test", payload));
        assertTrue(messenger.isOutgoingChannelRegistered(plugin, "easecation:test"));
    }

    @Test
    void keepsIncomingAndOutgoingRegistrationsIndependentAndCleansThemTogether() {
        JavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        TestPlugin plugin = createPlugin(true);
        JavaCustomPayloadListener listener = (player, channel, payload) -> {
        };

        messenger.registerOutgoingPluginChannel(plugin, "easecation:test");
        JavaCustomPayloadListenerRegistration registration = messenger.registerIncomingPluginChannel(
                plugin, "easecation:test", listener);

        assertTrue(registration.isValid());
        assertEquals(Set.of("easecation:test"), messenger.getOutgoingChannels(plugin));
        assertEquals(Set.of("easecation:test"), messenger.getIncomingChannels(plugin));

        messenger.unregisterIncomingPluginChannel(plugin);
        messenger.unregisterOutgoingPluginChannel(plugin);

        assertFalse(registration.isValid());
        assertTrue(messenger.getOutgoingChannels(plugin).isEmpty());
        assertTrue(messenger.getIncomingChannels(plugin).isEmpty());
    }

    @Test
    void unregistersOnlyTheRequestedIncomingChannel() {
        JavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        TestPlugin plugin = createPlugin(true);
        JavaCustomPayloadListener listener = (player, channel, payload) -> {
        };

        messenger.registerIncomingPluginChannel(plugin, "easecation:first", listener);
        messenger.registerIncomingPluginChannel(plugin, "easecation:second", listener);

        messenger.unregisterIncomingPluginChannel(plugin, "easecation:first");

        assertFalse(messenger.isIncomingChannelRegistered(plugin, "easecation:first"));
        assertTrue(messenger.isIncomingChannelRegistered(plugin, "easecation:second"));
    }

    @Test
    void rejectsReservedChannelsAndOversizedPayloads() {
        JavaCustomPayloadMessenger messenger = new StandardJavaCustomPayloadMessenger();
        TestPlugin plugin = createPlugin(true);

        assertThrows(ReservedChannelException.class,
                () -> messenger.registerOutgoingPluginChannel(plugin, "minecraft:brand"));
        messenger.registerOutgoingPluginChannel(plugin, "easecation:test");
        assertThrows(IllegalArgumentException.class,
                () -> StandardJavaCustomPayloadMessenger.validatePluginMessage(messenger, plugin, "easecation:test",
                        new byte[JavaCustomPayloadEnvelope.MAX_PAYLOAD_BYTES + 1]));
    }

    @Test
    void parsesOnlyValidClientChannelAnnouncements() {
        byte[] payload = "easecation:launcher_commerce\0minecraft:brand\0invalid channel\0"
                .getBytes(StandardCharsets.UTF_8);

        Set<String> channels = StandardJavaCustomPayloadMessenger.parseControlChannels(payload);

        assertEquals(Set.of("easecation:launcher_commerce", "minecraft:brand"), channels);
    }

    private static TestPlugin createPlugin(final boolean enabled) {
        TestPlugin plugin = new TestPlugin();
        plugin.setEnabled(enabled);
        return plugin;
    }

    private static final class TestPlugin extends PluginBase {
    }
}
