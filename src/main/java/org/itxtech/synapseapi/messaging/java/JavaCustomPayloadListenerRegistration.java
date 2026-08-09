package org.itxtech.synapseapi.messaging.java;

import cn.nukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Java 客户端自定义载荷监听器注册记录。
 */
public final class JavaCustomPayloadListenerRegistration {

    private final JavaCustomPayloadMessenger messenger;
    private final Plugin plugin;
    private final String channel;
    private final JavaCustomPayloadListener listener;

    JavaCustomPayloadListenerRegistration(final JavaCustomPayloadMessenger messenger, final Plugin plugin,
                                          final String channel, final JavaCustomPayloadListener listener) {
        this.messenger = Objects.requireNonNull(messenger, "messenger");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public String getChannel() {
        return this.channel;
    }

    public JavaCustomPayloadListener getListener() {
        return this.listener;
    }

    public boolean isValid() {
        return this.messenger.isRegistrationValid(this);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof JavaCustomPayloadListenerRegistration registration)) {
            return false;
        }
        return this.messenger.equals(registration.messenger)
                && this.plugin.equals(registration.plugin)
                && this.channel.equals(registration.channel)
                && this.listener.equals(registration.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.messenger, this.plugin, this.channel, this.listener);
    }
}
