package beat.osu.shared.dto.session.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveSessionDataResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
}
