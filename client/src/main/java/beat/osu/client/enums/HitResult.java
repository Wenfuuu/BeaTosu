package beat.osu.client.enums;

import lombok.Getter;

@Getter
public enum HitResult {
    PERFECT(300),
    GREAT(100),
    GOOD(50),
    MISS(0);

    private final int score;

    HitResult(int score) {
        this.score = score;
    }

    public static HitResult fromTimingError(long timingError) {
        long absError = Math.abs(timingError);
        if (absError <= 100) return PERFECT;
        if (absError <= 200) return GREAT;
        if (absError <= 300) return GOOD;
        return MISS;
    }
}
