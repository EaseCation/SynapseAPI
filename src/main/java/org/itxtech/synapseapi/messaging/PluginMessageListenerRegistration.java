package org.itxtech.synapseapi.messaging;

import cn.nukkit.plugin.Plugin;

public final class PluginMessageListenerRegistration {

    private final Messenger messenger;
    private final Plugin plugin;
    private final String channel;
    private final PluginMessageListener listener;

    public PluginMessageListenerRegistration(Messenger messenger, Plugin plugin, String channel, PluginMessageListener listener) {
        if (messenger == null) {
            throw new IllegalArgumentException("Messenger cannot be null!");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null!");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null!");
        }
        this.messenger = messenger;
        this.plugin = plugin;
        this.channel = channel;
        this.listener = listener;
    }

    public String getChannel() {
        return this.channel;
    }

    public PluginMessageListener getListener() {
        return this.listener;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public boolean isValid() {
        return this.messenger.isRegistrationValid(this);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PluginMessageListenerRegistration other = (PluginMessageListenerRegistration) obj;
        if (!(this.messenger == other.messenger || this.messenger.equals(other.messenger))) {
            return false;
        }
        if (!(this.plugin == other.plugin || this.plugin.equals(other.plugin))) {
            return false;
        }
        if (!this.channel.equals(other.channel)) {
            return false;
        }
        if (!(this.listener == other.listener || this.listener.equals(other.listener))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.messenger.hashCode();
        hash = 53 * hash + this.plugin.hashCode();
        hash = 53 * hash + this.channel.hashCode();
        hash = 53 * hash + this.listener.hashCode();
        return hash;
    }
}

