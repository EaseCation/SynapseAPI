package org.itxtech.synapseapi.network.protocol.mod;

public interface ServerSubPacketHandler extends SubPacketHandler {
    default void handle(StoreBuySuccessPacket packet) {
    }

    default void handle(AnimationEmotePacket packet) {
    }

    default void handle(DisplayEnvironmentPacket packet) {
    }

    default void handle(RequestHudPacket packet) {
    }

    default void handle(HudInteractPacket packet) {
    }

    default void handle(HudButtonInteractPacket packet) {
    }

    default void handle(ScreenInteractPacket packet) {
    }

    default void handle(InputChangedPacket packet) {
    }

    default void handle(FrameStatsPacket packet) {
    }

    default void handle(GameInteractPacket packet) {
    }

    default void handle(PathfindingResponsePacket packet) {
    }

    default void handle(DimensionChangedPacket packet) {
    }

    default void handle(ErrorReportPacket packet) {
    }

    default void handle(ClientEnvironmentPacket packet) {
    }

    default void handle(MagicReportPacket packet) {
    }

    default void handle(MagicReportFailPacket packet) {
    }

    default void handle(MagicDirectoryResultPacket packet) {
    }

    default void handle(MagicMapResultPacket packet) {
    }

    default void handle(MagicNodeUpdatedPacket packet) {
    }

    default void handle(EncryptedPacket packet) {
        throw new IllegalStateException("clientbound only");
    }
}
