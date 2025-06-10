package beat.osu.shared.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChatMessageDto implements Serializable {
    private int senderId;
    private String senderName;
    private boolean fromSupporter;
    private int recipientId;
    private String recipientName;
    private String message;
    private LocalDateTime timestamp;
}