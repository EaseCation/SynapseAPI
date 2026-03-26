package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FloatAttributeValue implements AttributeValue {
    public float value;
    public Operation operation;
    @Nullable
    public Float constraintMin;
    @Nullable
    public Float constraintMax;

    @Override
    public AttributeValueType getType() {
        return AttributeValueType.FLOAT;
    }

    public enum Operation {
        OVERRIDE("override"),
        ALPHA_BLEND("alpha_blend"),
        ADD("add"),
        SUBTRACT("subtract"),
        MULTIPLY("multiply"),
        MINIMUM("minimum"),
        MAXIMUM("maximum"),
        ;

        private static final Map<String, Operation> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toMap(Operation::getName, Function.identity()));

        private final String name;

        Operation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
