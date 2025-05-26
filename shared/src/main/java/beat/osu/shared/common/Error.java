package beat.osu.shared.common;

import lombok.Getter;

public class Error {

    @Getter
    private final String code;

    @Getter
    private final String message;

    public Error(String code, String message) {
        this.code = code;
        this.message = message;
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
}