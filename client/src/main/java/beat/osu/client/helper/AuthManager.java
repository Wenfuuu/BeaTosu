package beat.osu.client.helper;

import beat.osu.shared.dto.auth.UserDto;
import lombok.Getter;
import lombok.Setter;

public class AuthManager {
    @Getter
    @Setter
    private static UserDto user;

    public static boolean isAuthenticated() {
        return user != null;
    }

    public static void logout() {
        user = null;
    }
}
