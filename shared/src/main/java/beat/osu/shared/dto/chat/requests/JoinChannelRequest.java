package beat.osu.shared.dto.chat.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinChannelRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int channelId;
}