package beat.osu.shared.dto.match.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PlayerFinishedEventResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
}
