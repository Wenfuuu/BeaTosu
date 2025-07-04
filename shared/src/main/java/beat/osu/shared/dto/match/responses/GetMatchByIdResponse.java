package beat.osu.shared.dto.match.responses;

import beat.osu.shared.dto.match.MatchDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMatchByIdResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private MatchDto matches;
}
