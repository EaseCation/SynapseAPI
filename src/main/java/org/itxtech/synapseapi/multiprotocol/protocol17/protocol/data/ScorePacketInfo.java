package org.itxtech.synapseapi.multiprotocol.protocol17.protocol.data;

public class ScorePacketInfo {

    public static final int TYPE_PLAYER = 1;
    public static final int TYPE_ENTITY = 2;
    public static final int TYPE_DUMMY = 3;

    public long scoreboardId;
    public String objectiveName;
    public int score;
    public boolean isChangeType = true;
    public long entityId;
    public String fakePlayer;
    public byte addType;

}
