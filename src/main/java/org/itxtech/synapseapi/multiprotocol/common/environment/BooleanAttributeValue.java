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
public class BooleanAttributeValue implements AttributeValue {
    public boolean value;
    public Operation operation;

    @Override
    public AttributeValueType getType() {
        return AttributeValueType.BOOLEAN;
    }

    public enum Operation {
        OVERRIDE("override"),
        ALPHA_BLEND("alpha_blend"),
        AND("and"),
        NAND("nand"),
        OR("or"),
        NOR("nor"),
        XOR("xor"),
        XNOR("xnor"),
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
