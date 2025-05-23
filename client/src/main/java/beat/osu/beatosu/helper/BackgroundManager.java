package beat.osu.beatosu.helper;

import beat.osu.beatosu.utils.OsuParser;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackgroundManager {
    private static final String BACKGROUNDS_DIR = "./src/main/resources/assets/backgrounds/";
    private static final String TEMP_DIR = "./src/main/resources/assets/temp/";
    private static final Random random = new Random();
    private static List<String> backgroundFiles = null;
    private static boolean darkModeEnabled = false;

    private static final double DEFAULT_DARK_OPACITY = 0.5;
    private static final double DEFAULT_LIGHT_OPACITY = 0;
    private static final Duration TRANSITION_DURATION = Duration.millis(300);

    private static Rectangle currentOverlay = null;

    public static String getRandomBackgroundURL() {
        if (backgroundFiles == null) {
            loadBackgroundFiles();
        }

        if (backgroundFiles.isEmpty()) {
            return "online_background_422fab3bf0c3af0234ee21be511bc3a9.jpg";
        }

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

    public static void setRandomBackground(Scene scene) {
        String randomBg = getRandomBackgroundURL();
        try {
            File imageFile = new File(BACKGROUNDS_DIR + randomBg);
            String imageUrl = imageFile.toURI().toURL().toString();

            String backgroundStyle = "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center;";

            scene.getRoot().setStyle(backgroundStyle);

            updateOverlaySmooth(scene);

        } catch (Exception e) {
            System.err.println("Error setting background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setGameBackground(Scene scene) {
        System.out.println("setting game background");
        String gameBg = OsuParser.getBgFile();

        try {
            File imageFile = new File(TEMP_DIR + gameBg);
            String imageUrl = imageFile.toURI().toURL().toString();

            String backgroundStyle =
                    "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center; " +
                    "-fx-background-color: rgba(0, 0, 0, 0.25); ";

            scene.getRoot().setStyle(backgroundStyle);
        } catch (Exception e) {
            System.err.println("Error setting background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setDarkBackground(Scene scene, boolean darkMode) {
        darkModeEnabled = darkMode;
        updateOverlaySmooth(scene);
        System.out.println("Dark mode set to " + (darkModeEnabled ? "enabled" : "disabled"));
    }

    private static void updateOverlaySmooth(Scene scene) {
        if (!(scene.getRoot() instanceof StackPane)) {
            System.err.println("Root is not a StackPane, cannot apply overlay");
            return;
        }

        StackPane root = (StackPane) scene.getRoot();
        double targetOpacity = darkModeEnabled ? DEFAULT_DARK_OPACITY : DEFAULT_LIGHT_OPACITY;

        if (currentOverlay == null) {
            createInitialOverlay(root, targetOpacity);
        } else {
            animateOverlayOpacity(currentOverlay, targetOpacity);
        }
    }

    private static void createInitialOverlay(StackPane root, double opacity) {
        root.getChildren().removeIf(node -> node instanceof Rectangle
                && node.getId() != null && node.getId().equals("backgroundOverlay"));

        Rectangle overlay = new Rectangle();
        overlay.setId("backgroundOverlay");
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(opacity);

        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());

        root.getChildren().add(0, overlay);
        currentOverlay = overlay;
    }

    private static void animateOverlayOpacity(Rectangle overlay, double targetOpacity) {
        overlay.getProperties().values().removeIf(value -> value instanceof Timeline);

        FadeTransition fadeTransition = new FadeTransition(TRANSITION_DURATION, overlay);
        fadeTransition.setFromValue(overlay.getOpacity());
        fadeTransition.setToValue(targetOpacity);

        overlay.getProperties().put("fadeTransition", fadeTransition);

        fadeTransition.play();
    }
}
