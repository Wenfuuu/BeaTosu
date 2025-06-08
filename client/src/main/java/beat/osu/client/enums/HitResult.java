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

    public static HitResult fromTimingError(long timingError, double overallDifficulty) {
        long absError = Math.abs(timingError);
        if (absError < Math.round(80 - 6 * overallDifficulty)) return PERFECT;
        if (absError < Math.round(140 - 8 * overallDifficulty)) return GREAT;
        if (absError < Math.round(200 - 10 * overallDifficulty)) return GOOD;
        return MISS;
    }
}
