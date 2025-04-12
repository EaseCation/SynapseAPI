package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.Data;

@Data
public class BiomeString {
    private String string;
    private char index;

    public BiomeString(String string) {
        this.string = string;
    }

    public BiomeString(char index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return String.valueOf(string);
    }
}
