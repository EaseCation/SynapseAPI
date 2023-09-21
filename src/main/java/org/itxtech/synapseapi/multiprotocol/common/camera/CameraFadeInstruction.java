package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.utils.BlockColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraFadeInstruction {
    @Nullable
    public TimeData time;
    @Nullable
    public BlockColor color;

    public CameraFadeInstruction(TimeData time) {
        this.time = time;
    }

    public CameraFadeInstruction(BlockColor color) {
        this.color = color;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class TimeData {
        public float fadeInTime;
        public float stayTime;
        public float fadeOutTime;
    }
}
