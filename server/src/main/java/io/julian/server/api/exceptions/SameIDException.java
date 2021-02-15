package io.julian.server.api.exceptions;

public class SameIDException extends Throwable {
    private final String id;

    public SameIDException(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Server already contains message with id '%s'", id);
    }
}
