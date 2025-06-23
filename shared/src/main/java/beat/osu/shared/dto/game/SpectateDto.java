package beat.osu.shared.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpectateDto implements Serializable {
    private int spectatorUserId;
    private int playingUserId;
    private int beatmapId;
}
