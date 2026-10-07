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
public class LoginUid implements NetEaseJsonServerEvent {
    public final String eventName = "LOGIN_UID";
    @Default
    public String uid = "";
    @Default
    public String resid = "";
}
