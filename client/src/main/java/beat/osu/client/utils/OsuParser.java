package beat.osu.client.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BreakPeriod;
import beat.osu.client.model.TimingPoint;
import lombok.Getter;
import lombok.Setter;

public class OsuParser {
    private static BeatmapController beatmapController = new BeatmapController();
    @Getter
    private static Beatmap currentBeatmap;

    // Callback for error messages
    @Setter
    private static Consumer<String> errorCallback;

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
//    @Getter
    private static ArrayList<String> events = new ArrayList<>();

    private static String bgFileName = "";
    private static double bpm = 0;
    @Getter
    private static ArrayList<TimingPoint> timingPointsList = new ArrayList<>();
    @Getter
    private static ArrayList<BreakPeriod> breakPeriodsList = new ArrayList<>();

    private static void clearAll() {
        general.clear();
        metadata.clear();
        difficulty.clear();
        colours.clear();
        hitObjects.clear();
        timingPoints.clear();
        events.clear();
        bgFileName = "";
        bpm = 0;
        timingPointsList.clear();
        breakPeriodsList.clear();
    }

    private static double getStarRating(double hp, double cs, double od, double ar, double sm, double st) {
        return 0.15 * hp
                + 0.1 * cs
                + 0.25 * od
                + 0.3 * ar
                + 0.8 * (sm - 1.0)
                + 0.05 * st
                - 0.2;
    }

    private static String decodeType(int type) {
        boolean isHitCircle = (type & 1) != 0;
        boolean isSlider = (type & 2) != 0;

        if (isHitCircle)
            return "circle";
        else if (isSlider)
            return "slider";
        return "spinner";
    }

    public static String getHitObjectCount() {
        int circleCount = 0;
        int sliderCount = 0;
        int spinnerCount = 0;
        for (String hitObject : hitObjects) {
            String[] parts = hitObject.split(",");
            if (parts.length > 3) {
                int type = Integer.parseInt(parts[3]);
                String typeStr = decodeType(type);
                if (typeStr.equals("circle")) {
                    circleCount++;
                } else if (typeStr.equals("slider")) {
                    sliderCount++;
                } else if (typeStr.equals("spinner")) {
                    spinnerCount++;
                }
            }
        }
        return String.format("Circles: %d Sliders: %d Spinners: %d",
                circleCount, sliderCount, spinnerCount);
    }

    public static void insertBeatmapSet(String timeString) {
        int beatmapSetId = Integer.parseInt(metadata.get("BeatmapSetID"));
        String title = metadata.get("Title");
        String artist = metadata.get("Artist");
        String creator = metadata.get("Creator");

        beatmapController.insertBeatmapSet(beatmapSetId, title, artist,
                creator, timeString, getBPM()).thenApply(
                        response -> {
                            if (response.isSuccess()) {
                                System.out.println(
                                        "Beatmap set inserted successfully: " + response.getValue().getMessage());
                            } else {
                                String errorMessage = response.getError().getMessage();
                                System.err.println("Failed to insert beatmap set: " + errorMessage);

                                // Notify UI about the error
                                if (errorCallback != null) {
                                    if (errorMessage.contains("already exists")) {
                                        errorCallback.accept("Beatmap set " + beatmapSetId + " already exists");
                                    } else {
                                        errorCallback.accept("Failed to insert beatmap set: " + errorMessage);
                                    }
                                }
                            }
                            return null;
                        });
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
                                System.out
                                        .println("Beatmap inserted successfully: " + response.getValue().getMessage());
                            } else {
                                String errorMessage = response.getError().getMessage();
                                System.err.println("Failed to insert beatmap: " + errorMessage);

                                // Notify UI about the error
                                if (errorCallback != null) {
                                    if (errorMessage.contains("already exists")) {
                                        errorCallback.accept("Beatmap " + beatmapId + " already exists");
                                    } else {
                                        errorCallback.accept("Failed to insert beatmap: " + errorMessage);
                                    }
                                }
                            }
                            return null;
                        });
    }

    public static String getOszPath(Beatmap beatmap) {
        return String.format("%d %s - %s.osz",
                beatmap.getBeatmapSet().getBeatmapSetId(),
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getTitle());
    }

    public static void extractAndParse(Beatmap beatmap) {
        // Since .osz files are no longer stored in beatmap directory,
        // we assume the beatmap has already been extracted to temp directory
        File outputDir = new File(ResourceManager.getBeatmapDirectory(), String.valueOf(beatmap.getBeatmapSetId()));

        // If the extracted directory doesn't exist, we cannot proceed
        if (!outputDir.exists()) {
            throw new RuntimeException("Beatmap set " + beatmap.getBeatmapSetId()
                    + " not found in temp directory. Please re-upload the beatmap.");
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
        String removeDoubleQuotes = fixedTitle.replace("\"", "");
        String removeBackslash = removeDoubleQuotes.replace("\\", "");
        String removeColon = removeBackslash.replace(":", "");
        String removeSlash = removeColon.replace("/", "");
        String version = beatmap.getVersion().replace("?", "");
        String osuPath = String.format("%s - %s (%s) [%s].osu",
                beatmap.getBeatmapSet().getArtist().replace(":", ""),
                removeSlash,
                beatmap.getBeatmapSet().getCreator(),
                version);
        File beatmapDir = new File(ResourceManager.getBeatmapDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
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
            if (line.isEmpty() || line.startsWith("//"))
                continue;
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
                    if (line.startsWith("Combo")) {
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
                BreakPeriod bp = new BreakPeriod(startTime, endTime);
                breakPeriodsList.add(bp);
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

    public static int getBeatmapId() {
        String beatmapIdStr = metadata.get("BeatmapID");
        if (beatmapIdStr != null && !beatmapIdStr.isEmpty()) {
            return Integer.parseInt(beatmapIdStr);
        }
        return -1;
    }

    public static String getBgFile() {
        if (bgFileName.isBlank()) {
            for (String temp : events) {
                String[] arr = temp.split(",");
                String fileName = arr[2];
                bgFileName = fileName.replace("\"", "");
                if (bgFileName.endsWith(".jpg") || bgFileName.endsWith(".png") || bgFileName.endsWith(".jpeg"))
                    break;
            }
        }
        return bgFileName;
    }

    public static int getBPM() {
        if (bpm == 0) {
            String temp = timingPoints.get(0);
            String[] arr = temp.split(",");
            double beatLength = Double.parseDouble(arr[1]);
            bpm = 60000 / beatLength;
        }
        return (int) bpm;
    }

    public static double getPreviewTime() {
        String previewTimeStr = general.get("PreviewTime");
        if (previewTimeStr != null && !previewTimeStr.isEmpty()) {
            return Double.parseDouble(previewTimeStr);
        }
        return 0;
    }

    public static String getGeneralSampleSet() {
        String sampleSet = general.get("SampleSet");
        if (sampleSet != null && !sampleSet.isEmpty() && !sampleSet.equals("None")) {
            return sampleSet.toLowerCase();
        }
        return "normal";
    }
}
