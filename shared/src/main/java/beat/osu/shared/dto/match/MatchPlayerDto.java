package beat.osu.shared.dto.match;

import java.io.Serializable;

import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
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

    private PlayerRole role;
    private PlayerStatus status;

    private int matchSlotIndex;
}
