package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@Data
public class AddonUnmatch implements NetEaseJsonServerEvent {
    public final String eventName = "ADDON_UNMATCH";
}
