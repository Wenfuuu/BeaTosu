package beat.osu.shared.dto.match.events;

import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PlayerFinishedEvent implements Serializable {
    private int matchId;
    private UserDto user;
}
