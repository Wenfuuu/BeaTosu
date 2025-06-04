package beat.osu.client.helper;

import java.io.File;
import java.io.InputStream;

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


    // For reading bundled resources (CSS, default assets)
    public static InputStream getResourceAsStream(String path) {
        return ResourceManager.class.getResourceAsStream("/" + path);
    }

    // For reading user-generated content (beatmaps, temp)
    public static File getUserFile(String relativePath) {
        return new File(applicationDirectory, relativePath);
    }
}