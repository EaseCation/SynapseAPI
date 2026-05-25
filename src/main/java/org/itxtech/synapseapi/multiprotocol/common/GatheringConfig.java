package org.itxtech.synapseapi.multiprotocol.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GatheringConfig {
    public UUID experienceId;
    public String experienceName;
    public UUID experienceWorldId;
    public String experienceWorldName;
    public String creatorId;
    /**
     * @deprecated 1.26.20
     */
    public String storeId;
    /**
     * @since 1.26.20
     */
    public UUID targetId;
    /**
     * @since 1.26.20
     */
    public String scenarioId;
    /**
     * @since 1.26.20
     */
    public String serverId;
}
