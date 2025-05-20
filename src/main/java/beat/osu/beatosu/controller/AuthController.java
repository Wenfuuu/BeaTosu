package beat.osu.beatosu.controller;

import beat.osu.beatosu.database.AuthRepository;

public class AuthController {

    private AuthRepository authQuery;

    public AuthController() {
        this.authQuery = new AuthRepository();
    }


}
