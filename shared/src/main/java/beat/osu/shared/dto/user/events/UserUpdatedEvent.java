package beat.osu.shared.dto.user.events;

import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserDto userDto;
}
