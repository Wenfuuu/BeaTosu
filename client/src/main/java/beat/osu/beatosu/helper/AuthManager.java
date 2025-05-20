package beat.osu.beatosu.helper;

import beat.osu.beatosu.model.User;
import lombok.Getter;
import lombok.Setter;

public class AuthManager {
    @Getter
    @Setter
    private static User user;

    public static boolean isAuthenticated() {
        return user != null;
    }

    public static void logout() {
        user = null;
    }
}
