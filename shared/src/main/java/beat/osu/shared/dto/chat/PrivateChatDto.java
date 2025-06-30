package beat.osu.shared.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChatDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int otherUserId;
    private String otherUserName;
}
