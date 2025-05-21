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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class GameView extends Page {
    // Osu! playfield resolution (4:3)
    private final double OSU_WIDTH = 512.0;
    private final double OSU_HEIGHT = 384.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    private double circleSize; // Default Circle Size (CS) if parsing fails
    private double osuPixelDiameter;   // Diameter in original osu! coordinates

    // Game Loop Timer
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;

    private Pane root;

    public GameView(Stage stage, Beatmap selectedBeatmap) {
        super(stage);
        processBeatmap(selectedBeatmap);
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

    private void addHitObject(String data, Beatmap selectedBeatmap) {
        root.getChildren().add(HitObjectFactory.createHitObject(data, selectedBeatmap).getNode());
    }

    private void processBeatmap(Beatmap selectedBeatmap) {
        //search & extract .osz -> stored in temp folder
//     absolute path => src\main\resources\assets\beatmap\567148 Sayuri - Heikousen.osz
        String oszPath = String.format("./src/main/resources/assets/beatmap/%d %s - %s.osz", selectedBeatmap.getBeatmapSet().getBeatmapSetId(),
                selectedBeatmap.getBeatmapSet().getArtist(), selectedBeatmap.getBeatmapSet().getTitle());
        File oszFile = new File(oszPath);
        File outputDir = new File("./src/main/resources/assets/temp");
        try {
            OszExtractor.extractOsz(oszFile, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //parse the selected .osu file
        String osuPath = String.format("./src/main/resources/assets/temp/%s - %s (%s) [%s].osu",
                selectedBeatmap.getBeatmapSet().getArtist(),
                selectedBeatmap.getBeatmapSet().getTitle(),
                selectedBeatmap.getBeatmapSet().getCreator(),
                selectedBeatmap.getVersion());
        File osuFile = new File(osuPath);
        try {
            OsuParser.parse(osuFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        circleSize = selectedBeatmap.getCircleSize();

        for(String data: OsuParser.getHitObjects()) {
            addHitObject(data, selectedBeatmap);
        }
    }

    private void updateLayout() {
        double paneWidth = root.getWidth();
        double paneHeight = root.getHeight();
        if (paneWidth <= 0 || paneHeight <= 0) {
            return;
        }

        // --- Calculate Hit Object Diameter based on Beatmap CS ---
        osuPixelDiameter = (54.4 - (4.48 * circleSize)) * 2.0;

        double paneRatio = paneWidth / paneHeight;
        double scaleFactor;
        double scaledPlayfieldWidth;
        double scaledPlayfieldHeight;

        // Determine the scale factor based on the limiting dimension (width or height)
        // to maintain the 4:3 aspect ratio
        if (paneRatio > OSU_ASPECT_RATIO) {
            // Pane is wider than 4:3 (letterboxed), height is the limit
            scaleFactor = paneHeight / OSU_HEIGHT;
            scaledPlayfieldHeight = paneHeight;
            scaledPlayfieldWidth = OSU_WIDTH * scaleFactor;
        } else {
            // Pane is narrower than or equal to 4:3 (pillarboxed), width is the limit
            scaleFactor = paneWidth / OSU_WIDTH;
            scaledPlayfieldWidth = paneWidth;
            scaledPlayfieldHeight = OSU_HEIGHT * scaleFactor;
        }

        // Calculate the offsets needed to center the scaled playfield within the pane
        double offsetX = (paneWidth - scaledPlayfieldWidth) / 2.0;
        double offsetY = (paneHeight - scaledPlayfieldHeight) / 2.0;

        // Calculate the scaled size of the hit object
        double scaledHitObjectDiameter = osuPixelDiameter * scaleFactor;
        // The amount to shift left/up to center the node
        double centerAdjustment = scaledHitObjectDiameter / 2.0;

        // Iterate through the Nodes in the pane
        for (Node node : root.getChildren()) {
            if (node.getUserData() instanceof HitObject) {
//                System.out.println(node.getUserData());
                HitObject hitObject = (HitObject) node.getUserData();

                // Get original osu! coordinates from the HitObject
                double osuX = hitObject.getOsuX();
                double osuY = hitObject.getOsuY();

                // Calculate the final scaled and centered position on the pane
                double centerX = (osuX * scaleFactor) + offsetX;
                double centerY = (osuY * scaleFactor) + offsetY;

                double finalX = centerX - centerAdjustment;
                double finalY = centerY - centerAdjustment;

                hitObject.setPosition(finalX, finalY);
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
