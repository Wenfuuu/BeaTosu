package beat.osu.beatosu.enums;

import lombok.Getter;

public enum HitResult {
    PERFECT(300),
    GREAT(100),
    GOOD(50),
    MISS(0);

    @Getter
    private final int score;

    HitResult(int score) {
        this.score = score;
    }

    public static HitResult fromTimingError(long timingError) {
        long absError = Math.abs(timingError);
        if (absError <= 50) return PERFECT;
        if (absError <= 100) return GREAT;
        if (absError <= 150) return GOOD;
        return MISS;
    }
}
