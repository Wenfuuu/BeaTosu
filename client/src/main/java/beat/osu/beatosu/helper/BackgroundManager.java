package beat.osu.beatosu.helper;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackgroundManager {
    private static final String BACKGROUNDS_DIR = "./src/main/resources/assets/backgrounds/";
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

    public static void setRandomBackground(javafx.scene.Scene scene) {
        String randomBg = getRandomBackgroundURL();

        try {
            // Create a File object for the image
            File imageFile = new File(BACKGROUNDS_DIR + randomBg);

            // Convert to URI and then to URL string for JavaFX
            String imageUrl = imageFile.toURI().toURL().toString();

            // Create a style string with the full URL path
            String backgroundStyle = "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center;";

            System.out.println("Setting background: " + imageUrl);

            // Apply the style directly to the root element
            scene.getRoot().setStyle(backgroundStyle);
        } catch (Exception e) {
            System.err.println("Error setting background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setRandomBackgroundToRegion(Region region) {
        try {
            String randomBg = getRandomBackgroundURL();
            File imageFile = new File(BACKGROUNDS_DIR + randomBg);

            if (!imageFile.exists()) {
                System.err.println("Background image not found: " + imageFile.getAbsolutePath());
                return;
            }

            Image image = new Image(new FileInputStream(imageFile));

            BackgroundImage backgroundImage = new BackgroundImage(
                    image,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(
                            BackgroundSize.AUTO,
                            BackgroundSize.AUTO,
                            false,
                            false,
                            true,
                            true
                    )
            );

            Background background = new Background(backgroundImage);

            region.setBackground(background);

            System.out.println("Set background image: " + imageFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error setting background image to region: " + e.getMessage());
            e.printStackTrace();
        }
    }
}