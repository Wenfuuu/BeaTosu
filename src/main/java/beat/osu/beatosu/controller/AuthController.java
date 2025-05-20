package beat.osu.beatosu.controller;

import beat.osu.beatosu.database.AuthRepository;
import beat.osu.beatosu.dto.user.LoginResult;
import beat.osu.beatosu.model.User;

public class AuthController {

    private AuthRepository authRepository;

    public AuthController() {
        this.authRepository = new AuthRepository();
    }

    public LoginResult login(String username, String password) {
        if(username.isBlank() || password.isBlank()) {
            return new LoginResult(false, "There are empty fields!", null);
        }

        User user = authRepository.login(username, password);
        if(user == null) {
            return new LoginResult(false, "Invalid username or password!", null);
        }

        return new LoginResult(true, "Login Success!", user);
    }

}
