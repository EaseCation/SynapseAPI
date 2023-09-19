package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.*;
import cn.nukkit.item.Item;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

public class EntityMetadataGenerator {

	public static EntityMetadata generate14From(EntityMetadata v12Metadata) {
        EntityMetadata entityMetadata = new EntityMetadata();
        for (@SuppressWarnings("rawtypes") EntityData entityData : v12Metadata.getMap().values()) {
			if (entityData == null) {
				continue;
			}
        	int v12Id = entityData.getId();
			if (v12Id == Entity.DATA_NUKKIT_FLAGS) continue;
        	int v14Id = EntityDataItemIDTranslator.translateTo14Id(v12Id);
//        	if (v14Id == null) {
//        		MainLogger.getLogger().warning("Unable to translate to version 14 with id " + v12Id);
//        		continue;
//        	}
			if (v14Id == -1) {
				continue;
			}
        	if (entityData instanceof ByteEntityData) {
        		int data = entityData.getDataAsByte();
        		ByteEntityData byteEntityData = new ByteEntityData(v14Id, data);
        		entityMetadata.put(byteEntityData);
        	} else if(entityData instanceof FloatEntityData) {
        		float data = entityData.getDataAsFloat();
        		FloatEntityData floatEntityData = new FloatEntityData(v14Id, data);
        		entityMetadata.put(floatEntityData);
        	} else if(entityData instanceof IntEntityData) {
        		int data = entityData.getDataAsInt();
        		if (v14Id == EntityDataItemIDTranslator.VARIANT
						&& (v12Metadata.getLong(Entity.DATA_NUKKIT_FLAGS) & Entity.NUKKIT_FLAG_VARIANT_BLOCK) != 0) {
					int id = data >> Block.BLOCK_META_BITS;
					int meta = data & Block.BLOCK_META_MASK;
					data = GlobalBlockPalette.getOrCreateRuntimeId(id, meta);  //实体属性中的方块ID转换为RuntimeID
				}
        		IntEntityData intEntityData = new IntEntityData(v14Id, data);
        		entityMetadata.put(intEntityData);
        	} else if(entityData instanceof IntPositionEntityData) {
        		IntPositionEntityData intPositionEntityData = new IntPositionEntityData(v14Id, (IntPositionEntityData) entityData);
        		entityMetadata.put(intPositionEntityData);
        	} else if(entityData instanceof LongEntityData) {
        		long data = entityData.getDataAsLong();
				//DATA_FLAGS转换
				if (v14Id == EntityDataItemIDTranslator.FLAGS) {
					data = DataFlagTranslator.translate14(data);
				}
				LongEntityData longEntityData = new LongEntityData(v14Id, data);
        		entityMetadata.put(longEntityData);
        	} else if(entityData instanceof ShortEntityData) {
        		int data = entityData.getDataAsShort();
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
        		Vector3fEntityData vector3fEntityData = new Vector3fEntityData(v14Id, (Vector3fEntityData) entityData);
        		entityMetadata.put(vector3fEntityData);
        	}
        }
        return entityMetadata;
	}

	public static EntityMetadata generateFrom(EntityMetadata v12Metadata, AbstractProtocol protocol, boolean netease) {
		EntityMetadata entityMetadata = new EntityMetadata();
		boolean flagsTranslated = false;
		for (@SuppressWarnings("rawtypes") EntityData entityData : v12Metadata.getMap().values()) {
			if (entityData == null) {
				continue;
			}
			int v12Id = entityData.getId();
			if (v12Id == Entity.DATA_NUKKIT_FLAGS) continue;
			int newId = translateId(v12Id, protocol);
			if (newId == -1) {
				// discard
				continue;
			}
			if(entityData instanceof ByteEntityData) {
				int data = entityData.getDataAsByte();
				ByteEntityData byteEntityData = new ByteEntityData(newId, data);
				entityMetadata.put(byteEntityData);
			} else if(entityData instanceof FloatEntityData) {
				float data = entityData.getDataAsFloat();
				if (entityData.getId() == Entity.DATA_RIDER_MIN_ROTATION && data == 1 && protocol.ordinal() < AbstractProtocol.PROTOCOL_116_210.ordinal()) { // boat
					data = -90f;
				}
				FloatEntityData floatEntityData = new FloatEntityData(newId, data);
				entityMetadata.put(floatEntityData);
			} else if(entityData instanceof IntEntityData) {
				int data = entityData.getDataAsInt();
				if (newId == EntityDataItemIDTranslator.VARIANT
						&& (v12Metadata.getLong(Entity.DATA_NUKKIT_FLAGS) & Entity.NUKKIT_FLAG_VARIANT_BLOCK) != 0) {
					int id = data >> Block.BLOCK_META_BITS;
					int meta = data & Block.BLOCK_META_MASK;
					data = AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, id, meta);  //实体属性中的方块ID转换为RuntimeID
				} else if (entityData.getId() == Entity.DATA_AREA_EFFECT_CLOUD_PARTICLE_ID) {
					data = ParticleIdTranslator.translateTo(protocol, data);
//				} else if (entityData.getId() == Entity.DATA_HEARTBEAT_SOUND_EVENT) {
//					data = LevelSoundEventEnum.translateTo(protocol, data); //TODO
				}
				IntEntityData intEntityData = new IntEntityData(newId, data);
				entityMetadata.put(intEntityData);
			} else if(entityData instanceof IntPositionEntityData) {
				IntPositionEntityData intPositionEntityData = new IntPositionEntityData(newId, (IntPositionEntityData) entityData);
				entityMetadata.put(intPositionEntityData);
			} else if(entityData instanceof LongEntityData) {
				long data = entityData.getDataAsLong();
				//DATA_FLAGS转换
				boolean isFlags = false;
				int flags1Id = -1;
				int flags2Id = -1;
				long flags1 = -1;
				long flags2 = -1;
				if (v12Id == Entity.DATA_FLAGS) {
					if (flagsTranslated) {
						continue;
					}
					isFlags = true;
					flags1Id = newId;
					flags2Id = translateId(Entity.DATA_FLAGS_EXTENDED, protocol);
					flags1 = data;
					flags2 = v12Metadata.getLong(Entity.DATA_FLAGS_EXTENDED);
				} else if (v12Id == Entity.DATA_FLAGS_EXTENDED) {
					if (flagsTranslated) {
						continue;
					}
					isFlags = true;
					flags1Id = translateId(Entity.DATA_FLAGS, protocol);
					flags2Id = newId;
					flags1 = v12Metadata.getLong(Entity.DATA_FLAGS);
					flags2 = data;
				}
				if (isFlags) {
					long[] flags;
					if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_119_50.ordinal()) {
						flags = DataFlagTranslator.translate11950(flags1, flags2);
					} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_17.ordinal()) {
						flags = DataFlagTranslator.translate17(flags1, flags2);
					} else {
						data = DataFlagTranslator.translate14(flags1);
						flagsTranslated = true;
						entityMetadata.put(new LongEntityData(flags1Id, data));
						continue;
					}
					flagsTranslated = true;
					entityMetadata.put(new LongEntityData(flags1Id, flags[0]));
					if (flags2Id != -1) {
						entityMetadata.put(new LongEntityData(flags2Id, flags[1]));
					}
				} else {
					LongEntityData longEntityData = new LongEntityData(newId, data);
					entityMetadata.put(longEntityData);
				}
			} else if(entityData instanceof ShortEntityData) {
				int data = entityData.getDataAsShort();
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
				Vector3fEntityData vector3fEntityData = new Vector3fEntityData(newId, (Vector3fEntityData) entityData);
				entityMetadata.put(vector3fEntityData);
			}
		}
		return entityMetadata;
	}

	private static int translateId(int v12Id, AbstractProtocol protocol) {
		if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_119_40.ordinal()) {
			return EntityDataItemIDTranslator.translateTo11940Id(v12Id);
		} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_117.ordinal()) {
			return EntityDataItemIDTranslator.translateTo117Id(v12Id);
		} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_116_210.ordinal()) {
			return EntityDataItemIDTranslator.translateTo116210Id(v12Id);
		} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_112.ordinal()) {
			return EntityDataItemIDTranslator.translateTo112Id(v12Id);
		} else if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_111.ordinal()) {
			return EntityDataItemIDTranslator.translateTo111Id(v12Id);
		}
		return EntityDataItemIDTranslator.translateTo14Id(v12Id);
	}
}
