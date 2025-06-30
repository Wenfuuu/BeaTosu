package beat.osu.shared.dto.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RemoveSessionDataRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String key;
}
