package beat.osu.shared.dto.score.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetScoreRequest implements Serializable {
    private int beatmapId;
}
