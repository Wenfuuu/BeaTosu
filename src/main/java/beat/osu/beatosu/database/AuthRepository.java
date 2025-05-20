package beat.osu.beatosu.database;

import beat.osu.beatosu.database.connection.Connect;

import java.sql.Connection;

public class AuthRepository {

    private Connection conn;

    public AuthRepository() {
        this.conn = Connect.getInstance().getConn();
    }



}
