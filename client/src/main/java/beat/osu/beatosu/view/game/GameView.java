package beat.osu.beatosu.view.game;

import beat.osu.beatosu.factory.HitObjectFactory;
import beat.osu.beatosu.game.GameEvent;
import beat.osu.beatosu.helper.*;
import beat.osu.beatosu.interfaces.Observer;
import beat.osu.beatosu.model.Beatmap;
import beat.osu.beatosu.model.HitCircle;
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

public class GameView extends Page implements Observer {
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


    private Pane root;
    private Pane gamePane;

    private final Beatmap beatmap;
    private GameManager gm;

    public GameView(Stage stage, Beatmap selectedBeatmap) {
        super(stage);
        this.beatmap = selectedBeatmap;
        this.circleSize = selectedBeatmap.getCircleSize();
        this.gm = new GameManager(selectedBeatmap, inputManager);
        this.gm.addObserver(this);
        initializeUI();
        loadBackground();
        handleEvent();
        updateLayout();

        BgmManager.playGameBgm();
        gm.startGame();
    }

    private void initializeUI() {
        createGamePane();

        root.getChildren().add(gamePane);
    }

    private void createGamePane() {
        gamePane = new Pane();
        for (HitObject hitObject : gm.getHitObjects()) {
            gamePane.getChildren().add(hitObject.getNode());
        }
    }

    private void handleEvent() {
        root.setOnMouseMoved(e -> {
//            currentMouseX = e.getSceneX();
//            currentMouseY = e.getSceneY();
            gm.updateMousePosition(e.getSceneX(), e.getSceneY());
        });
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

        // Update hit object positions
        for (HitObject hitObject : gm.getHitObjects()) {
            double osuX = hitObject.getOsuX();
            double osuY = hitObject.getOsuY();

            double hitObjectX_in_RefScreen = PLAYFIELD_OFFSET_X_IN_REF + osuX;
            double hitObjectY_in_RefScreen = PLAYFIELD_OFFSET_Y_IN_REF + osuY;

            double finalObjectCenterX_onPane = viewportTopLeftX + (hitObjectX_in_RefScreen * masterScaleFactor);
            double finalObjectCenterY_onPane = viewportTopLeftY + (hitObjectY_in_RefScreen * masterScaleFactor);

            double screenScaledRadius = unscaledOsuPixelRadius * masterScaleFactor;

            hitObject.updateVisuals(finalObjectCenterX_onPane, finalObjectCenterY_onPane, screenScaledRadius);
        }

//        for (Node node : root.getChildren()) { // Or iterate a dedicated list of HitObjects
//            if (node.getUserData() instanceof HitObject) {
//                HitObject hitObject = (HitObject) node.getUserData();
//
//                double osuX = hitObject.getOsuX(); // Coordinate within 512x384 playfield
//                double osuY = hitObject.getOsuY(); // Coordinate within 512x384 playfield
//
//                // --- ADD THIS DETAILED LOG ---
////                String objectType = hitObject.getClass().getSimpleName();
////                System.out.println("[UpdateLayout] Processing " + objectType + " (ID: " + System.identityHashCode(hitObject) +
////                        ") with initial OsuCoords: (" + osuX + ", " + osuY + ")");
//
                // a. Convert osuX, osuY (from 512x384 playfield) to their position
                //    within the 640x480 reference coordinate system.
//                double hitObjectX_in_RefScreen = PLAYFIELD_OFFSET_X_IN_REF + osuX;
//                double hitObjectY_in_RefScreen = PLAYFIELD_OFFSET_Y_IN_REF + osuY;
//
//                // b. Scale these reference coordinates by masterScaleFactor and add viewport offset
//                //    to find the final on-screen center position for the hit object.
//                double finalObjectCenterX_onPane = viewportTopLeftX + (hitObjectX_in_RefScreen * masterScaleFactor);
//                double finalObjectCenterY_onPane = viewportTopLeftY + (hitObjectY_in_RefScreen * masterScaleFactor);
//
//                // c. Calculate the on-screen scaled radius of the hit object.
//                //    The visual size is determined by masterScaleFactor.
//                double screenScaledRadius = unscaledOsuPixelRadius * masterScaleFactor;
//
//                // --- You can also log the result here for the same object ID ---
////                System.out.println("[UpdateLayout] " + objectType + " (ID: " + System.identityHashCode(hitObject) +
////                        ") calculated ScreenCenter: (" + finalObjectCenterX_onPane +
////                        ", " + finalObjectCenterY_onPane + ")");
//
//                // Inside the loop
//                hitObject.updateVisuals(finalObjectCenterX_onPane, finalObjectCenterY_onPane, screenScaledRadius);
//            }
//        }
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
        root.getChildren().addAll(backgroundOverlay);

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
    }

    @Override
    public void setLayout() {
        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);
    }

    @Override
    public void update(GameEvent event) {

    }
}
