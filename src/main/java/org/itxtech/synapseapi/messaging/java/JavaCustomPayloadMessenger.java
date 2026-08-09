package org.itxtech.synapseapi.messaging.java;

import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapsePlayer;

import java.util.Set;

/**
 * Java 客户端自定义载荷的插件频道注册与分发接口。
 */
public interface JavaCustomPayloadMessenger {

    int MAX_CHANNEL_SIZE = 128;
    int MAX_MESSAGE_SIZE = 8 * 1024;
    String REGISTER_CHANNEL = "minecraft:register";
    String UNREGISTER_CHANNEL = "minecraft:unregister";

    boolean isReservedChannel(String channel);

    void registerOutgoingPluginChannel(Plugin plugin, String channel);

    void unregisterOutgoingPluginChannel(Plugin plugin, String channel);

    void unregisterOutgoingPluginChannel(Plugin plugin);

    JavaCustomPayloadListenerRegistration registerIncomingPluginChannel(Plugin plugin, String channel,
                                                                         JavaCustomPayloadListener listener);

    void unregisterIncomingPluginChannel(Plugin plugin, String channel, JavaCustomPayloadListener listener);

    void unregisterIncomingPluginChannel(Plugin plugin, String channel);

    void unregisterIncomingPluginChannel(Plugin plugin);

    Set<String> getOutgoingChannels();

    Set<String> getOutgoingChannels(Plugin plugin);

    Set<String> getIncomingChannels();

    Set<String> getIncomingChannels(Plugin plugin);

    Set<JavaCustomPayloadListenerRegistration> getIncomingChannelRegistrations(Plugin plugin);

    Set<JavaCustomPayloadListenerRegistration> getIncomingChannelRegistrations(String channel);

    Set<JavaCustomPayloadListenerRegistration> getIncomingChannelRegistrations(Plugin plugin, String channel);

    boolean isRegistrationValid(JavaCustomPayloadListenerRegistration registration);

    boolean isIncomingChannelRegistered(Plugin plugin, String channel);

    boolean isOutgoingChannelRegistered(Plugin plugin, String channel);

    boolean sendPluginMessage(Plugin source, SynapsePlayer player, String channel, byte[] payload);

    void dispatchIncomingMessage(SynapsePlayer player, String channel, byte[] payload);

    Set<String> getListeningChannels(SynapsePlayer player);

    void unregisterPlayerChannels(SynapsePlayer player);
}
