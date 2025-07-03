package beat.osu.shared.dto.auth.responses;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
}
