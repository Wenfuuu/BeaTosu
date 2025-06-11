package beat.osu.shared.dto.match.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveMatchRequest implements Serializable {
    private int matchId;
}
