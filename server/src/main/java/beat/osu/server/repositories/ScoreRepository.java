package beat.osu.server.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import beat.osu.server.database.Connect;
import beat.osu.server.entities.Score;

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
        String query = "INSERT INTO scores (beatmap_id, user_id, score, highest_combo, accuracy, " +
                "perfect_hit, geki_hit, great_hit, katu_hit, good_hit, miss, grade, date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, beatmapId);
            statement.setInt(2, userId);
            statement.setInt(3, score);
            statement.setInt(4, highestCombo);
            statement.setDouble(5, accuracy);
            statement.setInt(6, perfectHit);
            statement.setInt(7, gekiHit);
            statement.setInt(8, greatHit);
            statement.setInt(9, katuHit);
            statement.setInt(10, goodHit);
            statement.setInt(11, miss);
            statement.setString(12, grade);
            statement.setTimestamp(13, Timestamp.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Score> getScoresByBeatmapId(int beatmapId) {
        ArrayList<Score> scores = new ArrayList<>();

        String query = "SELECT * FROM scores WHERE beatmap_id = ? ORDER BY score DESC;";
        try (var statement = conn.prepareStatement(query)) {
            statement.setInt(1, beatmapId);
            var rs = statement.executeQuery();
            while (rs.next()) {
                Score score = new Score(
                        rs.getInt("id"),
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
