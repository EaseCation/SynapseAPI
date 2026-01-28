package org.itxtech.synapseapi.multiprotocol.protocol112;

import cn.nukkit.math.Rotation;
import cn.nukkit.network.protocol.types.StructureBlockType;
import cn.nukkit.network.protocol.types.StructureEditorData;
import cn.nukkit.network.protocol.types.StructureMirror;
import cn.nukkit.network.protocol.types.StructureSettings;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol111.BinaryStreamHelper111;

public class BinaryStreamHelper112 extends BinaryStreamHelper111 {

    public static BinaryStreamHelper112 create() {
        return new BinaryStreamHelper112();
    }

    @Override
    public StructureEditorData getStructureEditorData(BinaryStream stream) {
        StructureEditorData data = new StructureEditorData();
        data.name = stream.getString();
        data.dataField = stream.getString();
        data.includePlayers = stream.getBoolean();
        data.boundingBoxVisible = stream.getBoolean();
        data.type = StructureBlockType.getValues()[stream.getVarInt()];
        data.settings = getStructureSettings(stream);
        return data;
    }

    @Override
    public StructureSettings getStructureSettings(BinaryStream stream) {
        StructureSettings settings = new StructureSettings();
        settings.paletteName = stream.getString();
        settings.ignoreEntities = stream.getBoolean();
        settings.ignoreBlocks = stream.getBoolean();
        settings.size = stream.getBlockVector3();
        settings.offset = stream.getBlockVector3();
        settings.lastEditedByEntityUniqueId = stream.getEntityUniqueId();
        settings.rotation = Rotation.getValues()[stream.getByte()];
        settings.mirror = StructureMirror.getValues()[stream.getByte()];
        settings.integrityValue = stream.getLFloat();
        settings.integritySeed = stream.getLInt();
        return settings;
    }
}
