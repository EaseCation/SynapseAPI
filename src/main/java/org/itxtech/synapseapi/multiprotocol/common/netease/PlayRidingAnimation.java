package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlayRidingAnimation implements NetEaseJsonEvent {
    public final String eventName = "PLAY_RIDING_ANIMATION";
    public long entityId;
    @Default
    public String ridingAniName = "";
    public boolean ridingAniLoop;
}
