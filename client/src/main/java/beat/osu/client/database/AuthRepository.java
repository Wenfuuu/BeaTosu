package beat.osu.client.database;

import beat.osu.client.database.connection.Connect;
import beat.osu.client.dto.user.RegisterResult;
import beat.osu.client.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    private final Connection conn;

    public AuthRepository() {
        this.conn = Connect.getInstance().getConn();
    }

    public RegisterResult register(String username, String email, String password) {
        String query = "INSERT INTO users (username, email, password_hash, performance, accuracy, play_count, level) " +
                "VALUES (?, ?, ?, 0, 0.00, 0, 1);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.executeUpdate();

            return new RegisterResult(true, "Register Success!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password_hash = ?;";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getBytes("profile_picture"),
                    rs.getInt("performance"),
                    rs.getDouble("accuracy"),
                    rs.getInt("play_count"),
                    rs.getInt("level")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
