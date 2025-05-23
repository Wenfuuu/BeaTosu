package beat.osu.beatosu.view.game;

import beat.osu.beatosu.factory.HitObjectFactory;
import beat.osu.beatosu.helper.BackgroundManager;
import beat.osu.beatosu.helper.BgmManager;
import beat.osu.beatosu.helper.ScreenManager;
import beat.osu.beatosu.model.Beatmap;
import beat.osu.beatosu.model.HitObject;
import beat.osu.beatosu.utils.OsuParser;
import beat.osu.beatosu.utils.OszExtractor;
import beat.osu.beatosu.view.Page;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameView extends Page {
    // Osu! playfield resolution (4:3)
    private final double OSU_WIDTH = 640.0;
    private final double OSU_HEIGHT = 480.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    // Offset of the 512x384 playfield's top-left (0,0) within the 640x480 reference system
    // X: (640 - 512) / 2 = 64
    // Y: (480 - 384) / 2 + 8 = 48 + 8 = 56 (to account for the 8px downward shift from true center)
    private final double PLAYFIELD_OFFSET_X_IN_REF = 64.0;
    private final double PLAYFIELD_OFFSET_Y_IN_REF = 56.0;

    private double circleSize; // Default Circle Size (CS) if parsing fails
    private double osuPixelDiameter;   // Diameter in original osu! coordinates

    // Game Loop Timer
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;

    private Pane root;
    private final Set<KeyCode> previousKeys = new HashSet<>();
    private final Beatmap beatmap;

    public GameView(Stage stage, Beatmap selectedBeatmap) {
        super(stage);
        this.beatmap = selectedBeatmap;
        processBeatmap();
        loadBackground();

        updateLayout();
        BgmManager.playGameBgm();
        startGame();
    }

    private void loadBackground() {
        try {
            BackgroundManager.setGameBackground(scene);
        } catch (Exception e) {
            System.err.println("Error setting background for HomeView: " + e.getMessage());
            e.printStackTrace();
            root.setStyle("-fx-background-color: #121212;");
        }
    }

    private void addHitObject(String data) {
        root.getChildren().add(HitObjectFactory.createHitObject(data, beatmap).getNode());
    }

    private void processBeatmap() {
        //search & extract .osz -> stored in temp folder
//     absolute path => src\main\resources\assets\beatmap\567148 Sayuri - Heikousen.osz
        String oszPath = String.format("./src/main/resources/assets/beatmap/%d %s - %s.osz", beatmap.getBeatmapSet().getBeatmapSetId(),
                beatmap.getBeatmapSet().getArtist(), beatmap.getBeatmapSet().getTitle());
        File oszFile = new File(oszPath);
        File outputDir = new File("./src/main/resources/assets/temp");
        try {
            OszExtractor.extractOsz(oszFile, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //parse the selected .osu file
        String osuPath = String.format("./src/main/resources/assets/temp/%s - %s (%s) [%s].osu",
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getTitle(),
                beatmap.getBeatmapSet().getCreator(),
                beatmap.getVersion());
        File osuFile = new File(osuPath);
        try {
            OsuParser.parse(osuFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        circleSize = beatmap.getCircleSize();

        for(String data: OsuParser.getHitObjects()) {
            addHitObject(data);
        }
    }

    private void updateLayout() {
//        double paneWidth = ScreenManager.SCREEN_WIDTH;
//        double paneHeight = ScreenManager.SCREEN_HEIGHT;
        double paneWidth = root.getWidth();
        double paneHeight = root.getHeight();
        System.out.println("Pane Width: " + paneWidth);
        System.out.println("Pane Height: " + paneHeight);
        if (paneWidth <= 0 || paneHeight <= 0) {
            return;
        }

        // 1. Calculate the masterScaleFactor.
        // This factor determines how 1 game pixel (from the 640x480 reference) scales to your actual screen.
        double masterScaleFactor;
        double paneAspectRatio = paneWidth / paneHeight;

        if (paneAspectRatio > OSU_ASPECT_RATIO) { // Pane is wider than 4:3 reference (e.g., 16:9 pane)
            masterScaleFactor = paneHeight / OSU_HEIGHT; // Scale based on height (e.g., 864 / 480 = 1.8)
        } else { // Pane is narrower or equal to 4:3 reference
            masterScaleFactor = paneWidth / OSU_WIDTH;   // Scale based on width
        }

        // 2. Calculate the on-screen dimensions and top-left position of the scaled 640x480 reference viewport.
        // This viewport will be centered on your pane.
        double scaledRefScreenWidth = OSU_WIDTH * masterScaleFactor;
        double scaledRefScreenHeight = OSU_HEIGHT * masterScaleFactor;

        double viewportTopLeftX = (paneWidth - scaledRefScreenWidth) / 2.0;
        double viewportTopLeftY = (paneHeight - scaledRefScreenHeight) / 2.0;

        // osuPixelDiameter is the diameter in unscaled osu!pixels (relative to 512x384 CS definitions)
        // This calculation remains the same: (54.4 - (4.48 * CS)) is radius * 2 for diameter.
        osuPixelDiameter = (54.4 - (4.48 * this.circleSize)) * 2.0;
        double unscaledOsuPixelRadius = osuPixelDiameter / 2.0;

        for (Node node : root.getChildren()) { // Or iterate a dedicated list of HitObjects
            if (node.getUserData() instanceof HitObject) {
                HitObject hitObject = (HitObject) node.getUserData();

                double osuX = hitObject.getOsuX(); // Coordinate within 512x384 playfield
                double osuY = hitObject.getOsuY(); // Coordinate within 512x384 playfield

                // a. Convert osuX, osuY (from 512x384 playfield) to their position
                //    within the 640x480 reference coordinate system.
                double hitObjectX_in_RefScreen = PLAYFIELD_OFFSET_X_IN_REF + osuX;
                double hitObjectY_in_RefScreen = PLAYFIELD_OFFSET_Y_IN_REF + osuY;

                // b. Scale these reference coordinates by masterScaleFactor and add viewport offset
                //    to find the final on-screen center position for the hit object.
                double finalObjectCenterX_onPane = viewportTopLeftX + (hitObjectX_in_RefScreen * masterScaleFactor);
                double finalObjectCenterY_onPane = viewportTopLeftY + (hitObjectY_in_RefScreen * masterScaleFactor);

                // c. Calculate the on-screen scaled radius of the hit object.
                //    The visual size is determined by masterScaleFactor.
                double screenScaledRadius = unscaledOsuPixelRadius * masterScaleFactor;

                hitObject.updateVisuals(finalObjectCenterX_onPane, finalObjectCenterY_onPane, screenScaledRadius);
            }
        }
    }

    private void startGame() {
        System.out.println("Starting game");
        startTimeNanos = -1; // Reset start time

        // Stop existing loop if it's running (e.g., restarting the game)
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Record the start time on the very first frame
                if (startTimeNanos == -1) {
                    startTimeNanos = now;
                }

                // Calculate elapsed time in nanoseconds and convert to milliseconds
                long elapsedNanos = now - startTimeNanos;
                long elapsedMillis = elapsedNanos / 1_000_000;

                // --- Update Game Logic ---
                for (Node node : new ArrayList<>(root.getChildren())) {
                    if (node != null && node.getUserData() instanceof HitObject) {
                        HitObject hitObject = (HitObject) node.getUserData();
                        hitObject.update(elapsedMillis);

                        // --- Handle Key Presses ---
                        Set<KeyCode> currentKeys = inputManager.getPressedKeys();
                        boolean zPressed = currentKeys.contains(KeyCode.Z) && !previousKeys.contains(KeyCode.Z);
                        boolean xPressed = currentKeys.contains(KeyCode.X) && !previousKeys.contains(KeyCode.X);
                        if ((zPressed || xPressed) && hitObject.isVisible() && !hitObject.isHit()) {
                            // handle hit
                            long timingError = now - hitObject.getHitTime(); // Calculate hit timing
                            hitObject.setHit(true);
                            hitObject.playHitEffect();
                        }

                        previousKeys.clear();
                        previousKeys.addAll(currentKeys);
                    }
                }

                // --- Other game updates could go here ---
                // (e.g., check win/loss conditions, update score display)
            }
        };

        gameLoop.start();
    }

    @Override
    public void init() {
        root = new Pane();

        // Create an overlay pane for semi-transparent background
        Pane backgroundOverlay = new Pane();
        backgroundOverlay.setStyle("-fx-background-color: rgba(18, 18, 18, 0.5);");
        backgroundOverlay.prefWidthProperty().bind(root.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(root.heightProperty());

        // Add the overlay pane to the root
        root.getChildren().add(backgroundOverlay);

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
    }

    @Override
    public void setLayout() {
        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);
    }
}
