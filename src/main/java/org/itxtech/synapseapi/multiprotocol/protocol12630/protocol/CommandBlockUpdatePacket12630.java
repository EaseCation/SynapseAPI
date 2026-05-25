package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.CommandBlockUpdatePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.CommandBlockMode;
import lombok.ToString;

@ToString
public class CommandBlockUpdatePacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.COMMAND_BLOCK_UPDATE_PACKET;

    public boolean isBlock;

    public int x;
    public int y;
    public int z;
    public CommandBlockMode commandBlockMode;
    public boolean isRedstoneMode;
    public boolean isConditional;

    public long minecartEid;

    public String command;
    public String lastOutput;
    public String name;
    public String filteredName;
    public boolean shouldTrackOutput;
    public int tickDelay;
    public boolean executingOnFirstTick;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.isBlock = this.getUnsignedVarInt() == 1;
        if (this.isBlock) {
            BlockVector3 v = this.getBlockVector3();
            this.x = v.x;
            this.y = v.y;
            this.z = v.z;
            this.commandBlockMode = CommandBlockMode.getValues()[(int) this.getUnsignedVarInt()];
            this.isRedstoneMode = this.getBoolean();
            this.isConditional = this.getBoolean();
        } else {
            this.minecartEid = this.getEntityRuntimeId();
        }

        this.command = this.getString();
        this.lastOutput = this.getString();
        this.name = this.getString();
        this.filteredName = this.getString();
        this.shouldTrackOutput = this.getBoolean();
        this.tickDelay = this.getLInt();
        this.executingOnFirstTick = this.getBoolean();
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket toDefault() {
        CommandBlockUpdatePacket packet = new CommandBlockUpdatePacket();
        packet.isBlock = this.isBlock;
        packet.x = this.x;
        packet.y = this.y;
        packet.z = this.z;
        packet.commandBlockMode = this.commandBlockMode;
        packet.isRedstoneMode = this.isRedstoneMode;
        packet.isConditional = this.isConditional;
        packet.minecartEid = this.minecartEid;
        packet.command = this.command;
        packet.lastOutput = this.lastOutput;
        packet.name = this.name;
        packet.filteredName = this.filteredName;
        packet.shouldTrackOutput = this.shouldTrackOutput;
        packet.tickDelay = this.tickDelay;
        packet.executingOnFirstTick = this.executingOnFirstTick;
        return packet;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return CommandBlockUpdatePacket.class;
    }
}
