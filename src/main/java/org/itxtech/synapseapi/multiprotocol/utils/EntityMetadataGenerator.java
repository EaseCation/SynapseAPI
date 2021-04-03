package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.entity.data.*;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

public class EntityMetadataGenerator {

	public static EntityMetadata generate14From(EntityMetadata v12Metadata) {
        EntityMetadata entityMetadata = new EntityMetadata();
        for(@SuppressWarnings("rawtypes") EntityData entityData : v12Metadata.getMap().values()) {
        	int v12Id = entityData.getId();
        	Integer v14Id = EntityDataItemIDTranslator.translateTo14Id(v12Id);
        	if(v14Id == null) {
        		MainLogger.getLogger().warning("Unable to translate to version 14 with id " + v12Id);
        		continue;
        	}
        	if(entityData instanceof ByteEntityData) {
        		Integer data = ((ByteEntityData)entityData).getData();
        		ByteEntityData byteEntityData = new ByteEntityData(v14Id, data);
        		entityMetadata.put(byteEntityData);
        	} else if(entityData instanceof FloatEntityData) {
        		Float data = ((FloatEntityData)entityData).getData();
        		FloatEntityData floatEntityData = new FloatEntityData(v14Id, data);
        		entityMetadata.put(floatEntityData);
        	} else if(entityData instanceof IntEntityData) {
        		Integer data = ((IntEntityData)entityData).getData();
        		if (v14Id == EntityDataItemIDTranslator.VARIANT) {
					int id = data & 0xff;
					int meta = data >> 8;
					data = GlobalBlockPalette.getOrCreateRuntimeId(id, meta);  //实体属性中的方块ID转换为RuntimeID
				}
        		IntEntityData intEntityData = new IntEntityData(v14Id, data);
        		entityMetadata.put(intEntityData);
        	} else if(entityData instanceof IntPositionEntityData) {
        		BlockVector3 data = ((IntPositionEntityData)entityData).getData();
        		IntPositionEntityData intPositionEntityData = new IntPositionEntityData(v14Id, data.getX(), data.getY(), data.getZ());
        		entityMetadata.put(intPositionEntityData);
        	} else if(entityData instanceof LongEntityData) {
        		Long data = ((LongEntityData)entityData).getData();
				//DATA_FLAGS转换
				if (v14Id == EntityDataItemIDTranslator.FLAGS) data = DataFlagTranslator.translate14(data);
				LongEntityData longEntityData = new LongEntityData(v14Id, data);
        		entityMetadata.put(longEntityData);
        	} else if(entityData instanceof ShortEntityData) {
        		Integer data = ((ShortEntityData)entityData).getData();
        		ShortEntityData shortEntityData = new ShortEntityData(v14Id, data);
        		entityMetadata.put(shortEntityData);
        	} else if(entityData instanceof SlotEntityData) {
        		Item data = ((SlotEntityData)entityData).getData();
        		SlotEntityData slotEntityData = new SlotEntityData(v14Id, data);
        		entityMetadata.put(slotEntityData);
        	} else if(entityData instanceof StringEntityData) {
        		String data = ((StringEntityData)entityData).getData();
        		StringEntityData stringEntityData = new StringEntityData(v14Id, data);
        		entityMetadata.put(stringEntityData);
        	} else if(entityData instanceof Vector3fEntityData) {
        		Vector3f data = ((Vector3fEntityData)entityData).getData();
        		Vector3fEntityData vector3fEntityData = new Vector3fEntityData(v14Id, data);
        		entityMetadata.put(vector3fEntityData);
        	}
        }
        return entityMetadata;
	}

	public static EntityMetadata generateFrom(EntityMetadata v12Metadata, AbstractProtocol protocol, boolean netease) {
		EntityMetadata entityMetadata = new EntityMetadata();
		for(@SuppressWarnings("rawtypes") EntityData entityData : v12Metadata.getMap().values()) {
			int v12Id = entityData.getId();
			Integer newId;
			if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_116_210.ordinal()) {
				newId = EntityDataItemIDTranslator.translateTo116210Id(v12Id);
			} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_112.ordinal()) {
				newId = EntityDataItemIDTranslator.translateTo112Id(v12Id);
			} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_111.ordinal()) {
				newId = EntityDataItemIDTranslator.translateTo111Id(v12Id);
			} else {
				newId = EntityDataItemIDTranslator.translateTo14Id(v12Id);
			}
			if (newId == null) {
				MainLogger.getLogger().warning("Unable to translate to version " + protocol.name() + " with id " + v12Id);
				//entityMetadata.put(entityData);
				continue;
			}
			if(entityData instanceof ByteEntityData) {
				Integer data = ((ByteEntityData)entityData).getData();
				ByteEntityData byteEntityData = new ByteEntityData(newId, data);
				entityMetadata.put(byteEntityData);
			} else if(entityData instanceof FloatEntityData) {
				Float data = ((FloatEntityData)entityData).getData();
				FloatEntityData floatEntityData = new FloatEntityData(newId, data);
				entityMetadata.put(floatEntityData);
			} else if(entityData instanceof IntEntityData) {
				Integer data = ((IntEntityData)entityData).getData();
				if (newId == EntityDataItemIDTranslator.VARIANT) {
					int id = data & 0xff;
					int meta = data >> 8;
					data = AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, id, meta);  //实体属性中的方块ID转换为RuntimeID
				}
				IntEntityData intEntityData = new IntEntityData(newId, data);
				entityMetadata.put(intEntityData);
			} else if(entityData instanceof IntPositionEntityData) {
				BlockVector3 data = ((IntPositionEntityData)entityData).getData();
				IntPositionEntityData intPositionEntityData = new IntPositionEntityData(newId, data.getX(), data.getY(), data.getZ());
				entityMetadata.put(intPositionEntityData);
			} else if(entityData instanceof LongEntityData) {
				Long data = ((LongEntityData)entityData).getData();
				//DATA_FLAGS转换
				if (newId == EntityDataItemIDTranslator.FLAGS) {
					if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_17.ordinal()) {
						data = DataFlagTranslator.translate17(data);
					} else {
						data = DataFlagTranslator.translate14(data);
					}
				}
				LongEntityData longEntityData = new LongEntityData(newId, data);
				entityMetadata.put(longEntityData);
			} else if(entityData instanceof ShortEntityData) {
				Integer data = ((ShortEntityData)entityData).getData();
				ShortEntityData shortEntityData = new ShortEntityData(newId, data);
				entityMetadata.put(shortEntityData);
			} else if(entityData instanceof SlotEntityData) {
				Item data = ((SlotEntityData)entityData).getData();
				if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_112.ordinal()) {
					//转换为1.12后的NBTEntityData
					NBTEntityData nbtEntityData = new NBTEntityData(newId, data.getNamedTag());
					entityMetadata.put(nbtEntityData);
				} else {
					SlotEntityData slotEntityData = new SlotEntityData(newId, data);
					entityMetadata.put(slotEntityData);
				}
			} else if(entityData instanceof StringEntityData) {
				String data = ((StringEntityData)entityData).getData();
				StringEntityData stringEntityData = new StringEntityData(newId, data);
				entityMetadata.put(stringEntityData);
			} else if(entityData instanceof Vector3fEntityData) {
				Vector3f data = ((Vector3fEntityData)entityData).getData();
				Vector3fEntityData vector3fEntityData = new Vector3fEntityData(newId, data);
				entityMetadata.put(vector3fEntityData);
			}
		}
		return entityMetadata;
	}

}
