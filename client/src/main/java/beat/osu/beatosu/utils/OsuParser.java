package beat.osu.beatosu.utils;

import beat.osu.beatosu.controller.BeatmapController;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OsuParser is a utility class for parsing .osu files.
 * It extracts information from the file and provides methods to access the parsed data.
 */
@Getter
public class OsuParser {
    private static BeatmapController beatmapController;

    private static final Map<String, String> general = new HashMap<>();
    private static final Map<String, String> metadata = new HashMap<>();
    private static final Map<String, String> difficulty = new HashMap<>();
    private static final Map<String, String> colours = new HashMap<>();
    private static final ArrayList<String> hitObjects = new ArrayList<>();
    private static final ArrayList<String> timingPoints = new ArrayList<>();
    private static final ArrayList<String> events = new ArrayList<>();
    private static String bgFileName = "";
    private static double bgm = 0;

    public static void init() {
        beatmapController = new BeatmapController();
    }

    private static void clearAll() {
        general.clear();
        metadata.clear();
        difficulty.clear();
        colours.clear();
        hitObjects.clear();
        timingPoints.clear();
        events.clear();
        bgFileName = "";
        bgm = 0;
    }

    private static double getStarRating(double hp, double cs, double od, double ar, double sm, double st) {
        return 0.15 * hp
                + 0.1  * cs
                + 0.25 * od
                + 0.3  * ar
                + 0.8  * (sm - 1.0)
                + 0.05 * st
                - 0.2;
    }

    public static void insertBeatmapSet(String timeString) {
        int beatmapSetId = Integer.parseInt(metadata.get("BeatmapSetID"));
        String title = metadata.get("Title");
        String artist = metadata.get("Artist");
        String creator = metadata.get("Creator");

        beatmapController.insertBeatmapSet(beatmapSetId, title, artist,
                creator, timeString, getBGM(), getBgFile());
    }

    public static void insertData() {
        int beatmapId = Integer.parseInt(metadata.get("BeatmapID"));
        int beatmapSetId = Integer.parseInt(metadata.get("BeatmapSetID"));
        String version = metadata.get("Version");
        double hpDrainRate = Double.parseDouble(difficulty.get("HPDrainRate"));
        double circleSize = Double.parseDouble(difficulty.get("CircleSize"));
        double overallDifficulty = Double.parseDouble(difficulty.get("OverallDifficulty"));
        double approachRate = Double.parseDouble(difficulty.get("ApproachRate"));
        double slideMultiplier = Double.parseDouble(difficulty.get("SliderMultiplier"));
        double sliderTickRate = Double.parseDouble(difficulty.get("SliderTickRate"));
        double starRating = getStarRating(hpDrainRate, circleSize, overallDifficulty,
                approachRate, slideMultiplier, sliderTickRate);

        beatmapController.insertBeatmap(beatmapId, beatmapSetId, version, hpDrainRate,
                circleSize, overallDifficulty, approachRate, slideMultiplier,
                sliderTickRate, starRating);
    }

    //    new File("./src/main/java/resources/assets/temp/*.osu");
    public static void parse(File osuFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(osuFile));
        String line;
        String section = "";
        clearAll();

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1);
                continue;
            }

            switch (section) {
                case "General":
                    parseKeyValue(line, general);
                    break;
                case "Metadata":
                    parseKeyValue(line, metadata);
                    break;
                case "Difficulty":
                    parseKeyValue(line, difficulty);
                    break;
                case "Colours":
                    parseKeyValue(line, colours);
                    break;
                case "HitObjects":
                    hitObjects.add(line);
                    break;
                case "TimingPoints":
                    timingPoints.add(line);
                    break;
                case "Events":
                    events.add(line);
                    break;
            }
        }

        reader.close();
    }

    private static void parseKeyValue(String line, Map<String, String> map) {
        String[] parts = line.split(":", 2);
        if (parts.length == 2) {
            map.put(parts[0].trim(), parts[1].trim());
        }
    }

    public static String getBgFile() {
        if(bgFileName.isBlank()) {
            String temp = events.get(0);
            String[] arr = temp.split(",");
            String fileName = arr[2];
            bgFileName = fileName.replace("\"", "");
        }
        return bgFileName;
    }

    public static int getBGM() {
        if(bgm == 0) {
            String temp = timingPoints.get(0);
            String[] arr = temp.split(",");
            double beatLength = Double.parseDouble(arr[1]);
            bgm = 60000 / beatLength;
        }
        return (int)bgm;
    }
}
