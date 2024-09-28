package org.itxtech.synapseapi;

public final class SynapseSharedConstants {
    /**
     * 是否启用化学附加包的内容.
     */
    public static final boolean ENABLE_CHEMISTRY_FEATURE = false;

    /**
     * 检查资源数据.
     */
    public static final boolean CHECK_RESOURCE_DATA = false;

    /**
     * 强制识别连接来自中国版客户端, 用于 ProxyPass 抓包调试.
     */
    public static final boolean FORCE_NETEASE_PLAYER = false;

    /**
     * Enables new inventory system.
     */
    public static final boolean SERVER_AUTHORITATIVE_INVENTORY = false; //WIP
    /**
     * Enables Movement Anti-Cheat?
     */
    public static final boolean SERVER_AUTHORITATIVE_MOVEMENT = true;
    /**
     * Enables Block Breaking Anti-Cheat? (Server Authoritative Movement is required)
     */
    public static final boolean SERVER_AUTHORITATIVE_BLOCK_BREAKING = true;
    /**
     * Enables Movement Anti-Cheat debug message output?
     */
    public static final boolean MAC_DEBUG = false;
    /**
     * 可接受的客户端tick差异范围
     */
    public static final long INPUT_TICK_ACCEPTANCE_THRESHOLD = 50; // 2.5s
    /**
     * 检测到客户端tick失去同步后进行下一次检测的冷却时间
     */
    public static final int INPUT_TICK_CHECK_CD = 20 * 8; // 8s
    /**
     * 服务端处于低TPS时检测到客户端tick失去同步后进行下一次检测的冷却时间
     */
    public static final int INPUT_TICK_OVERLOAD_CHECK_CD = 20 * 20; // 20s
    /**
     * 判定为变速前客户端tick连续失去同步的次数
     */
    public static final long INPUT_TICK_DESYNC_KICK_THRESHOLD = 5; // 连续超过 64s 尝试同步都失败
    /**
     * 客户端tick失去同步计数器降低等级的冷却时间
     */
    public static final int INPUT_TICK_DESYNC_FORGET_CD = 0b11111111; // 12.75s

    /**
     * 验证 1.16.220+ 的网络物品是否设置了有效的方块运行时 ID.
     */
    public static final boolean ITEM_BLOCK_DEBUG = false;

    /**
     * 1.12+ 是否使用客户端区块缓存?
     */
    public static final boolean USE_CLIENT_BLOB_CACHE = true;

    /**
     * 1.18+ 是否使用子区块请求?
     */
    public static final boolean USE_SUB_CHUNK_REQUEST = true;

    /**
     * 玩家所处的中心区块直接发送不使用 blob cache 以加快加载速度.
     */
    public static final boolean CENTER_CHUNK_WITHOUT_CACHE = true;

    /**
     * Enables outgoing packets logging?
     */
    public static final boolean CLIENTBOUND_PACKET_LOGGING = false;
    /**
     * Enables incoming packets logging?
     */
    public static final boolean SERVERBOUND_PACKET_LOGGING = false;

    /**
     * NetworkStackLatencyPacket ping pong.
     */
    public static final boolean NETWORK_STACK_LATENCY_TELEMETRY = true;
    /**
     * 客户端pong超时时间
     */
    public static final int PONG_TIMEOUT_TICKS = 20 * 5; // 5s
    /**
     * 判定为离线断开连接前客户端连续pong超时的次数
     */
    public static final long PONG_TIMEOUT_DISCONNECT_THRESHOLD = 3; // 连续超过 15s 均超时

    public static final boolean MUTE_EMOTE_CHAT = true;

    private SynapseSharedConstants() {

    }
}
