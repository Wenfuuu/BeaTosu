package beat.osu.client.helper;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import beat.osu.client.Main;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BackgroundManager {
    private static final String BACKGROUNDS_DIR = "/assets/images/background/";
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

        int randomIndex = random.nextInt(backgroundFiles.size());
        return backgroundFiles.get(randomIndex);
    }

    private static void loadBackgroundFiles() {
        backgroundFiles = new ArrayList<>();

        try {
            // Try to load as resources (works in IDE with file:/, NOT in JAR)
            URL url = Main.class.getResource(BACKGROUNDS_DIR);
            if (url != null && url.getProtocol().equals("file")) {
                // Development mode: load files directly from filesystem
                File dir = new File(url.toURI());
                File[] files = dir.listFiles((d, name) ->
                        name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".png") ||
                                name.toLowerCase().endsWith(".jpeg")
                );
                if (files != null) {
                    for (File file : files) {
                        backgroundFiles.add(file.getName());
                    }
                }
            } else {
                // JAR mode: resources are packed, can't list — fallback to hardcoded
                for (int i = 1; i <= 10; i++) {
                    String name = "bg-" + i + ".png";
                    if (Main.class.getResource(BACKGROUNDS_DIR + name) != null) {
                        backgroundFiles.add(name);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load background images: " + e.getMessage());
        }

        if (backgroundFiles.isEmpty()) {
            System.err.println("No background images found in: " + BACKGROUNDS_DIR);
        } else {
            System.out.println("Loaded " + backgroundFiles.size() + " background images");
        }
    }

    public static void setResultButtonBackground(Button button, String fileName) {
        String imagePath = "/assets/images/button/pause-menu/" + fileName;
        URL imageUrl = Main.class.getResource(imagePath);
        if(imageUrl == null) return;

        Image image = new Image(imageUrl.toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(75);
        imageView.setPreserveRatio(true);

        button.setGraphic(imageView);
    }

    public static void setPauseButtonBackground(Button button, String fileName) {
        String imagePath = "/assets/images/button/pause-menu/" + fileName;
        URL imageUrl = Main.class.getResource(imagePath);
        if(imageUrl == null) return;

        Image image = new Image(imageUrl.toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(ScreenManager.SCREEN_HEIGHT / 6);
        imageView.setPreserveRatio(true);

        button.setGraphic(imageView);
    }

    public static void setRandomBackground(Scene scene) {
        String randomBg = getRandomBackgroundURL();
        URL imageUrl = Main.class.getResource(BACKGROUNDS_DIR + randomBg);
        if (imageUrl == null) {
            System.err.println("Background image not found: " + randomBg);
            return;
        }

        String backgroundStyle = "-fx-background-image: url('" + imageUrl.toExternalForm() + "'); " +
                "-fx-background-size: cover; " +
                "-fx-background-position: center center;";
        scene.getRoot().setStyle(backgroundStyle);
        updateOverlaySmooth(scene);
    }

    // get the beatmap set background
    public static void setBeatmapBackground(Region region) {
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        String beatmapBg = OsuParser.getBgFile();
        if (beatmapBg == null || beatmapBg.isEmpty()) {
            System.err.println("No background file found for the beatmap.");
            return;
        }

        try {
            File tempDir = ResourceManager.getBeatmapDirectory();
            File beatmapDir = new File(tempDir, String.valueOf(beatmap.getBeatmapSetId()));
            File imageFile = new File(beatmapDir, beatmapBg);
            System.out.println(imageFile.getAbsolutePath());
            if (!imageFile.exists()) {
                System.err.println("Background image not found: " + imageFile.getAbsolutePath());
                return;
            }

            Image image = new Image(new FileInputStream(imageFile));
            region.setBackground(new Background(
                    new BackgroundImage(
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
                    )
            ));
        } catch (Exception e) {
            System.err.println("Error setting beatmap background: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // get the beatmap background, not beatmap set
    public static void setGameBackground(Scene scene) {
        System.out.println("setting game background");
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        String gameBg = OsuParser.getBgFile();

        try {
            File tempDir = ResourceManager.getBeatmapDirectory();
            File beatmapDir = new File(tempDir, String.valueOf(beatmap.getBeatmapSetId()));
            File imageFile = new File(beatmapDir, gameBg);
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

    public static void setModalBeatmapBackground(Region backgroundLayer, Beatmap beatmap) {
        if (backgroundLayer == null || beatmap == null) {
            System.err.println("Background layer or beatmap is null");
            return;
        }

        String beatmapBg = OsuParser.getBgFile();
        if (beatmapBg == null || beatmapBg.isEmpty()) {
            System.err.println("No background file found for the beatmap.");
            return;
        }

        try {
            File tempDir = ResourceManager.getBeatmapDirectory();
            File beatmapDir = new File(tempDir, String.valueOf(beatmap.getBeatmapSetId()));
            File imageFile = new File(beatmapDir, beatmapBg);
            
            if (!imageFile.exists()) {
                System.err.println("Background image not found: " + imageFile.getAbsolutePath());
                return;
            }

            Image image = new Image(new FileInputStream(imageFile));
            backgroundLayer.setBackground(new Background(
                    new BackgroundImage(
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
                    )
            ));
            
            System.out.println("Modal beatmap background set successfully for: " + beatmapBg);
        } catch (Exception e) {
            System.err.println("Error setting modal beatmap background: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setDarkBackground(Scene scene, boolean darkMode) {
        darkModeEnabled = darkMode;
        updateOverlaySmooth(scene);
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
