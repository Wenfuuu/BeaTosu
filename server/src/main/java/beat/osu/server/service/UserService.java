package beat.osu.server.service;

import java.util.List;

import beat.osu.server.entities.User;
import beat.osu.server.repositories.UserRepository;
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
}
