package beat.osu.server.service;

import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.auth.responses.RegisterResponse;

public class AuthService {

    private UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    public Result<RegisterResponse> registerUser(RegisterRequest request) {
//        userRepository.InsertUser(request.getUsername(), request.getPassword(), request.getEmail());
//
//        boolean success = true;
//        String message = "User registered successfully";
//
//        return Result.success("Registration successful", new RegisterResponse(success, message));
//    }
}
