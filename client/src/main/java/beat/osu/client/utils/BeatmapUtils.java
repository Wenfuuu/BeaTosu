package beat.osu.client.utils;

import beat.osu.client.helper.ResourceManager;
import beat.osu.client.model.Song;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BeatmapUtils {

    public static int getBeatmapCount() {
        File beatmapsDirectory = ResourceManager.getTempDirectory();
        return Objects.requireNonNull(beatmapsDirectory.listFiles()).length - 1;
    }

    public static Set<Song> getBeatmapSongs() {
        Set<Song> songs = new HashSet<>();
        File tempDirectory = ResourceManager.getTempDirectory();
        File[] beatmapSetDirectories = Objects.requireNonNull(tempDirectory.listFiles());

        for (File beatmapSetDirectory : beatmapSetDirectories) {
            if (beatmapSetDirectory.isDirectory()) {
                String audioPath = beatmapSetDirectory.getAbsolutePath() + File.separator + "audio.mp3";
                File[] beatmapSetFiles = Objects.requireNonNull(beatmapSetDirectory.listFiles());

                for (File beatmapFile : beatmapSetFiles) {
                    if (beatmapFile.getName().endsWith(".osu")) {
                        Song song = parseBeatmapFromFilename(beatmapFile.getName(), audioPath);
                        if (song != null) {
                            songs.add(song);
                        }
                        break;
                    }
                }
            }
        }

        return songs;
    }

    private static Song parseBeatmapFromFilename(String filename, String audioPath) {
        try {
            String nameWithoutExtension = filename.substring(0, filename.lastIndexOf(".osu"));

            int versionStart = nameWithoutExtension.lastIndexOf(") [");
            if (versionStart == -1) return null;

            int creatorStart = nameWithoutExtension.lastIndexOf(" (", versionStart);
            if (creatorStart == -1) return null;

            String artistTitlePart = nameWithoutExtension.substring(0, creatorStart);

            String[] artistTitle = artistTitlePart.split(" - ", 2);
            if (artistTitle.length != 2) return null;

            String artist = artistTitle[0].trim();
            String title = artistTitle[1].trim();

            return new Song(title, artist, audioPath);
        } catch (Exception e) {
            System.err.println("Error parsing filename: " + filename);
            return null;
        }
    }
}
