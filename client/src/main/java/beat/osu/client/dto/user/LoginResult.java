package beat.osu.client.dto.user;

import beat.osu.client.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {
    private boolean success;
    private String message;
    private User user;
}
