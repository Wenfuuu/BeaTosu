package beat.osu.shared.dto.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RemoveSessionRequest implements Serializable {
    private int userId;
    private String key;
}
