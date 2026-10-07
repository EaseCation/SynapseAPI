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
public class SetBlockDictValue {
    public Integer destroyTime;
    public Float explosionResistance;
    @Default
    public String loot = "";
    @Default
    public Boolean solid = true;
    public SetBlockDictTier tier;
}
