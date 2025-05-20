package beat.osu.beatosu.controller;

import beat.osu.beatosu.database.AuthRepository;
import beat.osu.beatosu.dto.user.LoginResult;
import beat.osu.beatosu.dto.user.RegisterResult;
import beat.osu.beatosu.model.User;

public class AuthController {

    private final AuthRepository authRepository;

    public AuthController() {
        this.authRepository = new AuthRepository();
    }

    public RegisterResult register(String username, String email, String password) {
        if(username.isBlank() || email.isBlank() || password.isBlank()) {
            return new RegisterResult(false, "There are empty fields!");
        }

        if(!email.endsWith("@gmail.com")) {
            return new RegisterResult(false, "Invalid email format!");
        }

        if(password.length() < 8) {
            return new RegisterResult(false, "Password must be at least 8 characters!");
        }

        return authRepository.register(username, email, password);
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
