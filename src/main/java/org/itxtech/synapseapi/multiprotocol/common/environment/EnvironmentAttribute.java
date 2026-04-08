package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.EasingType;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EnvironmentAttribute {
    static final EnvironmentAttribute[] EMPTY = new EnvironmentAttribute[0];

    public String name;
    @Nullable
    public AttributeValue fromAttribute;
    public AttributeValue attribute;
    @Nullable
    public AttributeValue toAttribute;
    public int currentTransitionTicks;
    public int totalTransitionTicks;
    public EasingType easing;
}
