package beat.osu.server.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import beat.osu.server.database.Connect;
import beat.osu.server.entities.Beatmap;

public class BeatmapRepository {
    private final Connection conn;

    public BeatmapRepository() {
        conn = Connect.getInstance().getConn();
    }

    public ArrayList<Beatmap> getAllBeatmaps() {
        ArrayList<Beatmap> beatmaps = new ArrayList<>();

        String query = "SELECT * FROM beatmaps ORDER BY beatmap_set_id ASC, star_rating ASC;";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                Beatmap bm = new Beatmap(
                        rs.getInt("id"),
                        rs.getInt("beatmap_set_id"),
                        rs.getString("version"),
                        rs.getDouble("hp_drain_rate"),
                        rs.getDouble("circle_size"),
                        rs.getDouble("overall_difficulty"),
                        rs.getDouble("approach_rate"),
                        rs.getDouble("slider_multiplier"),
                        rs.getDouble("slider_tick_rate"),
                        rs.getDouble("star_rating")
                );
                beatmaps.add(bm);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return beatmaps;
    }

    public Beatmap getBeatmapById(int id) {
        String query = "SELECT * FROM beatmaps WHERE id = ?;";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new Beatmap(
                        rs.getInt("id"),
                        rs.getInt("beatmap_set_id"),
                        rs.getString("version"),
                        rs.getDouble("hp_drain_rate"),
                        rs.getDouble("circle_size"),
                        rs.getDouble("overall_difficulty"),
                        rs.getDouble("approach_rate"),
                        rs.getDouble("slider_multiplier"),
                        rs.getDouble("slider_tick_rate"),
                        rs.getDouble("star_rating")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void insertBeatmap(
            int id,
            int beatmapSetId,
            String version,
            double hpDrainRate,
            double circleSize,
            double overallDifficulty,
            double approachRate,
            double slideMultiplier,
            double sliderTickRate,
            double starRating
    ) throws SQLException {
        String query = "INSERT INTO beatmaps (id, beatmap_set_id, version, hp_drain_rate, circle_size, " +
                "overall_difficulty, approach_rate, slider_multiplier, slider_tick_rate, star_rating) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, id);
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
            // Check if it's a duplicate key constraint violation
            if (e.getSQLState() != null && e.getSQLState().equals("23000") && 
                e.getMessage().contains("Duplicate entry")) {
                throw new SQLException("DUPLICATE_BEATMAP:" + id, e);
            }
            throw e;
        }
    }
}