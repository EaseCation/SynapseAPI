package org.itxtech.synapseapi.multiprotocol.common.diagnostics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EntityDiagnosticTimingInfo {
    public String displayName;
    public String entity;
    public long timeInNS;
    public int percentOfTotal;
}
