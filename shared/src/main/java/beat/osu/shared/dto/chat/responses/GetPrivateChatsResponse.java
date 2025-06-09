package beat.osu.shared.dto.chat.responses;

import beat.osu.shared.dto.chat.PrivateChatDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPrivateChatsResponse implements Serializable {
    private List<PrivateChatDto> privateChats;
}
