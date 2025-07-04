package org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data;

import cn.nukkit.block.BlockID;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ToString
public class PaletteBlockData {

    public static final PaletteBlockData AIR = new PaletteBlockData(BlockID.AIR, new LegacyStates[]{new LegacyStates(BlockID.AIR, 0)},
            new Block("minecraft:air", 0, Collections.emptyList()));

    @ToString
    public static class LegacyStates {
        public final int id;
        public final int val;

        public LegacyStates(int id, int val) {
            this.id = id;
            this.val = val;
        }
    }

    @ToString
    public static class Block {
        public String name;
        public int version;
        public List<Tag> states;

        public Block(String name, int version, List<Tag> states) {
            this.name = name;
            this.version = version;
            this.states = states;
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            tag.putInt("version", version);
            tag.putCompound("states", this.getStatesTag());
            return tag;
        }

        public CompoundTag getStatesTag() {
            CompoundTag states = new CompoundTag();
            for (Tag state : this.states) {
                states.put(state.getName(), state);
            }
            return states;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return Objects.equals(name, block.name) &&
                    states.containsAll(block.states);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version, states);
        }
    }

    public int id;
    public LegacyStates[] legacyStates;
    public Block block;
    public int legacyIndex;

    public PaletteBlockData(int id, LegacyStates[] legacyStates, Block block) {
        this(id, legacyStates, block, 0);
    }

    public PaletteBlockData(int id, LegacyStates[] legacyStates, Block block, int legacyIndex) {
        this.id = id;
        this.legacyStates = legacyStates;
        this.block = block;
        this.legacyIndex = legacyIndex;
    }

    private PaletteBlockData() {
    }

    public static PaletteBlockData createUnknownData() {
        return new PaletteBlockData();
    }
}
