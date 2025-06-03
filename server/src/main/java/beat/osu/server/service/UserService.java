package beat.osu.server.service;

import beat.osu.server.entities.User;
import beat.osu.server.repositories.UserRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public User findUserById(int userId) {
        return userRepository.findUserById(userId);
    }
}
