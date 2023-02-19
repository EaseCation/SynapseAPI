package org.itxtech.synapseapi.multiprotocol.protocol116210;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.utils.*;
import org.itxtech.synapseapi.multiprotocol.protocol116200.BinaryStreamHelper116200;
import org.itxtech.synapseapi.multiprotocol.protocol116210.protocol.ItemStackRequestPacket116210;

import java.util.ArrayList;
import java.util.List;

public class BinaryStreamHelper116210 extends BinaryStreamHelper116200 {

    public static BinaryStreamHelper116210 create() {
        return new BinaryStreamHelper116210();
    }

    @Override
    public String getGameVersion() {
        return "1.16.210";
    }

    @Override
    public void putSkin(BinaryStream stream, Skin skin) {
        stream.putString(skin.getSkinId());
        stream.putString(skin.getPlayFabId());
        stream.putString(skin.getSkinResourcePatch());
        stream.putImage(skin.getSkinData());

        List<SkinAnimation> animations = skin.getAnimations();
        stream.putLInt(animations.size());
        for (SkinAnimation animation : animations) {
            stream.putImage(animation.image);
            stream.putLInt(animation.type);
            stream.putLFloat(animation.frames);
            stream.putLInt(animation.expression);
        }

        stream.putImage(skin.getCapeData());
        stream.putString(skin.getGeometryData());
        stream.putString(skin.getAnimationData());
        stream.putBoolean(skin.isPremium());
        stream.putBoolean(skin.isPersona());
        stream.putBoolean(skin.isCapeOnClassic());
        stream.putString(skin.getCapeId());
        stream.putString(skin.getFullSkinId());
        stream.putString(skin.getArmSize());
        stream.putString(skin.getSkinColor());
        List<PersonaPiece> pieces = skin.getPersonaPieces();
        stream.putLInt(pieces.size());
        for (PersonaPiece piece : pieces) {
            stream.putString(piece.id);
            stream.putString(piece.type);
            stream.putString(piece.packId);
            stream.putBoolean(piece.isDefault);
            stream.putString(piece.productId);
        }

        List<PersonaPieceTint> tints = skin.getTintColors();
        stream.putLInt(tints.size());
        for (PersonaPieceTint tint : tints) {
            stream.putString(tint.pieceType);
            List<String> colors = tint.colors;
            stream.putLInt(colors.size());
            for (String color : colors) {
                stream.putString(color);
            }
        }
    }

    @Override
    public Skin getSkin(BinaryStream stream) {
        Skin skin = new Skin();
        skin.setSkinId(stream.getString());
        skin.setPlayFabId(stream.getString());
        skin.setSkinResourcePatch(stream.getString());
        skin.setSkinData(stream.getImage());

        int animationCount = stream.getLInt();
        for (int i = 0; i < animationCount; i++) {
            SerializedImage image = stream.getImage();
            int type = stream.getLInt();
            float frames = stream.getLFloat();
            int expression = stream.getLInt();
            skin.getAnimations().add(new SkinAnimation(image, type, frames, expression));
        }

        skin.setCapeData(stream.getImage());
        skin.setGeometryData(stream.getString());
        skin.setAnimationData(stream.getString());
        skin.setPremium(stream.getBoolean());
        skin.setPersona(stream.getBoolean());
        skin.setCapeOnClassic(stream.getBoolean());
        skin.setCapeId(stream.getString());
        skin.setFullSkinId(stream.getString());
        skin.setArmSize(stream.getString());
        skin.setSkinColor(stream.getString());

        int piecesLength = stream.getLInt();
        for (int i = 0; i < piecesLength; i++) {
            String pieceId = stream.getString();
            String pieceType = stream.getString();
            String packId = stream.getString();
            boolean isDefault = stream.getBoolean();
            String productId = stream.getString();
            skin.getPersonaPieces().add(new PersonaPiece(pieceId, pieceType, packId, isDefault, productId));
        }

        int tintsLength = stream.getLInt();
        for (int i = 0; i < tintsLength; i++) {
            String pieceType = stream.getString();
            List<String> colors = new ArrayList<>();
            int colorsLength = stream.getLInt();
            for (int i2 = 0; i2 < colorsLength; i2++) {
                colors.add(stream.getString());
            }
            skin.getTintColors().add(new PersonaPieceTint(pieceType, colors));
        }
        return skin;
    }

    @Override
    protected Object getItemStackRequestAction(BinaryStream stream) {
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket116210.ACTION_TAKE:
                int count = stream.getByte();
                Object source = this.getItemStackRequestSlotInfo(stream);
                Object destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116210.ACTION_PLACE:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116210.ACTION_SWAP:
                source = this.getItemStackRequestSlotInfo(stream);
                destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116210.ACTION_DROP:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                boolean randomly = stream.getBoolean();
                break;
            case ItemStackRequestPacket116210.ACTION_DESTROY:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_CONSUME_INPUT:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT:
                int slot = stream.getByte();
                break;
            case ItemStackRequestPacket116210.ACTION_LAB_TABLE_COMBINE:
                break;
            case ItemStackRequestPacket116210.ACTION_BEACON_PAYMENT:
                int primaryEffect = stream.getVarInt();
                int secondaryEffect = stream.getVarInt();
                break;
            case ItemStackRequestPacket116210.ACTION_MINE_BLOCK:
                int hotbarSlot = stream.getVarInt();
                int predictedDurability = stream.getVarInt();
                int stackId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RECIPE:
                int recipeNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RECIPE_AUTO:
                recipeNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116210.ACTION_CREATIVE_CREATE:
                int creativeItemNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RECIPE_OPTIONAL:
                recipeNetworkId = stream.getVarInt();
                int filteredStringIndex = stream.getLInt();
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING:
                break;
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING:
                int length = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < length; i++) {
                    Item result = stream.getItemInstance();
                }
                int iterations = stream.getByte();
                break;
            default:
                throw new UnsupportedOperationException("Unhandled item stack request action type: " + type);
        }

        return null;
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 3;
        int ARG_TYPE_VALUE = 4;
        int ARG_TYPE_WILDCARD_INT = 5;
        int ARG_TYPE_OPERATOR = 6;
        int ARG_TYPE_TARGET = 7;
        int ARG_TYPE_WILDCARD_TARGET = 8;
        int ARG_TYPE_FILE_PATH = 16;
        int ARG_TYPE_STRING = 32;
        int ARG_TYPE_BLOCK_POSITION = 40;
        int ARG_TYPE_POSITION = 41;
        int ARG_TYPE_MESSAGE = 44;
        int ARG_TYPE_RAWTEXT = 46;
        int ARG_TYPE_JSON = 50;
        int ARG_TYPE_COMMAND = 63;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.WILDCARD_TARGET, ARG_TYPE_WILDCARD_TARGET);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_POSITION, ARG_TYPE_BLOCK_POSITION);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
        this.registerCommandParameterType(CommandParamType.FILE_PATH, ARG_TYPE_FILE_PATH);

        this.registerCommandParameterType(CommandParamType.COMPARE_OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.INTEGER_RANGE, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.EQUIPMENT_SLOT, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_STATES, ARG_TYPE_STRING);
    }
}
