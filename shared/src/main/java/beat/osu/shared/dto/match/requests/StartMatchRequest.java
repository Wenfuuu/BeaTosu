package beat.osu.shared.dto.match.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StartMatchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
}
