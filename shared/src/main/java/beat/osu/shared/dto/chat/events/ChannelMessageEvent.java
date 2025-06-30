package beat.osu.shared.dto.chat.events;

import java.io.Serializable;

import beat.osu.shared.dto.chat.ChannelMessageDto;
import lombok.Data;

@Data
public class ChannelMessageEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private ChannelMessageDto channelMessage;
    private long timestamp;

    public ChannelMessageEvent(ChannelMessageDto channelMessage) {
        this.channelMessage = channelMessage;
        this.timestamp = System.currentTimeMillis();
    }
}
