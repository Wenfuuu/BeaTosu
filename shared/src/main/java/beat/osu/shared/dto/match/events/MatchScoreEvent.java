package beat.osu.shared.dto.match.events;

import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MatchScoreEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private int score;
    private int highestCombo;
    private int combo;
    private double accuracy;
    private MatchPlayerDto matchPlayer;
    private UserDto user;
}
