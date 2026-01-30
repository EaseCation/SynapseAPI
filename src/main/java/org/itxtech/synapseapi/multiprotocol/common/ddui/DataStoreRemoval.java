package org.itxtech.synapseapi.multiprotocol.common.ddui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataStoreRemoval implements DataStoreAction {
    public String name;

    @Override
    public DataStoreActionType getActionType() {
        return DataStoreActionType.REMOVAL;
    }
}
