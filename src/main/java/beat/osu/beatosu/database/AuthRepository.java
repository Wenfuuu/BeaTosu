package beat.osu.beatosu.database;

import beat.osu.beatosu.database.connection.Connect;
import beat.osu.beatosu.dto.user.LoginResult;
import beat.osu.beatosu.dto.user.RegisterResult;
import beat.osu.beatosu.model.User;

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
        String query = "INSERT INTO users(username, email, password) VALUES(?, ?, ?);";
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
        String query = "SELECT * FROM users WHERE username = ? AND password = ?;";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()) return null;

            return new User(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getBytes(5)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
