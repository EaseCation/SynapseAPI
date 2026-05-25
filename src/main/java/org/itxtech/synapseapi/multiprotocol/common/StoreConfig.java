package org.itxtech.synapseapi.multiprotocol.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StoreConfig {
    /**
     * The unique identifier for the store.
     */
    public String storeId;
    /**
     * The name of the store.
     */
    public String storeName;
}
