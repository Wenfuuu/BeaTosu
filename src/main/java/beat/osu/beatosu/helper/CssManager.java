package beat.osu.beatosu.helper;

import beat.osu.beatosu.Main;

import java.net.URL;

public class CssManager {

    /**
     * Loads a CSS file from the /assets/css/ directory.
     *
     * @param filename The CSS filename (e.g., "RegisterModal.css")
     * @return URL to the CSS file, or null if not found
     */
    public static URL getCssURL(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.err.println("Filename cannot be null or empty.");
            return null;
        }

        String path = "/assets/css/" + filename;
        URL cssUrl = Main.class.getResource(path);

        if (cssUrl == null) {
            System.err.println("CSS file not found at path: " + path);
        }

        return cssUrl;
    }

}
