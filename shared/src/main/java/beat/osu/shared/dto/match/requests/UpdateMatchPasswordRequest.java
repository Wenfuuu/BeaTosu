package beat.osu.shared.dto.match.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchPasswordRequest implements Serializable {
    private int matchId;
    private String newPassword;
}
