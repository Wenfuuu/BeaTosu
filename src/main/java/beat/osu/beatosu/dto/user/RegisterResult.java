package beat.osu.beatosu.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResult {
    private boolean success;
    private String message;
}
