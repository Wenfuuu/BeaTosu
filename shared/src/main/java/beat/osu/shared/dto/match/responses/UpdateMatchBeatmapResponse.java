package beat.osu.shared.dto.match.responses;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchBeatmapResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
}
