package beat.osu.shared.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMessageDto implements Serializable {
    private int channelId;
    private int senderId;
    private String senderName;
    private boolean fromSupporter;
    private String message;
    private LocalDateTime timestamp;
}
