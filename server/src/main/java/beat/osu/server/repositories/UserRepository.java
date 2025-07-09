package beat.osu.server.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beat.osu.server.database.Connect;
import beat.osu.server.entities.User;

public class UserRepository {
    private final Connection conn;

    public UserRepository() {
        this.conn = Connect.getInstance().getConn();
    }

    public void insertUser(String username, String email, String password, String countryCode, byte[] profilePicture,
            boolean isSupporter) {
        String query = "INSERT INTO users (username, email, password_hash, country_code, profile_picture, performance, accuracy, play_count, level, experience, is_supporter) "
                +
                "VALUES (?, ?, ?, ?, ?, 0, 0.00, 0, 1, 0, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, countryCode);
            statement.setBytes(5, profilePicture); // Allow null for optional profile picture
            statement.setBoolean(6, isSupporter);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?;";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, email);
            var rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("country_code"),
                        rs.getBytes("profile_picture"),
                        rs.getInt("performance"),
                        rs.getDouble("accuracy"),
                        rs.getInt("play_count"),
                        rs.getInt("level"),
                        rs.getInt("experience"),
                        rs.getBoolean("is_supporter"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?;";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            var rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("country_code"),
                        rs.getBytes("profile_picture"),
                        rs.getInt("performance"),
                        rs.getDouble("accuracy"),
                        rs.getInt("play_count"),
                        rs.getInt("level"),
                        rs.getInt("experience"),
                        rs.getBoolean("is_supporter"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserById(int userId) {
        String query = "SELECT * FROM users WHERE id = ?;";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            var rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("country_code"),
                        rs.getBytes("profile_picture"),
                        rs.getInt("performance"),
                        rs.getDouble("accuracy"),
                        rs.getInt("play_count"),
                        rs.getInt("level"),
                        rs.getInt("experience"),
                        rs.getBoolean("is_supporter"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET username = ?, email = ?, country_code = ?, profile_picture = ?, " +
                "performance = ?, accuracy = ?, play_count = ?, level = ?, experience = ?, is_supporter = ? " +
                "WHERE id = ?;";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountryCode());
            statement.setBytes(4, user.getProfilePicture());
            statement.setInt(5, user.getPerformance());
            statement.setDouble(6, user.getAccuracy());
            statement.setInt(7, user.getPlayCount());
            statement.setInt(8, user.getLevel());
            statement.setInt(9, user.getExperience());
            statement.setBoolean(10, user.isSupporter());
            statement.setInt(11, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getUserRank(int userId) {
        User user = findUserById(userId);
        if (user == null) {
            return -1;
        }

        String query = "SELECT COUNT(*) + 1 as user_rank FROM users WHERE performance > ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, user.getPerformance());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_rank");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }
}