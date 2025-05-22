package beat.osu.beatosu.helper;

import beat.osu.beatosu.utils.OsuParser;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackgroundManager {
    private static final String BACKGROUNDS_DIR = "./src/main/resources/assets/backgrounds/";
    private static final String TEMP_DIR = "./src/main/resources/assets/temp/";
    private static final Random random = new Random();
    private static List<String> backgroundFiles = null;

    public static String getRandomBackgroundURL() {
        if (backgroundFiles == null) {
            loadBackgroundFiles();
        }

        if (backgroundFiles.isEmpty()) {
            return "online_background_422fab3bf0c3af0234ee21be511bc3a9.jpg";
        }

        // Get a random background from the list
        int randomIndex = random.nextInt(backgroundFiles.size());
        return backgroundFiles.get(randomIndex);
    }

    private static void loadBackgroundFiles() {
        backgroundFiles = new ArrayList<>();
        File directory = new File(BACKGROUNDS_DIR);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") ||
                            name.toLowerCase().endsWith(".png") ||
                            name.toLowerCase().endsWith(".jpeg")
            );

            if (files != null) {
                for (File file : files) {
                    // Store the filename for later use
                    backgroundFiles.add(file.getName());
                }
            }
        }

        if (backgroundFiles.isEmpty()) {
            System.err.println("No background images found in: " + BACKGROUNDS_DIR);
        } else {
            System.out.println("Loaded " + backgroundFiles.size() + " background images");
        }
    }

    // Modify BackgroundManager to create layered backgrounds
    public static void setRandomBackground(Scene scene) {
        String randomBg = getRandomBackgroundURL();
        try {
            File imageFile = new File(BACKGROUNDS_DIR + randomBg);
            String imageUrl = imageFile.toURI().toURL().toString();

            // Apply the background image first
            String backgroundStyle = "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center;";

            scene.getRoot().setStyle(backgroundStyle);

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25);");
            ((StackPane)scene.getRoot()).getChildren().add(0, overlay);

        } catch (Exception e) {
            System.err.println("Error setting background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setGameBackground(Scene scene) {
        System.out.println("setting game background");
        String gameBg = OsuParser.getBgFile();

        try {
            // Create a File object for the image
            File imageFile = new File(TEMP_DIR + gameBg);

            // Convert to URI and then to URL string for JavaFX
            String imageUrl = imageFile.toURI().toURL().toString();
            System.out.println(imageUrl);

            // Create a style string with the full URL path
            String backgroundStyle =
                    "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center; " + // Ensured space after semicolon
                    "-fx-background-color: rgba(0, 0, 0, 0.25); "; // Added trailing space to match setRandomBackground

            System.out.println("Setting background: " + imageUrl);

            // Apply the style directly to the root element
            scene.getRoot().setStyle(backgroundStyle);
        } catch (Exception e) {
            System.err.println("Error setting background image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}