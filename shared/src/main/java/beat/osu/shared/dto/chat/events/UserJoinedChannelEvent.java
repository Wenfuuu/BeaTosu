package beat.osu.shared.dto.chat.events;

import java.io.Serializable;

import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.user.UserDto;
import lombok.Data;

@Data
public class UserJoinedChannelEvent implements Serializable {
    private ChannelDto channel;
    private int userId;
    private long timestamp;

    public UserJoinedChannelEvent(ChannelDto channel, int userId) {
        this.channel = channel;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }
}
