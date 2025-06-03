package beat.osu.shared.dto.system.responses;

import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetConnectedUsersResponse implements Serializable {
    private List<UserDto> connectedUsers;
}