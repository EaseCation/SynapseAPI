package org.itxtech.synapseapi.multiprotocol.common.netease;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetBlockDictTier {
    public Integer level;
    @JsonProperty("destroy_special")
    public Boolean destroySpecial;
    @Default
    public String digger = "";
}
