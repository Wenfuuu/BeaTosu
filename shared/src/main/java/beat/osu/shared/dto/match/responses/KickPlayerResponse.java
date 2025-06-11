package beat.osu.shared.dto.match.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KickPlayerResponse implements Serializable {
    private String message;
}
