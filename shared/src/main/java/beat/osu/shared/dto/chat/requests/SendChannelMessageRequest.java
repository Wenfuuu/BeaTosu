package beat.osu.shared.dto.chat.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendChannelMessageRequest implements Serializable {
    private int channelId;
    private String message;
}