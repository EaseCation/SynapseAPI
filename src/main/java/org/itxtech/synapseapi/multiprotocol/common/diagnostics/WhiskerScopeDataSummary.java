package org.itxtech.synapseapi.multiprotocol.common.diagnostics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WhiskerScopeDataSummary {
    public String label;
    public String indentation;
    public long totalHighCostNS;
    public long totalMidCostNS;
    public long totalLowCostNS;
}
