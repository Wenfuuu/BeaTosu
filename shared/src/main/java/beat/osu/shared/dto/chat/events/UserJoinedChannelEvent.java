package beat.osu.shared.dto.chat.events;

import java.io.Serializable;

import beat.osu.shared.dto.user.UserDto;
import lombok.Data;

@Data
public class UserJoinedChannelEvent implements Serializable {
    private int channelId;
    private UserDto userDto;
    private long timestamp;

    public UserJoinedChannelEvent(int channelId, UserDto userDto) {
        this.channelId = channelId;
        this.userDto = userDto;
        this.timestamp = System.currentTimeMillis();
    }
}
