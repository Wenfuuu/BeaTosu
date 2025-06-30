package beat.osu.shared.dto.auth.responses;

import java.io.Serializable;

import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private UserDto user;
}