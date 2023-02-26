package org.itxtech.synapseapi.multiprotocol.protocol11963;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.PersonaPiece;
import cn.nukkit.utils.PersonaPieceTint;
import cn.nukkit.utils.SerializedImage;
import cn.nukkit.utils.SkinAnimation;
import org.itxtech.synapseapi.multiprotocol.protocol11960.BinaryStreamHelper11960;

import java.util.ArrayList;
import java.util.List;

public class BinaryStreamHelper11963 extends BinaryStreamHelper11960 {
    public static BinaryStreamHelper11963 create() {
        return new BinaryStreamHelper11963();
    }

    @Override
    public String getGameVersion() {
        return "1.19.63";
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
        stream.putString(skin.getGeometryDataEngineVersion());
        stream.putString(skin.getAnimationData());
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

        stream.putBoolean(skin.isPremium());
        stream.putBoolean(skin.isPersona());
        stream.putBoolean(skin.isCapeOnClassic());
        stream.putBoolean(skin.isPrimaryUser());
        stream.putBoolean(skin.isOverridingPlayerAppearance());
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
        skin.setGeometryDataEngineVersion(stream.getString());
        skin.setAnimationData(stream.getString());
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

        skin.setPremium(stream.getBoolean());
        skin.setPersona(stream.getBoolean());
        skin.setCapeOnClassic(stream.getBoolean());
        skin.setPrimaryUser(stream.getBoolean());
        skin.setOverridingPlayerAppearance(stream.getBoolean());
        return skin;
    }
}
