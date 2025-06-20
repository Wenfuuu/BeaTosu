package beat.osu.server.repositories;

import beat.osu.server.database.Connect;
import beat.osu.server.entities.Score;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class ScoreRepository {
    private final Connection conn;

    public ScoreRepository() {
        this.conn = Connect.getInstance().getConn();
    }

    public void insertScore(
            int beatmapId,
            int userId,
            int score,
            int highestCombo,
            double accuracy,
            int perfectHit,
            int gekiHit,
            int greatHit,
            int katuHit,
            int goodHit,
            int miss,
            String grade,
            LocalDateTime date
    ) {
        String query = "INSERT INTO beatmap_scores (id, beatmap_id, user_id, score, highest_combo, accuracy, " +
                "perfect_hit, geki_hit, great_hit, katu_hit, good_hit, miss, grade, date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, UUID.randomUUID().toString());
            statement.setInt(2, beatmapId);
            statement.setInt(3, userId);
            statement.setInt(4, score);
            statement.setInt(5, highestCombo);
            statement.setDouble(6, accuracy);
            statement.setInt(7, perfectHit);
            statement.setInt(8, gekiHit);
            statement.setInt(9, greatHit);
            statement.setInt(10, katuHit);
            statement.setInt(11, goodHit);
            statement.setInt(12, miss);
            statement.setString(13, grade);
            statement.setDate(14, Date.valueOf(date.toLocalDate()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Score> getAllScoresByBeatmapId(int beatmapId) {
        ArrayList<Score> scores = new ArrayList<>();

        String query = "SELECT * FROM beatmap_scores WHERE beatmap_id = ? ORDER BY score DESC;";
        try (var statement = conn.prepareStatement(query)) {
            statement.setInt(1, beatmapId);
            var rs = statement.executeQuery();
            while (rs.next()) {
                Score score = new Score(
                        rs.getString("id"),
                        rs.getInt("beatmap_id"),
                        rs.getInt("user_id"),
                        rs.getInt("score"),
                        rs.getInt("highest_combo"),
                        rs.getDouble("accuracy"),
                        rs.getInt("perfect_hit"),
                        rs.getInt("geki_hit"),
                        rs.getInt("great_hit"),
                        rs.getInt("katu_hit"),
                        rs.getInt("good_hit"),
                        rs.getInt("miss"),
                        rs.getString("grade"),
                        rs.getTimestamp("date").toLocalDateTime()
                );
                scores.add(score);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return scores;
    }
}
