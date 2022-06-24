package org.itxtech.synapseapi.multiprotocol.protocol119;

import cn.nukkit.block.Block;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.LittleEndianByteBufOutputStream;
import cn.nukkit.network.protocol.types.InputInteractionModel;
import cn.nukkit.utils.BinaryStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.protocol11830.BinaryStreamHelper11830;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;

@Log4j2
public class BinaryStreamHelper119 extends BinaryStreamHelper11830 {
    public static BinaryStreamHelper119 create() {
        return new BinaryStreamHelper119();
    }

    @Override
    public String getGameVersion() {
        return "1.19.0";
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 3;
        int ARG_TYPE_VALUE = 4;
        int ARG_TYPE_WILDCARD_INT = 5;
        int ARG_TYPE_OPERATOR = 6;
        int ARG_TYPE_TARGET = 8;
        int ARG_TYPE_WILDCARD_TARGET = 10;
        int ARG_TYPE_FILE_PATH = 17;
        int ARG_TYPE_EQUIPMENT_SLOT = 38;
        int ARG_TYPE_STRING = 39;
        int ARG_TYPE_BLOCK_POSITION = 47;
        int ARG_TYPE_POSITION = 48;
        int ARG_TYPE_MESSAGE = 51;
        int ARG_TYPE_RAWTEXT = 53;
        int ARG_TYPE_JSON = 57;
        int ARG_TYPE_COMMAND = 70;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
    }

    @Override
    public InputInteractionModel getInteractionModel(BinaryStream stream) {
        return InputInteractionModel.getValues()[(int) stream.getUnsignedVarInt()];
    }

    @Override
    protected void putSlot(BinaryStream stream, Item item, boolean instanceItem) {
        if (item == null || item.getId() == Item.AIR) {
            stream.putByte((byte) 0);
            return;
        }

        int id = item.getId();
        int meta = item.getDamage();
        boolean isBlock = id < 256 && id != 166;
        boolean isDurable = item instanceof ItemDurable;

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, item);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        stream.putVarInt(networkId);
        stream.putLShort(item.getCount());

        boolean useLegacyData = !isBlock && !isDurable && !AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId);
        stream.putUnsignedVarInt(useLegacyData ? meta : 0);

        if (!instanceItem) {
            stream.putBoolean(true); // hasNetId
            stream.putVarInt(1); // netId
        }

        Block block = isBlock ? item.getBlockUnsafe() : null;
        int runtimeId = block == null ? 0 : AdvancedGlobalBlockPalette.getOrCreateRuntimeId(this.protocol, stream.neteaseMode, block.getId(), block.getDamage());
        stream.putVarInt(runtimeId);

        if (SynapseSharedConstants.ITEM_BLOCK_DEBUG) {
            if (block == null && isBlock) {
                Block expected = Item.get(id, meta).getBlockUnsafe();
                if (expected != null) {
                    log.warn("Invalid block given: {}\nExpected block: {}", item, expected);
                }
            }
        }

        ByteBuf userDataBuf = ByteBufAllocator.DEFAULT.ioBuffer();
        try (LittleEndianByteBufOutputStream out = new LittleEndianByteBufOutputStream(userDataBuf)) {
            if (!instanceItem && isDurable) {
                byte[] nbt = item.getCompoundTag();
                CompoundTag tag;
                if (nbt == null || nbt.length == 0) {
                    tag = new CompoundTag();
                } else {
                    tag = NBTIO.read(nbt, ByteOrder.LITTLE_ENDIAN);
                }
                if (tag.contains("Damage")) {
                    tag.put("__DamageConflict__", tag.removeAndGet("Damage"));
                }
                tag.putInt("Damage", meta);
                out.writeShort(-1);
                out.writeByte(1); // Hardcoded in current version
                out.write(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN));
            } else if (item.hasCompoundTag()) {
                out.writeShort(-1);
                out.writeByte(1); // Hardcoded in current version
                out.write(item.getCompoundTag());
            } else {
                userDataBuf.writeShortLE(0);
            }

            List<String> canPlaceOn = extractStringList(stream, item, "CanPlaceOn");
            out.writeInt(canPlaceOn.size());
            for (String string : canPlaceOn) {
                out.writeUTF(string);
            }

            List<String> canDestroy = extractStringList(stream, item, "CanDestroy");
            out.writeInt(canDestroy.size());
            for (String string : canDestroy) {
                out.writeUTF(string);
            }

            if (id == ItemID.SHIELD) {
                out.writeLong(0);
            }

            byte[] bytes = new byte[userDataBuf.readableBytes()];
            userDataBuf.readBytes(bytes);
            stream.putByteArray(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write item user data", e);
        } finally {
            userDataBuf.release();
        }
    }

}
