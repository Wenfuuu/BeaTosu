package beat.osu.shared.dto.chat.responses;

import beat.osu.shared.dto.chat.ChannelDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinChannelResponse implements Serializable {
    private ChannelDto channel;
}