package beat.osu.shared.common;

import lombok.Getter;

import java.io.Serializable;

public class Error implements Serializable {

    @Getter
    private final String code;

    @Getter
    private final String message;

    public Error(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static Error network(String message) {
        return new Error("NETWORK_ERROR", message);
    }

    public static Error badRequest(String message) {
        return new Error("BAD_REQUEST", message);
    }

    public static Error notFound(String message) {
        return new Error("NOT_FOUND", message);
    }

    public static Error validation(String message) {
        return new Error("VALIDATION", message);
    }

    public static Error internal(String message) {
        return new Error("INTERNAL_ERROR", message);
    }

    public static Error unauthorized(String message) {
        return new Error("UNAUTHORIZED", message);
    }
}