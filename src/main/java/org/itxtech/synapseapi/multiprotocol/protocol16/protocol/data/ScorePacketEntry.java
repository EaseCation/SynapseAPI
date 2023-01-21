package org.itxtech.synapseapi.multiprotocol.protocol16.protocol.data;

import lombok.ToString;

import java.util.UUID;

@ToString
public class ScorePacketEntry {

    public UUID uuid;
    public String objectiveName;
    public int score;

}
