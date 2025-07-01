package beat.osu.server.service;

import beat.osu.server.entities.User;
import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.user.requests.GetUsernameByIdRequest;
import beat.osu.shared.dto.user.responses.GetUsernameByIdResponse;
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
}
