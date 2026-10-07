package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LockDifficulty implements NetEaseJsonEvent {
    public final String eventName = "LOCK_DIFFICULTY";
    public boolean lock;
}
