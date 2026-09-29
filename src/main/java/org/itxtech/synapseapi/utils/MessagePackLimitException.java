package org.itxtech.synapseapi.utils;

import java.io.IOException;

/** 资源预算超限与报文损坏分别处理，避免旧客户端的较大业务事件触发连接处罚。 */
public final class MessagePackLimitException extends IOException {
    public MessagePackLimitException(String message) {
        super(message);
    }
}
