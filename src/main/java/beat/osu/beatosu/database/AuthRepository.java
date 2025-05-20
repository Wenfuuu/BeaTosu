package beat.osu.beatosu.database;

import beat.osu.beatosu.database.connection.Connect;
import beat.osu.beatosu.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    private Connection conn;

    public AuthRepository() {
        this.conn = Connect.getInstance().getConn();
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
