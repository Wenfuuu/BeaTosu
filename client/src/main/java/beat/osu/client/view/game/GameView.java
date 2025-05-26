package beat.osu.client.view.game;

import beat.osu.client.enums.GameState;
import beat.osu.client.game.GameEvent;
import beat.osu.client.helper.*;
import beat.osu.client.interfaces.Observer;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitObject;
import beat.osu.client.view.Page;
import beat.osu.client.view.game.component.GameUI;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    private GameUI uiPane;

    private final Beatmap beatmap;
    private final GameManager gm;

    public GameView(Stage stage, Beatmap selectedBeatmap) {
        super(stage);
        this.beatmap = selectedBeatmap;
        this.circleSize = beatmap.getCircleSize();
        this.gm = new GameManager(selectedBeatmap, inputManager);
        this.gm.addObserver(this);

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);

        initializeUI();
        loadBackground();
        handleEvent();
        updateLayout();

        gm.startGame();
    }

    private void initializeUI() {
        uiPane = new GameUI();
        createGamePane();

        root.getChildren().addAll(gamePane, uiPane);
    }

    private void createGamePane() {
        gamePane = new Pane();
        for (HitObject hitObject : gm.getHitObjects()) {
            gamePane.getChildren().add(hitObject.getNode());
        }
    }

    private void handleEvent() {
        root.setOnMouseMoved(e -> {
            gm.updateMousePosition(e.getSceneX(), e.getSceneY());
        });
    }

    private void togglePause() {
        if(gm.getGameState() == GameState.PAUSED) {
            gm.startGame();
//            uiPane.hidePauseMenu();
        } else if(gm.getGameState() == GameState.PLAYING) {
            gm.pauseGame();
//            uiPane.showPauseMenu();
        }
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

        // Update UI element positions
        VBox topRightPanel = (VBox) uiPane.getProperties().get("topRightPanel");
        VBox bottomLeftPanel = (VBox) uiPane.getProperties().get("bottomLeftPanel");

        if (topRightPanel != null) {
            topRightPanel.setLayoutX(paneWidth - 150); // 150px from right edge
            topRightPanel.setLayoutY(10);
        }

        if (bottomLeftPanel != null) {
            bottomLeftPanel.setLayoutX(10);
            bottomLeftPanel.setLayoutY(paneHeight - 30); // 30px from bottom
        }
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
//        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
//        root.widthProperty().addListener(resizeListener);
//        root.heightProperty().addListener(resizeListener);
    }

    @Override
    public void update(GameEvent event) {
        Platform.runLater(() -> handleGameEvent(event));
    }

    private void handleGameEvent(GameEvent event) {
        switch (event.getType()) {
            case ACCURACY_CHANGED:
                Double newAccuracy = event.getData(Double.class);
                if (newAccuracy != null) {
                    String accuracyText = String.format("%.2f%%", newAccuracy);
                    uiPane.getAccuracyLabel().setText(accuracyText);
                }
        }
    }
}
