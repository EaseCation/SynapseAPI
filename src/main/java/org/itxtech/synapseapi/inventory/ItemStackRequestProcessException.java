package org.itxtech.synapseapi.inventory;

public class ItemStackRequestProcessException extends RuntimeException {
    public ItemStackRequestProcessException(String message) {
        super(message);
    }

    public ItemStackRequestProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return this;
    }
}
