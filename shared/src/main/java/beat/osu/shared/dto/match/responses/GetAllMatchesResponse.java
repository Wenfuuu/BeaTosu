package beat.osu.shared.dto.match.responses;

import java.io.Serializable;
import java.util.List;

import beat.osu.shared.dto.match.MatchDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllMatchesResponse implements Serializable {
    private List<MatchDto> matches;
}
