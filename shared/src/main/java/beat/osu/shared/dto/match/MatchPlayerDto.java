package beat.osu.shared.dto.match;

import java.io.Serializable;

import beat.osu.shared.dto.user.UserDto;
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

    private UserDto user;

    private String role;
    private String status;

    private int matchSlotIndex;
}
