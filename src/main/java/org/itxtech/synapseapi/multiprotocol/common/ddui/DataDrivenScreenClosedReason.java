package org.itxtech.synapseapi.multiprotocol.common.ddui;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DataDrivenScreenClosedReason {
    PROGRAMMATIC_CLOSE("programmaticclose"),
    PROGRAMMATIC_CLOSE_ALL("programmaticcloseall"),
    CLIENT_CANCELED("clientcanceled"),
    USER_BUSY("userbusy"),
    INVALID_FORM("invalidform"),
    ;

    private static final Map<String, DataDrivenScreenClosedReason> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(DataDrivenScreenClosedReason::getName, Function.identity()));

    private final String name;

    DataDrivenScreenClosedReason(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static DataDrivenScreenClosedReason byName(String name) {
        return BY_NAME.get(name);
    }
}
