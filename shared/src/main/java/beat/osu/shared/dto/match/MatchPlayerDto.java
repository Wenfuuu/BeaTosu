package beat.osu.shared.dto.match;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayerDto implements Serializable {
    private int id;
    private int matchId;
    private int userId;

    private String username;
    private byte[] profilePicture;
    private String role;
    private String status;
    private int rank;

    private int matchSlotIndex;
}
