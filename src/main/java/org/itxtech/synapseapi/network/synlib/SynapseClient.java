package org.itxtech.synapseapi.network.synlib;

import cn.nukkit.Server;
import cn.nukkit.utils.ThreadedLogger;
import com.nukkitx.network.util.Bootstraps;
import com.nukkitx.network.util.EventLoops;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.network.protocol.spp.SynapseDataPacket;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by boybook on 16/6/24.
 */
@Log4j2
public class SynapseClient extends Thread {

    public static final String VERSION = "0.3.0";

    public boolean needReconnect = false;
    protected ConcurrentLinkedQueue<SynapseDataPacket> externalQueue;
    protected ConcurrentLinkedQueue<SynapseDataPacket> internalQueue;
    private final ThreadedLogger logger;
    private final String interfaz;
    private final int port;
    private final AtomicBoolean shutdown;
    private final AtomicBoolean closing;
    private boolean needAuth = true;
    private boolean connected = false;
    private final Session session;

    public SynapseClient(ThreadedLogger logger, int port) {
        this(logger, port, "127.0.0.1");
    }

    public SynapseClient(ThreadedLogger logger, int port, String interfaz) {
        this.logger = logger;
        this.interfaz = interfaz;
        this.port = port;
        if (port < 1 || port > 65536) {
            throw new IllegalArgumentException("Invalid port range");
        }
        this.shutdown = new AtomicBoolean();
        this.closing = new AtomicBoolean();
        this.externalQueue = new ConcurrentLinkedQueue<>();
        this.internalQueue = new ConcurrentLinkedQueue<>();
        this.session = new Session(this);

        this.start();
    }

    public void reconnect() {
        this.needReconnect = true;
    }

    public boolean isNeedAuth() {
        return needAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public ConcurrentLinkedQueue<SynapseDataPacket> getExternalQueue() {
        return externalQueue;
    }

    public ConcurrentLinkedQueue<SynapseDataPacket> getInternalQueue() {
        return internalQueue;
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    public void shutdown() {
        this.shutdown.compareAndSet(false, true);
    }

    public boolean isClosing() {
        return closing.get();
    }

    public void markClosing() {
        this.closing.compareAndSet(false, true);
    }

    public int getPort() {
        return port;
    }

    public String getInterface() {
        return interfaz;
    }

    public ThreadedLogger getLogger() {
        return logger;
    }

    public void quit() {
        this.shutdown();
    }

    public void pushMainToThreadPacket(SynapseDataPacket data) {
        this.internalQueue.offer(data);
    }

    public SynapseDataPacket readMainToThreadPacket() {
        return this.internalQueue.poll();
    }

    public int getInternalQueueSize() {
        return this.internalQueue.size();
    }

    public void pushThreadToMainPacket(SynapseDataPacket data) {
        this.externalQueue.offer(data);
    }

    public SynapseDataPacket readThreadToMainPacket() {
        return this.externalQueue.poll();
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void run() {
        this.setName("SynLib Client Thread #" + Thread.currentThread().getId());
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
        try {
            this.connect();
            this.session.run();
        } catch (Exception e) {
            Server.getInstance().getLogger().logException(e);
        }
    }

    public boolean connect() {
        try {
            Bootstrap b = new Bootstrap();  //服务引导程序，服务器端快速启动程序
            b.option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT);
            Bootstraps.setupClientBootstrap(b);
            b.group(EventLoops.commonGroup())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new SynapseClientInitializer(this));

            b.connect(this.interfaz, this.port).get();
            // 等待服务端监听端口关闭，等待服务端链路关闭之后main函数才退出
            //future.channel().closeFuture().sync();
            return true;
        } catch (Exception e) {
            log.warn("Synapse Client can't connect to server: {}:{}", this.interfaz, this.port, e);
            log.warn("We will reconnect in 3 seconds");
            this.reconnect();
            return false;
        }
    }

    public class ShutdownHandler extends Thread {
        @Override
        public void run() {
            if (!shutdown.get()) {
                log.fatal("SynLib Client crashed!");
            }
        }
    }

}
