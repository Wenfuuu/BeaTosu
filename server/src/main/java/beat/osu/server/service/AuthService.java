package beat.osu.server.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.LogoutRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.auth.responses.LoginResponse;
import beat.osu.shared.dto.auth.responses.LogoutResponse;
import beat.osu.shared.dto.auth.responses.RegisterResponse;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;
import beat.osu.shared.enums.message.RealtimeMessageType;
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

            String hashedPassword = hashPassword(request.getPassword());
            userRepository.insertUser(request.getUsername(), request.getEmail(), hashedPassword, request.getCountryCode(), request.getProfilePicture(), request.isSupporter());

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

            User user = userRepository.findUserByUsername(request.getUsername());
            if (user == null) {
                return Result.failure(Error.notFound("User not found!"));
            }

            String hashedPassword = hashPassword(request.getPassword());
            if (!user.getPasswordHash().equals(hashedPassword)) {
                return Result.failure(Error.validation("Invalid password!"));
            }

            if (sessionService.getClientIdByUserId(user.getId()) != null) {
                return Result.failure(Error.validation("This account is already logged in!"));
            }

            System.out.println("Client " + clientId + " logged in as user: " + user.getUsername());
            sessionService.setSessionData(clientId, "userId", user.getId());

            String message = "Successfully logged in as " + user.getUsername() + "!";
            UserDto userData = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getCountryCode(),
                    user.getProfilePicture(), user.getPerformance(), user.getAccuracy(), user.getPlayCount(), user.getLevel(), userRepository.getUserRank(user.getId()), user.isSupporter());

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

    public Result<LogoutResponse> logoutUser(LogoutRequest request, String clientId) {
        try {
            Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
            if (userId == null) {
                return Result.failure(Error.unauthorized("User not authenticated"));
            }

            User user = userRepository.findUserById(userId);
            if (user == null) {
                return Result.failure(Error.notFound("User not found"));
            }

            UserDto userData = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getCountryCode(),
                    user.getProfilePicture(), user.getPerformance(), user.getAccuracy(), user.getPlayCount(), user.getLevel(), userRepository.getUserRank(user.getId()), user.isSupporter());
            
            UserDisconnectedEvent event = new UserDisconnectedEvent(userData);
            RealtimeMessage userDisconnectedMessage = new RealtimeMessage(
                    RealtimeMessageType.USER_DISCONNECTED,
                    "SYSTEM",
                    event
            );
            
            sessionService.removeSessionValue(clientId, "userId");
            
            RealtimeMessageHandler.broadcastToAll(userDisconnectedMessage);

            String message = "Successfully logged out!";
            return Result.success(new LogoutResponse(message));
        } catch (Exception e) {
            return Result.failure(Error.internal("Logout failed: " + e.getMessage()));
        }
    }

    private String hashPassword(String password) {
        StringBuilder hex = new StringBuilder();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

            for (byte b : encodedHash) {
                String hexStr = Integer.toHexString(0xff & b);
                if (hexStr.length() == 1) {
                    hex.append('0');
                }
                hex.append(hexStr);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return hex.toString();
    }
}
