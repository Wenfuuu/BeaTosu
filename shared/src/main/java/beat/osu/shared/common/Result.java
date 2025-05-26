package beat.osu.shared.common;

import lombok.Getter;

public class Result<T> {

    @Getter
    private final boolean success;
    private final T value;
    private final Error error;

    public static <T> Result<T> success(T value) {
        return new Result<>(true, value, null);
    }

    public static <T> Result<T> failure(Error error) {
        return new Result<>(false, null, error);
    }

    public Result(boolean success, T value, Error error) {
        this.success = success;
        this.value = value;
        this.error = error;
    }

    public T getValue() {
        if (!success) {
            throw new IllegalStateException("Cannot get value from failed result");
        }
        return value;
    }

    public Error getError() {
        if (success) {
            throw new IllegalStateException("Cannot get error from successful result");
        }
        return error;
    }
}
