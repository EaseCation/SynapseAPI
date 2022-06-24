package org.itxtech.synapseapi;

public final class SynapseSharedConstants {

    /**
     * 检查资源数据.
     */
    public static final boolean CHECK_RESOURCE_DATA = false;

    /**
     * Enables Movement Anti-Cheat debug message output?
     */
    public static final boolean MAC_DEBUG = false;

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
     * Enables new change dimension loading screen system?
     */
    public static final boolean USE_CHANGE_DIMENSION_PACKET = false;

    /**
     * 玩家所处的中心区块直接发送不使用 blob cache 以加快加载速度.
     */
    public static final boolean CENTER_CHUNK_WITHOUT_CACHE = true;

    private SynapseSharedConstants() {

    }
}
