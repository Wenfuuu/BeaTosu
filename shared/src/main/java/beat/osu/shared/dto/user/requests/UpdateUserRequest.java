package beat.osu.shared.dto.user.requests;

import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UpdateUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserDto user;
}
