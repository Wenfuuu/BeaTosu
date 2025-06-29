package beat.osu.client.enums;

import lombok.Getter;

@Getter
public enum ScoreFilter {
    GLOBAL("Global"),
    LOCAL("Local");

    private final String scoreFilter;

    ScoreFilter(String scoreFilter) {
        this.scoreFilter = scoreFilter;
    }

    public static String[] getAllScoreFilters() {
        ScoreFilter[] values = values();
        String[] scoreFilters = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            scoreFilters[i] = values[i].getScoreFilter();
        }
        return scoreFilters;
    }
}
