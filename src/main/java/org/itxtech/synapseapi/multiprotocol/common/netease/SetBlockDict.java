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
public class SetBlockDict implements NetEaseJsonEvent {
    public final String eventName = "SET_BLOCK_DICT";
    @Default
    public String blockName = "";
    @JsonProperty("auxVaule")
    public int auxValue;
    public SetBlockDictValue value;
}
