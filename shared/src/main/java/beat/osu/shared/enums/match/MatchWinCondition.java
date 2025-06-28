package beat.osu.shared.enums.match;

import lombok.Getter;

@Getter
public enum MatchWinCondition {
    SCORE("Score"),
    ACCURACY("Accuracy"),
    COMBO("Combo");

    private final String displayName;

    MatchWinCondition(String displayName) {
        this.displayName = displayName;
    }

    public static String[] getAllDisplayNames() {
        MatchWinCondition[] values = values();
        String[] displayNames = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            displayNames[i] = values[i].getDisplayName();
        }
        return displayNames;
    }

    public static MatchWinCondition fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("MatchWinCondition value cannot be null");
        }
        
        String trimmedValue = value.trim();
        
        for (MatchWinCondition condition : values()) {
            if (condition.getDisplayName().equalsIgnoreCase(trimmedValue)) {
                return condition;
            }
        }
        
        return MatchWinCondition.valueOf(trimmedValue.toUpperCase());
    }
}
