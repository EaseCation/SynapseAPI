package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LockGameType implements NetEaseJsonEvent {
    public final String eventName = "LOCK_GAME_TYPE";
    public boolean lock;
}
