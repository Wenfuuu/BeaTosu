package beat.osu.shared.dto.session.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetSessionDataResponse {
    private Object value;
    private String message;
}
