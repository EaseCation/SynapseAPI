package org.itxtech.synapseapi.messaging.java;

import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.messaging.ChannelNotRegisteredException;
import org.itxtech.synapseapi.messaging.ReservedChannelException;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ScriptMessagePacket11810;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Java 客户端自定义载荷的标准插件频道注册与分发实现。
 */
public final class StandardJavaCustomPayloadMessenger implements JavaCustomPayloadMessenger {

    private final ConcurrentMap<String, Set<Plugin>> outgoingByChannel = new ConcurrentHashMap<>();
    private final ConcurrentMap<Plugin, Set<String>> outgoingByPlugin = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<JavaCustomPayloadListenerRegistration>> incomingByChannel = new ConcurrentHashMap<>();
    private final ConcurrentMap<Plugin, Set<JavaCustomPayloadListenerRegistration>> incomingByPlugin = new ConcurrentHashMap<>();
    private final ConcurrentMap<SynapsePlayer, Set<String>> listeningChannelsByPlayer = new ConcurrentHashMap<>();

    @Override
    public void registerOutgoingPluginChannel(final Plugin plugin, final String channel) {
        validatePlugin(plugin);
        validatePluginChannel(channel);
        this.outgoingByChannel.computeIfAbsent(channel, ignored -> ConcurrentHashMap.newKeySet()).add(plugin);
        this.outgoingByPlugin.computeIfAbsent(plugin, ignored -> ConcurrentHashMap.newKeySet()).add(channel);
    }

    @Override
    public void unregisterOutgoingPluginChannel(final Plugin plugin, final String channel) {
        validatePlugin(plugin);
        validateChannel(channel);
        removeOutgoing(plugin, channel);
    }

    @Override
    public JavaCustomPayloadListenerRegistration registerIncomingPluginChannel(final Plugin plugin, final String channel,
                                                                                final JavaCustomPayloadListener listener) {
        validatePlugin(plugin);
        validatePluginChannel(channel);
        Objects.requireNonNull(listener, "listener");

        JavaCustomPayloadListenerRegistration registration = new JavaCustomPayloadListenerRegistration(this, plugin, channel, listener);
        Set<JavaCustomPayloadListenerRegistration> channelRegistrations = this.incomingByChannel
                .computeIfAbsent(channel, ignored -> ConcurrentHashMap.newKeySet());
        if (!channelRegistrations.add(registration)) {
            throw new IllegalArgumentException("This Java custom payload listener registration already exists");
        }
        this.incomingByPlugin.computeIfAbsent(plugin, ignored -> ConcurrentHashMap.newKeySet()).add(registration);
        return registration;
    }

    @Override
    public void unregisterIncomingPluginChannel(final Plugin plugin, final String channel,
                                                final JavaCustomPayloadListener listener) {
        validatePlugin(plugin);
        validateChannel(channel);
        Objects.requireNonNull(listener, "listener");
        removeIncoming(new JavaCustomPayloadListenerRegistration(this, plugin, channel, listener));
    }

    @Override
    public void unregisterIncomingPluginChannel(final Plugin plugin, final String channel) {
        validatePlugin(plugin);
        validateChannel(channel);
        Set<JavaCustomPayloadListenerRegistration> registrations = getIncomingChannelRegistrations(plugin, channel);
        for (JavaCustomPayloadListenerRegistration registration : registrations) {
            removeIncoming(registration);
        }
    }

    @Override
    public void unregisterOutgoingPluginChannel(final Plugin plugin) {
        validatePlugin(plugin);

        Set<String> outgoingChannels = this.outgoingByPlugin.remove(plugin);
        if (outgoingChannels != null) {
            for (String channel : outgoingChannels) {
                Set<Plugin> plugins = this.outgoingByChannel.get(channel);
                if (plugins != null) {
                    plugins.remove(plugin);
                    if (plugins.isEmpty()) {
                        this.outgoingByChannel.remove(channel, plugins);
                    }
                }
            }
        }

    }

    @Override
    public void unregisterIncomingPluginChannel(final Plugin plugin) {
        validatePlugin(plugin);

        Set<JavaCustomPayloadListenerRegistration> incomingRegistrations = this.incomingByPlugin.remove(plugin);
        if (incomingRegistrations != null) {
            for (JavaCustomPayloadListenerRegistration registration : incomingRegistrations) {
                Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByChannel.get(registration.getChannel());
                if (registrations != null) {
                    registrations.remove(registration);
                    if (registrations.isEmpty()) {
                        this.incomingByChannel.remove(registration.getChannel(), registrations);
                    }
                }
            }
        }
    }

    @Override
    public boolean isOutgoingChannelRegistered(final Plugin plugin, final String channel) {
        validatePlugin(plugin);
        validateChannel(channel);
        Set<String> channels = this.outgoingByPlugin.get(plugin);
        return channels != null && channels.contains(channel);
    }

    @Override
    public boolean isIncomingChannelRegistered(final Plugin plugin, final String channel) {
        validatePlugin(plugin);
        validateChannel(channel);
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByPlugin.get(plugin);
        if (registrations == null) {
            return false;
        }
        for (JavaCustomPayloadListenerRegistration registration : registrations) {
            if (registration.getChannel().equals(channel)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getOutgoingChannels() {
        return Set.copyOf(this.outgoingByChannel.keySet());
    }

    @Override
    public Set<String> getOutgoingChannels(final Plugin plugin) {
        validatePlugin(plugin);
        Set<String> channels = this.outgoingByPlugin.get(plugin);
        return channels == null ? Collections.emptySet() : Set.copyOf(channels);
    }

    @Override
    public Set<String> getIncomingChannels() {
        return Set.copyOf(this.incomingByChannel.keySet());
    }

    @Override
    public Set<String> getIncomingChannels(final Plugin plugin) {
        validatePlugin(plugin);
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByPlugin.get(plugin);
        if (registrations == null || registrations.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> channels = new HashSet<>();
        for (JavaCustomPayloadListenerRegistration registration : registrations) {
            channels.add(registration.getChannel());
        }
        return Set.copyOf(channels);
    }

    @Override
    public Set<JavaCustomPayloadListenerRegistration> getIncomingChannelRegistrations(final Plugin plugin) {
        validatePlugin(plugin);
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByPlugin.get(plugin);
        return registrations == null ? Collections.emptySet() : Set.copyOf(registrations);
    }

    @Override
    public Set<JavaCustomPayloadListenerRegistration> getIncomingChannelRegistrations(final String channel) {
        validateChannel(channel);
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByChannel.get(channel);
        return registrations == null ? Collections.emptySet() : Set.copyOf(registrations);
    }

    @Override
    public Set<JavaCustomPayloadListenerRegistration> getIncomingChannelRegistrations(final Plugin plugin, final String channel) {
        validatePlugin(plugin);
        validateChannel(channel);
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByPlugin.get(plugin);
        if (registrations == null || registrations.isEmpty()) {
            return Collections.emptySet();
        }
        Set<JavaCustomPayloadListenerRegistration> matchingRegistrations = new HashSet<>();
        for (JavaCustomPayloadListenerRegistration registration : registrations) {
            if (channel.equals(registration.getChannel())) {
                matchingRegistrations.add(registration);
            }
        }
        return Set.copyOf(matchingRegistrations);
    }

    @Override
    public boolean isRegistrationValid(final JavaCustomPayloadListenerRegistration registration) {
        Objects.requireNonNull(registration, "registration");
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByPlugin.get(registration.getPlugin());
        return registrations != null && registrations.contains(registration);
    }

    @Override
    public Set<String> getListeningChannels(final SynapsePlayer player) {
        Objects.requireNonNull(player, "player");
        Set<String> channels = this.listeningChannelsByPlayer.get(player);
        return channels == null ? Collections.emptySet() : Set.copyOf(channels);
    }

    @Override
    public boolean sendPluginMessage(final Plugin source, final SynapsePlayer player, final String channel, final byte[] payload) {
        validatePluginMessage(this, source, channel, payload);
        if (!player.isOnline() || !player.isJavaClient()) {
            return false;
        }
        Set<String> channels = this.listeningChannelsByPlayer.get(player);
        if (channels == null || !channels.contains(channel)) {
            return false;
        }
        return sendToClient(player, channel, payload);
    }

    /**
     * 处理已由底层信封校验过的客户端载荷。
     */
    @Override
    public void dispatchIncomingMessage(final SynapsePlayer player, final String channel, final byte[] payload) {
        if (player == null || channel == null || payload == null || payload.length > JavaCustomPayloadEnvelope.MAX_PAYLOAD_BYTES) {
            return;
        }
        if (JavaCustomPayloadMessenger.REGISTER_CHANNEL.equals(channel)) {
            this.listeningChannelsByPlayer.computeIfAbsent(player, ignored -> ConcurrentHashMap.newKeySet())
                    .addAll(parseControlChannels(payload));
            return;
        }
        if (JavaCustomPayloadMessenger.UNREGISTER_CHANNEL.equals(channel)) {
            Set<String> channels = this.listeningChannelsByPlayer.get(player);
            if (channels != null) {
                channels.removeAll(parseControlChannels(payload));
                if (channels.isEmpty()) {
                    this.listeningChannelsByPlayer.remove(player, channels);
                }
            }
            return;
        }
        if (!JavaCustomPayloadEnvelope.isValidChannel(channel)) {
            return;
        }

        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByChannel.get(channel);
        if (registrations == null) {
            return;
        }
        for (JavaCustomPayloadListenerRegistration registration : registrations) {
            Plugin plugin = registration.getPlugin();
            if (!plugin.isEnabled()) {
                unregisterIncomingPluginChannel(plugin);
                unregisterOutgoingPluginChannel(plugin);
                continue;
            }
            try {
                registration.getListener().onJavaCustomPayloadReceived(player, channel, payload.clone());
            } catch (Throwable throwable) {
                SynapseAPI.getInstance().getLogger().warning("Could not pass incoming Java custom payload", throwable);
            }
        }
    }

    public static void validatePluginMessage(final JavaCustomPayloadMessenger messenger, final Plugin source,
                                             final String channel, final byte[] payload) {
        Objects.requireNonNull(messenger, "messenger");
        validatePlugin(source);
        if (!source.isEnabled()) {
            throw new IllegalArgumentException("Plugin must be enabled to send Java custom payloads");
        }
        validateChannel(channel);
        if (!messenger.isOutgoingChannelRegistered(source, channel)) {
            throw new ChannelNotRegisteredException(channel);
        }
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (payload.length > JavaCustomPayloadEnvelope.MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Payload exceeds the Java custom payload size limit");
        }
    }

    static Set<String> parseControlChannels(final byte[] payload) {
        if (payload == null || payload.length > JavaCustomPayloadEnvelope.MAX_PAYLOAD_BYTES) {
            return Collections.emptySet();
        }
        String value = new String(payload, StandardCharsets.UTF_8);
        Set<String> channels = new HashSet<>();
        for (String channel : value.split("\0", -1)) {
            if (JavaCustomPayloadEnvelope.isValidChannel(channel)) {
                channels.add(channel);
            }
        }
        return Set.copyOf(channels);
    }

    @Override
    public void unregisterPlayerChannels(final SynapsePlayer player) {
        if (player != null) {
            this.listeningChannelsByPlayer.remove(player);
        }
    }

    private static void validatePlugin(final Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
    }

    private static void validatePluginChannel(final String channel) {
        validateChannel(channel);
        if (channel.startsWith("minecraft:")) {
            throw new ReservedChannelException(channel);
        }
    }

    private static void validateChannel(final String channel) {
        if (!JavaCustomPayloadEnvelope.isValidChannel(channel)) {
            throw new IllegalArgumentException("Invalid Java custom payload channel");
        }
    }

    @Override
    public boolean isReservedChannel(final String channel) {
        validateChannel(channel);
        return channel.startsWith("minecraft:");
    }

    /**
     * 将已通过插件与客户端频道校验的载荷封装为 ScriptMessage 并发送。
     */
    private static boolean sendToClient(final SynapsePlayer player, final String channel, final byte[] payload) {
        Optional<String> encoded = JavaCustomPayloadEnvelope.encode(channel, payload);
        if (encoded.isEmpty()) {
            return false;
        }
        ScriptMessagePacket11810 packet = new ScriptMessagePacket11810();
        packet.messageId = JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID;
        packet.value = encoded.get();
        return player.dataPacket(packet);
    }

    private void removeOutgoing(final Plugin plugin, final String channel) {
        Set<Plugin> plugins = this.outgoingByChannel.get(channel);
        if (plugins != null) {
            plugins.remove(plugin);
            if (plugins.isEmpty()) {
                this.outgoingByChannel.remove(channel, plugins);
            }
        }
        Set<String> channels = this.outgoingByPlugin.get(plugin);
        if (channels != null) {
            channels.remove(channel);
            if (channels.isEmpty()) {
                this.outgoingByPlugin.remove(plugin, channels);
            }
        }
    }

    private void removeIncoming(final JavaCustomPayloadListenerRegistration registration) {
        Set<JavaCustomPayloadListenerRegistration> registrations = this.incomingByChannel.get(registration.getChannel());
        if (registrations != null) {
            registrations.remove(registration);
            if (registrations.isEmpty()) {
                this.incomingByChannel.remove(registration.getChannel(), registrations);
            }
        }
        Set<JavaCustomPayloadListenerRegistration> pluginRegistrations = this.incomingByPlugin.get(registration.getPlugin());
        if (pluginRegistrations != null) {
            pluginRegistrations.remove(registration);
            if (pluginRegistrations.isEmpty()) {
                this.incomingByPlugin.remove(registration.getPlugin(), pluginRegistrations);
            }
        }
    }
}
