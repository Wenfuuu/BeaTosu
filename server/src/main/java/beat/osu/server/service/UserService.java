package beat.osu.server.service;

import beat.osu.server.entities.User;
import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.requests.GetUsernameByIdRequest;
import beat.osu.shared.dto.user.responses.GetUsernameByIdResponse;
import beat.osu.shared.dto.user.requests.UpdateUserRequest;
import beat.osu.shared.dto.user.responses.UpdateUserResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public User findUserById(int userId) {
        return userRepository.findUserById(userId);
    }

    public int getUserRank(int userId) {
        return userRepository.getUserRank(userId);
    }

    public Result<GetUsernameByIdResponse> getUsernameById(GetUsernameByIdRequest request) {
        try {
            User user = userRepository.findUserById(request.getUserId());

            if (user == null) {
                return Result.failure(Error.notFound("User not found with ID: " + request.getUserId()));
            }

            String message = "Username retrieved successfully";
            return Result.success(new GetUsernameByIdResponse(user.getUsername(), message));
        } catch (Exception e) {
            return Result.failure(Error.internal("Failed to get username: " + e.getMessage()));
        }
    }

    public Result<UpdateUserResponse> updateUser(UpdateUserRequest request) {
        try {
            UserDto userDto = request.getUser();

            // Convert UserDto to User entity
            User user = new User(
                    userDto.getId(),
                    userDto.getUsername(),
                    userDto.getEmail(),
                    null, // password_hash is not included in updates
                    userDto.getCountryCode(),
                    userDto.getProfilePicture(),
                    userDto.getPerformance(),
                    userDto.getAccuracy(),
                    userDto.getPlayCount(),
                    userDto.getLevel(),
                    userDto.getExperience(),
                    userDto.isSupporter());

            // Update user in database
            userRepository.updateUser(user);

            String message = "User updated successfully";
            return Result.success(new UpdateUserResponse(message));
        } catch (Exception e) {
            return Result.failure(Error.internal("Failed to update user: " + e.getMessage()));
        }
    }
}
