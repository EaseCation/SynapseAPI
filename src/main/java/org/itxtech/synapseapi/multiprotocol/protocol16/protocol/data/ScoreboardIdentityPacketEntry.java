package org.itxtech.synapseapi.multiprotocol.protocol16.protocol.data;

import lombok.ToString;

import java.util.UUID;

@ToString
public class ScoreboardIdentityPacketEntry {

    public int scoreboardId;
    public UUID uuid;

}
