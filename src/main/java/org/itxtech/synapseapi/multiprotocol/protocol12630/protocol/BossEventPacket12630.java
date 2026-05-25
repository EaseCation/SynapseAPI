package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.BossEventPacket;
import cn.nukkit.network.protocol.BossEventPacket.BossBarColor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class BossEventPacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.BOSS_EVENT_PACKET;

    /* S2C: Shows the bossbar to the player. */
    public static final int TYPE_SHOW = 0;
    /* C2S: Registers a player to a boss fight. */
    public static final int TYPE_REGISTER_PLAYER = 1;
    /* S2C: Removes the bossbar from the client. */
    public static final int TYPE_HIDE = 2;
    /* C2S: Unregisters a player from a boss fight. */
    public static final int TYPE_UNREGISTER_PLAYER = 3;
    /* S2C: Appears not to be implemented. Currently bar percentage only appears to change in response to the target entity's health. */
    public static final int TYPE_HEALTH_PERCENT = 4;
    /* S2C: Also appears to not be implemented. Title clientside sticks as the target entity's nametag, or their entity type name if not set. */
    public static final int TYPE_TITLE = 5;
    /* S2C: Not sure on this. Includes color and overlay fields, plus an unknown short. */
    public static final int TYPE_UPDATE_PROPERTIES = 6;
    /* S2C: Sets color and overlay of the bar. */
    public static final int TYPE_TEXTURE = 7;
    /**
     * @since 1.18.10
     */
    /* C2S: Client asking the server to resend all boss data. */
    public static final int TYPE_QUERY = 8;

    public static final int OVERLAY_PROGRESS = 0;
    public static final int OVERLAY_NOTCHED_6 = 1;
    public static final int OVERLAY_NOTCHED_10 = 2;
    public static final int OVERLAY_NOTCHED_12 = 3;
    public static final int OVERLAY_NOTCHED_20 = 4;

    public long bossEid;
    public int type;
    public long playerEid;
    public float healthPercent;
    public String title = "";
    public String filteredTitle = "";
    public BossBarColor color = BossBarColor.PINK;
    public int overlay = OVERLAY_PROGRESS;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.bossEid = this.getEntityUniqueId();
        this.playerEid = this.getEntityUniqueId();
        this.type = this.getByte();
        this.title = this.getString();
        this.filteredTitle = this.getString();
        this.healthPercent = this.getLFloat();
        this.color = BossBarColor.getValues()[this.getByte()];
        this.overlay = this.getByte();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.bossEid);
        this.putEntityUniqueId(this.playerEid);
        this.putByte(this.type);
        this.putString(this.title);
        this.putString(this.filteredTitle);
        this.putLFloat(this.healthPercent);
        this.putByte(this.color.ordinal());
        this.putByte(this.overlay);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        BossEventPacket packet = (BossEventPacket) pk;
        this.bossEid = packet.bossEid;
        this.type = packet.type;
        this.playerEid = packet.playerEid;
        this.healthPercent = packet.healthPercent;
        this.title = packet.title;
        this.color = packet.color;
        this.overlay = packet.overlay;
        return this;
    }

    @Override
    public DataPacket toDefault() {
        BossEventPacket pk = new BossEventPacket();
        pk.bossEid = this.bossEid;
        pk.type = this.type;
        pk.playerEid = this.playerEid;
        pk.healthPercent = this.healthPercent;
        pk.title = this.title;
        pk.color = this.color;
        pk.overlay = this.overlay;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return BossEventPacket.class;
    }
}
