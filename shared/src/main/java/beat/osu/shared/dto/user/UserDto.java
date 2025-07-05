package beat.osu.shared.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int BASE_EXP = 100000;
    private static final double EXP_GROWTH_RATE = 1.1;

    private int id;
    private String username;
    private String email;
    private String countryCode;
    private byte[] profilePicture;
    private int performance;
    private double accuracy;
    private int playCount;
    private int level;
    private int experience;
    private int rank;
    private boolean isSupporter;

    public static int getExpForLevel(int level) {
        return (int) Math.round(BASE_EXP * Math.pow(EXP_GROWTH_RATE, level - 1));
    }

    private boolean canLevelUp() {
        int requiredExp = getExpForLevel(level);
        return experience >= requiredExp;
    }

    private void levelUp() {
        if (canLevelUp()) {
            experience -= getExpForLevel(level);
            level++;
        }
    }

    private void updateLevel() {
        while (canLevelUp()) {
            levelUp();
        }
    }

    public void addExperience(int exp) {
        experience += exp;
        updateLevel();
    }

    public void updateAccuracy(double newAccuracy) {
        accuracy = (accuracy * playCount + newAccuracy) / (playCount + 1);
        playCount++;
    }
}