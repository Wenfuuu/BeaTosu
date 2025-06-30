package beat.osu.shared.dto.match.responses;

import java.io.Serializable;

import beat.osu.shared.dto.match.MatchDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinMatchResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private MatchDto match;
    private String message;
}
