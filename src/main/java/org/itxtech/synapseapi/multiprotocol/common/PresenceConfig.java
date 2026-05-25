package org.itxtech.synapseapi.multiprotocol.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PresenceConfig {
    /**
     * Name of the experience.
     */
    @Nullable
    public String experienceName;
    /**
     * Name of the world.
     */
    @Nullable
    public String worldName;
    /**
     * Optional rich presence ID to override client-driven rich presence.
     * @since 1.26.30
     */
    @Default
    public String richPresenceId = "";
}
