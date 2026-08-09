package org.itxtech.synapseapi.messaging.java;

import org.itxtech.synapseapi.SynapsePlayer;

/**
 * Java 客户端自定义载荷监听器。
 */
@FunctionalInterface
public interface JavaCustomPayloadListener {

    void onJavaCustomPayloadReceived(SynapsePlayer player, String channel, byte[] payload);
}
