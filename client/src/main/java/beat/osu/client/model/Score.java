package beat.osu.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    private int id;
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
}
