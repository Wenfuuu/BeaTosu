package beat.osu.shared.dto.chat;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDto implements Serializable {
    private int id;
    private String name;
    private String description;

    private int memberCount;
}
