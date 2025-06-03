package beat.osu.server.repositories;

import beat.osu.server.database.Connect;
import beat.osu.server.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {
    private final Connection conn;

    public UserRepository() {
        this.conn = Connect.getInstance().getConn();
    }

    public void insertUser(String username, String email, String password, String countryCode) {
        String query = "INSERT INTO users (username, email, password_hash, country_code, performance, accuracy, play_count, level) " +
                "VALUES (?, ?, ?, ?, 0, 0.00, 0, 1);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4 , countryCode);
            statement.executeUpdate();
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
                        rs.getInt("level")
                );
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
                        rs.getInt("level")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}