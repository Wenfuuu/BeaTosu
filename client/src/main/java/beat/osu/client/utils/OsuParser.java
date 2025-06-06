package beat.osu.client.utils;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BreakPoint;
import beat.osu.client.model.TimingPoint;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OsuParser {
    private static BeatmapController beatmapController = new BeatmapController();
    @Getter
    private static Beatmap currentBeatmap;

    @Getter
    private static Map<String, String> general = new HashMap<>();
    @Getter
    private static Map<String, String> metadata = new HashMap<>();
    @Getter
    private static Map<String, String> difficulty = new HashMap<>();
    @Getter
    private static Map<String, String> colours = new HashMap<>();
    @Getter
    private static ArrayList<String> hitObjects = new ArrayList<>();
    @Getter
    private static ArrayList<String> timingPoints = new ArrayList<>();
    @Getter
    private static ArrayList<String> events = new ArrayList<>();

    private static String bgFileName = "";
    private static double bgm = 0;
    @Getter
    private static ArrayList<TimingPoint> timingPointsList = new ArrayList<>();
    @Getter
    private static ArrayList<BreakPoint> breakPointsList = new ArrayList<>();

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
                creator, timeString, getBGM()).thenApply(
                response -> {
                    if (response.isSuccess()) {
                        System.out.println("Beatmap set inserted successfully: " + response.getValue().getMessage());
                    } else {
                        System.err.println("Failed to insert beatmap set: " + response.getError().getMessage());
                    }
                    return null;
                }
        );
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

        beatmapController.insertBeatmap(beatmapId, beatmapSetId, version,
                hpDrainRate, circleSize, overallDifficulty, approachRate,
                slideMultiplier, sliderTickRate, starRating).thenApply(
                response -> {
                    if (response.isSuccess()) {
                        System.out.println("Beatmap inserted successfully: " + response.getValue().getMessage());
                    } else {
                        System.err.println("Failed to insert beatmap: " + response.getError().getMessage());
                    }
                    return null;
                }
        );
    }

    public static String getOszPath(Beatmap beatmap) {
        return String.format("%d %s - %s.osz",
                beatmap.getBeatmapSet().getBeatmapSetId(),
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getTitle());
    }

    public static void extractAndParse(Beatmap beatmap) {
//        String oszPath = String.format("./src/main/resources/beatmaps/%s",
//                getOszPath(beatmap));
        File oszFile = new File(ResourceManager.getBeatmapDirectory(), getOszPath(beatmap));
//        String outputPath = String.format("./src/main/resources/temp/%s", beatmap.getBeatmapSetId());
        File outputDir = new File(ResourceManager.getTempDirectory(), String.valueOf(beatmap.getBeatmapSetId()));

        try {
            OszExtractor.extractOsz(oszFile, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void parseBeatmap(Beatmap beatmap) throws IOException {
        currentBeatmap = beatmap;
        String title = beatmap.getBeatmapSet().getTitle().replace("<", "");
        String fixedTitle = title.replace(">", "");
        String version = beatmap.getVersion().replace("?", "");
        String osuPath = String.format("%s - %s (%s) [%s].osu",
                beatmap.getBeatmapSet().getArtist(),
                fixedTitle,
                beatmap.getBeatmapSet().getCreator(),
                version);
        File beatmapDir = new File(ResourceManager.getTempDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
        File osuFile = new File(beatmapDir, osuPath);

        parseOsuFile(osuFile);
    }

    public static void parseOsuFile(File osuFile) throws IOException {
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
                    if(line.startsWith("Combo")) {
                        parseKeyValue(line, colours);
                    }
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

        for (String timingPointData : timingPoints) {
            TimingPoint tp = new TimingPoint(timingPointData);
            timingPointsList.add(tp);
        }

        for (String eventData : events) {
            String[] parts = eventData.split(",");
            if (parts.length > 2 && parts[0].equals("2")) { // Break point
                int startTime = Integer.parseInt(parts[1]);
                int endTime = Integer.parseInt(parts[2]);
                BreakPoint bp = new BreakPoint(startTime, endTime);
                breakPointsList.add(bp);
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

//    public static String getBeatmapSetBgFile() {
//
//    }

    public static String getBgFile() {
        if(bgFileName.isBlank()) {
            for (String temp : events) {
                String[] arr = temp.split(",");
                String fileName = arr[2];
                bgFileName = fileName.replace("\"", "");
                if(bgFileName.endsWith(".jpg") || bgFileName.endsWith(".png") || bgFileName.endsWith(".jpeg")) break;
            }
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

    public static double getPreviewTime() {
        String previewTimeStr = general.get("PreviewTime");
        if (previewTimeStr != null && !previewTimeStr.isEmpty()) {
            return Double.parseDouble(previewTimeStr);
        }
        return 0;
    }
}
