package beat.osu.server.service;

import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.auth.responses.RegisterResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthService {

    private UserRepository userRepository;

    public Result<RegisterResponse> registerUser(RegisterRequest request, String clientId) {
        userRepository.InsertUser(request.getUsername(), request.getPassword(), request.getEmail(), request.getCountryCode());

        boolean success = true;
        String message = "User registered successfully!";

        return Result.success(new RegisterResponse(success, message));
    }
}
