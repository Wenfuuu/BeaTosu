package beat.osu.shared.dto.user.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UpdateUserResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
}
