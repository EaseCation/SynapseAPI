package org.itxtech.synapseapi.multiprotocol.protocol12010;

import cn.nukkit.command.data.CommandData;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.command.data.CommandOverload;
import cn.nukkit.command.data.CommandParamOption;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.PersonaPiece;
import cn.nukkit.utils.PersonaPieceTint;
import cn.nukkit.utils.SkinAnimation;
import org.apache.commons.lang3.RandomStringUtils;
import org.itxtech.synapseapi.multiprotocol.protocol120.BinaryStreamHelper120;
import org.itxtech.synapseapi.multiprotocol.protocol12010.protocol.AvailableCommandsPacket12010;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BinaryStreamHelper12010 extends BinaryStreamHelper120 {
    public static BinaryStreamHelper12010 create() {
        return new BinaryStreamHelper12010();
    }

    @Override
    public String getGameVersion() {
        return "1.20.10";
    }

    @Override
    public void putCommandData(BinaryStream stream, Map<String, CommandDataVersions> commands, List<CommandEnum> enums, List<String> postFixes, List<CommandEnum> softEnums) {
        stream.putUnsignedVarInt(commands.size());

        commands.forEach((name, cmdData) -> {
            CommandData data = cmdData.versions.get(0);

            stream.putString(name);
            stream.putString(data.description);
            int flags = 0;
            for (CommandFlag flag : data.flags) {
                flags |= 1 << flag.ordinal();
            }
            stream.putLShort(flags);
            stream.putByte((byte) data.permission.ordinal());

            stream.putLInt(data.aliases == null ? -1 : enums.indexOf(data.aliases));

            stream.putUnsignedVarInt(0); //TODO: chainedSubCommandData

            stream.putUnsignedVarInt(data.overloads.size());
            for (CommandOverload overload : data.overloads.values()) {
                stream.putBoolean(false); //TODO: overload.isChaining

                stream.putUnsignedVarInt(overload.input.parameters.length);
                for (CommandParameter parameter : overload.input.parameters) {
                    stream.putString(parameter.name);

                    int type = 0;
                    int translatedType = this.getCommandParameterTypeId(parameter.type, AvailableCommandsPacket12010.ARG_TYPE_INT);
                    if (parameter.postFix != null) {
                        int i = postFixes.indexOf(parameter.postFix);
                        if (i < 0) {
                            throw new IllegalStateException("Postfix '" + parameter.postFix + "' isn't in postfix array");
                        }
                        type = AvailableCommandsPacket12010.ARG_FLAG_POSTFIX | i;
                    } else {
                        type |= AvailableCommandsPacket12010.ARG_FLAG_VALID;
                        if (parameter.enumData != null) {
                            if (parameter.enumData.isSoft()) {
                                type |= AvailableCommandsPacket12010.ARG_FLAG_SOFT_ENUM | softEnums.indexOf(parameter.enumData);
                            } else {
                                type |= AvailableCommandsPacket12010.ARG_FLAG_ENUM | enums.indexOf(parameter.enumData);
                            }
                        } else {
                            type |= translatedType;
                        }
                    }

                    stream.putLInt(type);
                    stream.putBoolean(parameter.optional);
                    int options = 0;
                    for (CommandParamOption option : parameter.options) {
                        options |= 1 << option.ordinal();
                    }
                    stream.putByte((byte) options);
                }
            }
        });
    }

    @Override
    public void putSkin(BinaryStream stream, Skin skin) {
        if (!stream.neteaseMode) {
            super.putSkin(stream, skin);
            return;
        }

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
        stream.putString(skin.getGeometryDataEngineVersion());
        stream.putString(skin.getAnimationData());
        stream.putString(skin.getCapeId());
        //TODO: 中国版 1.20.10 ConfirmSkinPacket 导致其他玩家隐形问题的临时方案, 网易下个构建修复后恢复 -- 07/01/2024
        stream.putString(skin.getFullSkinId() + RandomStringUtils.random(8, 0, 0, true, true, null, ThreadLocalRandom.current()));
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

        stream.putBoolean(skin.isPremium());
        stream.putBoolean(skin.isPersona());
        stream.putBoolean(skin.isCapeOnClassic());
        stream.putBoolean(skin.isPrimaryUser());
        stream.putBoolean(skin.isOverridingPlayerAppearance());
    }
}
