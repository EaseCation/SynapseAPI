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
public class SetShearsDestorySpeed implements NetEaseJsonEvent {
    public static final int OPERATE_ADD = 0;
    public static final int OPERATE_REMOVE = 1;
    public static final int OPERATE_RESET = 2;

    public final String eventName = "SET_SHEARS_DESTORY_SPEED";
    public int operate;
    public float speed;
    @Default
    public String identifier = "";
}
