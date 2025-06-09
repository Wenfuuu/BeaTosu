package beat.osu.shared.dto.chat.responses;

import beat.osu.shared.dto.chat.PrivateChatMessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendPrivateChatMessageResponse implements Serializable {
    private PrivateChatMessageDto privateChatMessage;
}
