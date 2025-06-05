package beat.osu.server.service;

import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.auth.responses.LoginResponse;
import beat.osu.shared.dto.auth.responses.RegisterResponse;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthService {

    private UserRepository userRepository;
    private SessionService sessionService;

    public Result<RegisterResponse> registerUser(RegisterRequest request, String clientId) {
        try {
            if (request.getUsername().isBlank() || request.getEmail().isBlank() || request.getPassword().isBlank()) {
                return Result.failure(Error.validation("There are empty fields!"));
            }

            if (!request.getEmail().endsWith("@gmail.com")) {
                return Result.failure(Error.validation("Invalid email format!"));
            }

            if (request.getPassword().length() < 8) {
                return Result.failure(Error.validation("Password must be at least 8 characters!"));
            }

            userRepository.insertUser(request.getUsername(), request.getEmail(), request.getPassword(), request.getCountryCode());

            String message = "User registered successfully!";

            return Result.success(new RegisterResponse(message));
        } catch (Exception e) {
            return Result.failure(Error.internal("Registration failed: " + e.getMessage()));
        }
    }

    public Result<LoginResponse> loginUser(LoginRequest request, String clientId) {
        try {
            if (request.getUsername().isBlank() || request.getPassword().isBlank()) {
                return Result.failure(Error.validation("There are empty fields!"));
            }

            var user = userRepository.findUserByUsername(request.getUsername());
            if (user == null) {
                return Result.failure(Error.notFound("User not found!"));
            }

            if (!user.getPasswordHash().equals(request.getPassword())) {
                return Result.failure(Error.validation("Invalid password!"));
            }

            System.out.println("Client " + clientId + " logged in as user: " + user.getUsername());
            sessionService.setSessionData(clientId, "userId", user.getId());

            String message = "Successfully logged in as " + user.getUsername() + "!";
            UserDto userData = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getCountryCode(),
                    user.getProfilePicture(), user.getPerformance(), user.getAccuracy(), user.getPlayCount(), user.getLevel());

            UserConnectedEvent event = new UserConnectedEvent(userData);
            RealtimeMessage userConnectedMessage = new RealtimeMessage(
                    RealtimeMessageType.USER_CONNECTED,
                    "SYSTEM",
                    event
            );
            RealtimeMessageHandler.broadcastToAll(userConnectedMessage);

            return Result.success(new LoginResponse(message, userData));
        } catch (Exception e) {
            return Result.failure(Error.internal("Login failed: " + e.getMessage()));
        }
    }
}
