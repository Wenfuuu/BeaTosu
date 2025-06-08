package beat.osu.server.service;

import beat.osu.server.entities.User;
import beat.osu.server.repositories.UserRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.system.responses.GetConnectedUsersResponse;
import beat.osu.shared.dto.user.UserDto;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SystemService {
    private SessionService sessionService;
    private UserRepository userRepository;

    private List<Integer> getAllUserIds() {
        List<Integer> userIds = new ArrayList<>();

        for (Map<String, Object> session : sessionService.getSessions().values()) {
            Object userId = session.get("userId");
            if (userId != null) {
                userIds.add((Integer) userId);
            }
        }

        return userIds;
    }

    public Result<GetConnectedUsersResponse> getConnectedUsers() {
        List<Integer> userIds = getAllUserIds();
        List<UserDto> users = new ArrayList<>();

        for (Integer userId : userIds) {
            User user = userRepository.findUserById(userId);

            if (user != null) {
                users.add(new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCountryCode(),
                        user.getProfilePicture(),
                        user.getPerformance(),
                        user.getAccuracy(),
                        user.getPlayCount(),
                        user.getLevel(),
                        userRepository.getUserRank(userId),
                        user.isSupporter()
                ));
            } else {
                return Result.failure(Error.notFound("User with ID " + userId + " not found."));
            }
        }

        GetConnectedUsersResponse response = new GetConnectedUsersResponse(users);
        return Result.success(response);
    }
}
