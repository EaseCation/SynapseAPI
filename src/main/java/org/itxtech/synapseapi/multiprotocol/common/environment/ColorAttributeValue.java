package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ColorAttributeValue implements AttributeValue {
    public ColorData value;
    public Operation operation;

    @Override
    public AttributeValueType getType() {
        return AttributeValueType.COLOR;
    }

    public enum Operation {
        OVERRIDE("override"),
        ALPHA_BLEND("alpha_blend"),
        ADD("add"),
        SUBTRACT("subtract"),
        MULTIPLY("multiply"),
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
