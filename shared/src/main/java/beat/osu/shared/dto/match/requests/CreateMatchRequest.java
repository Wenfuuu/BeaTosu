package beat.osu.shared.dto.match.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String password;
    private int maxPlayerCount;
    private int beatmapId;
}
