package beat.osu.shared.dto.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class GetSessionDataRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String key;
}
