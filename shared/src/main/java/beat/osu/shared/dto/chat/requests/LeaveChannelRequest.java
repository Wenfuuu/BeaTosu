package beat.osu.shared.dto.chat.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveChannelRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int channelId;
}
