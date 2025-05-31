package beat.osu.shared.dto.auth.responses;

import beat.osu.shared.dto.auth.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse implements Serializable {
    private String message;
    private UserDto user;
}