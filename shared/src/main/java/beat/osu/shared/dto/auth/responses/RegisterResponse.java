package beat.osu.shared.dto.auth.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse implements Serializable {
    private boolean success;
    private String message;
}
