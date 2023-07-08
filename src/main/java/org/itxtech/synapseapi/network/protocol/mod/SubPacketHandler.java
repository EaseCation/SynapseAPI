package org.itxtech.synapseapi.network.protocol.mod;

public interface SubPacketHandler {
    void handle(StoreBuySuccessPacket packet);

    void handle(AnimationEmotePacket packet);

    void handle(DisplayEnvironmentPacket packet);

    void handle(RequestHudPacket packet);

    void handle(HudInteractPacket packet);

    void handle(HudButtonInteractPacket packet);

    void handle(ScreenInteractPacket packet);

    void handle(InputChangedPacket packet);

    void handle(FrameStatsPacket packet);

    void handle(GameInteractPacket packet);

    void handle(PathfindingResponsePacket packet);

    void handle(DimensionChangedPacket packet);

    void handle(ErrorReportPacket packet);

    void handle(ClientEnvironmentPacket packet);

    void handle(MagicReportPacket packet);

    void handle(MagicReportFailPacket packet);

    void handle(MagicDirectoryResultPacket packet);

    void handle(MagicMapResultPacket packet);

    void handle(MagicNodeUpdatedPacket packet);

    void handle(EncryptedPacket packet);
}
