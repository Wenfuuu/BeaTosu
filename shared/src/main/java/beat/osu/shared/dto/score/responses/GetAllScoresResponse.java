package beat.osu.shared.dto.score.responses;

import beat.osu.shared.dto.score.ScoreDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllScoresResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<ScoreDto> scores;
}
