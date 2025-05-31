package beat.osu.shared.dto.auth.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse implements Serializable {
    private boolean success;
    private String message;
}
