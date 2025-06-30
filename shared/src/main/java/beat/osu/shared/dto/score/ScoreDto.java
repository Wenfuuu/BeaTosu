package beat.osu.shared.dto.score;

import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private int beatmapId;
    private int userId;
    private int score;
    private int highestCombo;
    private double accuracy;
    private int perfectHit;
    private int gekiHit;
    private int greatHit;
    private int katuHit;
    private int goodHit;
    private int miss;
    private String grade;
    private LocalDateTime date;
    private String username;
    private byte[] profilePicture;
}
