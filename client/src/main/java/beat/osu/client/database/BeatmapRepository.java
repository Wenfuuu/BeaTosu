package beat.osu.client.database;

import beat.osu.client.database.connection.Connect;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BeatmapRepository {
    private final Connection conn;

    public BeatmapRepository() {
        conn = Connect.getInstance().getConn();
    }

    public ArrayList<Beatmap> fetchBeatmaps() {
        ArrayList<Beatmap> beatmaps = new ArrayList<>();

        File dir = new File("./src/main/resources/assets/beatmap");
        Set<String> validFilenames = new HashSet<>();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".osz")) {
                    validFilenames.add(file.getName());
                }
            }
        }

        String query = "SELECT * FROM beatmaps bm " +
                "JOIN beatmap_sets bs ON bm.beatmap_set_id = bs.id " +
                "ORDER BY bs.id ASC, star_rating ASC;";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                int setId = rs.getInt("bs.id");
                String title = rs.getString("bs.title");
                String artist = rs.getString("bs.artist");
                String creator = rs.getString("bs.creator");
                String length = rs.getString("bs.length");
                int bpm = rs.getInt("bs.bpm");

                // has beatmap file validation
                String expectedFilename = String.format("%d %s - %s.osz", setId, artist, title);
                if (!validFilenames.contains(expectedFilename)) {
                    continue;
                }

                BeatmapSet set = new BeatmapSet(setId, title, artist, creator, length, bpm);

                Beatmap bm = new Beatmap(
                        rs.getInt("bm.id"),
                        rs.getInt("bm.beatmap_set_id"),
                        rs.getString("bm.version"),
                        rs.getDouble("bm.hp_drain_rate"),
                        rs.getDouble("bm.circle_size"),
                        rs.getDouble("bm.overall_difficulty"),
                        rs.getDouble("bm.approach_rate"),
                        rs.getDouble("bm.slide_multiplier"),
                        rs.getDouble("bm.slider_tick_rate"),
                        rs.getDouble("bm.star_rating"),
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
        String query = "INSERT INTO beatmaps (id, beatmap_set_id, version, hp_drain_rate, circle_size, " +
                "overall_difficulty, approach_rate, slide_multiplier, slider_tick_rate, star_rating) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
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
