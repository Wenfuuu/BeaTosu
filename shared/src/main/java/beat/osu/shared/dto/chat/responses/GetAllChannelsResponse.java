package beat.osu.shared.dto.chat.responses;

import beat.osu.shared.dto.chat.ChannelDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllChannelsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ChannelDto> channels;
}