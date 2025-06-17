package beat.osu.client.utils;

import beat.osu.client.helper.ResourceManager;

import java.io.File;
import java.util.Objects;

public class BeatmapUtils {

    public static int getBeatmapCount() {
        File beatmapsDirectory = ResourceManager.getTempDirectory();
        return Objects.requireNonNull(beatmapsDirectory.listFiles()).length - 1;
    }
}
