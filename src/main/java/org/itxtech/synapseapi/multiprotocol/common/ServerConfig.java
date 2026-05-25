package org.itxtech.synapseapi.multiprotocol.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ServerConfig {
    @Nullable
    public GatheringConfig gatheringConfig;
    /**
     * @since 1.26.20
     */
    @Nullable
    public StoreConfig storeConfig;
    /**
     * @since 1.26.20
     */
    @Nullable
    public PresenceConfig presenceConfig;
}
