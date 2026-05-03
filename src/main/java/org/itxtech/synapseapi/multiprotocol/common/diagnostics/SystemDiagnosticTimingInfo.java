package org.itxtech.synapseapi.multiprotocol.common.diagnostics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SystemDiagnosticTimingInfo {
    public String displayName;
    public long systemIndex;
    public long timeInNS;
    public int percentOfTotal;
}
