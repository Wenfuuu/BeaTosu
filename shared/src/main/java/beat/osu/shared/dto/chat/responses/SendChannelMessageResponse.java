package beat.osu.shared.dto.chat.responses;

import beat.osu.shared.dto.chat.ChannelMessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendChannelMessageResponse implements Serializable {
    private ChannelMessageDto channelMessage;
}