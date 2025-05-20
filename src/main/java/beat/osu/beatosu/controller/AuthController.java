package beat.osu.beatosu.controller;

import beat.osu.beatosu.database.AuthRepository;
import beat.osu.beatosu.model.User;

public class AuthController {

    private AuthRepository authRepository;

    public AuthController() {
        this.authRepository = new AuthRepository();
    }

    public String login(String username, String password) {
        if(username.isBlank() || password.isBlank()) {
            return "There are empty fields!";
        }

        User user = authRepository.login(username, password);
        if(user == null) {
            return "Invalid username or password!";
        }

        // store auth

        return "Login Success";
    }

}
