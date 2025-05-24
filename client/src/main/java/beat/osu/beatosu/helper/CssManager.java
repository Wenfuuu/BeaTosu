package beat.osu.beatosu.helper;

import beat.osu.beatosu.Main;

import java.net.URL;

public class CssManager {
    public static URL getGlobalCssURL() {
        String path = "/assets/css/global/index.css";
        URL cssUrl = Main.class.getResource(path);

        if (cssUrl == null) {
            System.err.println("CSS file not found at path: " + path);
        }

        return cssUrl;
    }

    public static URL getLandingCssURL(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.err.println("Filename cannot be null or empty.");
            return null;
        }

        String path = "/assets/css/landing/" + filename;
        URL cssUrl = Main.class.getResource(path);

        if (cssUrl == null) {
            System.err.println("CSS file not found at path: " + path);
        }

        return cssUrl;
    }

    public static URL getHomeCssURL(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.err.println("Filename cannot be null or empty.");
            return null;
        }

        String path = "/assets/css/home/" + filename;
        URL cssUrl = Main.class.getResource(path);

        if (cssUrl == null) {
            System.err.println("CSS file not found at path: " + path);
        }

        return cssUrl;
    }

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
