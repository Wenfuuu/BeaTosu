package beat.osu.server.repositories;

import beat.osu.server.database.Connect;
import beat.osu.server.entities.BeatmapSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BeatmapSetRepository {
    private final Connection conn;

    public BeatmapSetRepository() {
        conn = Connect.getInstance().getConn();
    }

    public ArrayList<BeatmapSet> fetchBeatmapSets() {
        ArrayList<BeatmapSet> beatmapSets = new ArrayList<>();

        String query = "SELECT * FROM beatmap_sets ORDER BY id ASC;";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                BeatmapSet set = new BeatmapSet(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("creator"),
                        rs.getString("length"),
                        rs.getInt("bpm")
                );
                beatmapSets.add(set);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return beatmapSets;
    }

    public void insertBeatmapSet(
            int beatmapSetId,
            String title,
            String artist,
            String creator,
            String length,
            int bpm
    ) {
        String query = "INSERT INTO beatmap_sets (id, title, artist, creator, length, bpm) " +
                "VALUES (?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, beatmapSetId);
            statement.setString(2, title);
            statement.setString(3, artist);
            statement.setString(4, creator);
            statement.setString(5, length);
            statement.setInt(6, bpm);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}