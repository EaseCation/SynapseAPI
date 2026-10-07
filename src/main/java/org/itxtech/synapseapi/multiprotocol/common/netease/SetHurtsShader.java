package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetHurtsShader implements NetEaseJsonEvent {
    public final String eventName = "SET_HURTSHADER";
    public long entityId;
    public boolean val;
}
