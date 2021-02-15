package io.julian.server.api.exceptions;

public class NoIDException extends Throwable {
    private final String id;

    public NoIDException(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Server does not contain message with id '%s'", id);
    }
}
