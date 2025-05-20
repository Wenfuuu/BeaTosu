package beat.osu.beatosu.database.connection;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {

    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private final String HOST = "localhost:3306";
    private final String DATABASE = "osu";
    private final String CONNECTION = String.format("jdbc:mysql://%s/%s", HOST, DATABASE);

    @Getter
    private Connection conn;
    private static Connect instance;

    public static Connect getInstance(){
        if(instance == null){
            instance = new Connect();
        }
        return instance;
    }

    private Connect(){
        try {
            conn = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
