package beat.osu.shared.dto.match.responses;

import beat.osu.shared.dto.match.MatchDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StartMatchResponse implements Serializable {
    private MatchDto matchDto;
    private String message;
}
