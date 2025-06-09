package beat.osu.shared.dto.chat.events;

import beat.osu.shared.dto.chat.ChannelMessageDto;
import beat.osu.shared.dto.chat.PrivateChatMessageDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class PrivateChatMessageEvent implements Serializable {
    private PrivateChatMessageDto privateChatMessage;
    private long timestamp;

    public PrivateChatMessageEvent(PrivateChatMessageDto privateChatMessage) {
        this.privateChatMessage = privateChatMessage;
        this.timestamp = System.currentTimeMillis();
    }
}
