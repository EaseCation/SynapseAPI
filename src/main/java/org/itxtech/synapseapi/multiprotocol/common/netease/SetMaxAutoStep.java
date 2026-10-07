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
public class SetMaxAutoStep implements NetEaseJsonEvent {
    public static final int RESET = -1;

    public final String eventName = "SET_MAX_AUTO_STEP";
    public long entityId;
    @Default
    public float value = 9 / 16f;
}
