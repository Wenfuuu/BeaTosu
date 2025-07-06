package beat.osu.client.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import beat.osu.client.model.Song;
import beat.osu.client.utils.OsuParser;

public class ResourceManager {

    private static final String APP_NAME = "beatosu";
    private static File applicationDirectory;

    static {
        String userHome = System.getProperty("user.home");
        applicationDirectory = new File(userHome, "." + APP_NAME);
        if (!applicationDirectory.exists()) {
            applicationDirectory.mkdirs();
        }
    }

    public static File getBeatmapDirectory() {
        File dir = new File(applicationDirectory, "beatmaps");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getTempDirectory() {
        File dir = new File(applicationDirectory, "temp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getReplayDirectory() {
        File dir = new File(applicationDirectory, "replays");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getBeatmapSetAudioPath(int beatmapSetId) {
        File dir = new File(getTempDirectory(), String.valueOf(beatmapSetId));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "audio.mp3").getAbsolutePath();
    }

    public static boolean beatmapSetDirectoryExists(int beatmapSetId) {
        File dir = new File(getTempDirectory(), String.valueOf(beatmapSetId));
        return dir.exists() && dir.isDirectory();
    }

    public static int getRandomBeatmapFromCurrentlyPlayingSong() {
        Song currentSong = PlaylistManager.getInstance().getCurrentSong();

        File tempDirectory = ResourceManager.getTempDirectory();
        File[] beatmapSetDirectories = Objects.requireNonNull(tempDirectory.listFiles());

        ArrayList<File> beatmapFiles = new ArrayList<>();

        for (File beatmapSetDirectory : beatmapSetDirectories) {
            if (beatmapSetDirectory.getName().equals(String.valueOf(currentSong.getId()))) {
                if (beatmapSetDirectory.isDirectory()) {
                    File[] beatmapSetFiles = Objects.requireNonNull(beatmapSetDirectory.listFiles());

                    for (File beatmapFile : beatmapSetFiles) {
                        if (beatmapFile.getName().endsWith(".osu")) {
                            beatmapFiles.add(beatmapFile);
                        }
                    }
                }
            }
        }

        Random random = new Random();
        File selectedBeatmapFile = beatmapFiles.get(random.nextInt(beatmapFiles.size()));

        int selectedBeatmapId;

        try {
            OsuParser.parseOsuFile(selectedBeatmapFile);
            selectedBeatmapId = OsuParser.getBeatmapId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return selectedBeatmapId;
    }

    public static InputStream getResourceAsStream(String path) {
        return ResourceManager.class.getResourceAsStream("/" + path);
    }

    public static File getUserFile(String relativePath) {
        return new File(applicationDirectory, relativePath);
    }

    public static String extractResourceToTempAndGetPath(String resourcePath, String fileName) {
        try (InputStream inputStream = getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }

            File tempDir = getTempDirectory();
            File extractedFile = new File(tempDir, fileName);

            // Copy the resource to the temp directory
            Files.copy(inputStream, extractedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return extractedFile.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Failed to extract resource " + resourcePath + " to temp directory: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}