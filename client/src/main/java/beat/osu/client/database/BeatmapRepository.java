package beat.osu.client.database;

import beat.osu.client.database.connection.Connect;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BeatmapRepository {
    private final Connection conn;

    public BeatmapRepository() {
        conn = Connect.getInstance().getConn();
    }

    public ArrayList<Beatmap> fetchBeatmaps() {
        ArrayList<Beatmap> beatmaps = new ArrayList<>();
        String query = "SELECT * FROM Beatmaps bm " +
                "JOIN BeatmapSets bs ON bm.beatmapSetId = bs.beatmapSetId " +
                "ORDER BY bs.beatmapSetId ASC, starRating ASC;";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                BeatmapSet set = new BeatmapSet(
                        rs.getInt("beatmapSetId"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("creator"),
                        rs.getString("length"),
                        rs.getInt("bpm"),
                        rs.getString("backgroundFile")
                );

                Beatmap bm = new Beatmap(
                        rs.getInt("beatmapId"),
                        rs.getInt("beatmapSetId"),
                        rs.getString("version"),
                        rs.getDouble("hpDrainRate"),
                        rs.getDouble("circleSize"),
                        rs.getDouble("overallDifficulty"),
                        rs.getDouble("approachRate"),
                        rs.getDouble("slideMultiplier"),
                        rs.getDouble("sliderTickRate"),
                        rs.getDouble("starRating"),
                        set
                );

                beatmaps.add(bm);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return beatmaps;
    }

    public void insertBeatmapSet(
            int beatmapSetId,
            String title,
            String artist,
            String creator,
            String length,
            int bpm,
            String backgroundFile
    ) {
        String query = "INSERT INTO beatmapSets VALUES(?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, beatmapSetId);
            statement.setString(2, title);
            statement.setString(3, artist);
            statement.setString(4, creator);
            statement.setString(5, length);
            statement.setInt(6, bpm);
            statement.setString(7, backgroundFile);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertBeatmap(
            int beatmapId,
            int beatmapSetId,
            String version,
            double hpDrainRate,
            double circleSize,
            double overallDifficulty,
            double approachRate,
            double slideMultiplier,
            double sliderTickRate,
            double starRating
    ) {
        String query = "INSERT INTO beatmaps VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, beatmapId);
            statement.setInt(2, beatmapSetId);
            statement.setString(3, version);
            statement.setDouble(4, hpDrainRate);
            statement.setDouble(5, circleSize);
            statement.setDouble(6, overallDifficulty);
            statement.setDouble(7, approachRate);
            statement.setDouble(8, slideMultiplier);
            statement.setDouble(9, sliderTickRate);
            statement.setDouble(10, starRating);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
