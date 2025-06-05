package beat.osu.shared.dto.user.events;

import beat.osu.shared.dto.user.UserDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserConnectedEvent implements Serializable {
    private UserDto userDto;
    private long timestamp;

    public UserConnectedEvent(UserDto userDto) {
        this.userDto = userDto;
        this.timestamp = System.currentTimeMillis();
    }
}
