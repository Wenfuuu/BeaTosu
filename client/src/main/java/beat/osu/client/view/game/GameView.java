package beat.osu.client.view.game;

import beat.osu.client.Main;
import beat.osu.client.enums.HitResult;
import beat.osu.client.events.game.*;
import beat.osu.client.helper.*;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitObject;
import beat.osu.client.view.game.component.FailOverlay;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.game.component.GameUI;
import beat.osu.client.view.game.component.PauseOverlay;
import beat.osu.client.view.game.component.ResultOverlay;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;

public class GameView extends Page implements GameEventListener {
    // Osu! playfield resolution (4:3)
    private final double OSU_WIDTH = 640.0;
    private final double OSU_HEIGHT = 480.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    // Offset of the 512x384 playfield's top-left (0,0) within the 640x480 reference
    // system
    // X: (640 - 512) / 2 = 64
    // Y: (480 - 384) / 2 + 8 = 48 + 8 = 56 (to account for the 8px downward shift
    // from true center)
    private final double PLAYFIELD_OFFSET_X_IN_REF = 64.0;
    private final double PLAYFIELD_OFFSET_Y_IN_REF = 56.0;

    private final double circleSize; // Default Circle Size (CS) if parsing fails
    private double osuPixelDiameter; // Diameter in original osu! coordinates

    private StackPane root;
    private Pane gamePane;
    private GameUI uiPane;
    private PauseOverlay pauseOverlay;
    private ResultOverlay resultOverlay;
    private FailOverlay failOverlay;

    private final Beatmap beatmap;
    private final GameManager gm;

    // additional spins
    private Image[] digitImages;

    public GameView(Stage stage, Beatmap selectedBeatmap) {
        super(stage);
        setupView();

        this.beatmap = selectedBeatmap;
        this.circleSize = selectedBeatmap.getCircleSize();
        this.gm = new GameManager(selectedBeatmap, inputManager, root.getWidth(), root.getHeight());
        this.gm.addListener(this);

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);

        initializeUI();
        loadBackground();
        handleEvent();
        updateLayout();
        BgmManager.getInstance().prepareGameBgm();

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            scene.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        gm.startGame();
    }

    private void initializeUI() {
        digitImages = new Image[10];
        for (int i = 0; i < 10; i++) {
            digitImages[i] = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/score-" + i + ".png")).toExternalForm());
        }

        uiPane = new GameUI();
        pauseOverlay = new PauseOverlay();
        resultOverlay = new ResultOverlay();
        failOverlay = new FailOverlay();
        createGamePane();

        root.getChildren().addAll(gamePane, uiPane, pauseOverlay, resultOverlay, failOverlay);
    }

    private void createGamePane() {
        gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
        for (HitObject hitObject : gm.getHitObjects()) {
            // gamePane.getChildren().add(hitObject.getNode());
            gamePane.getChildren().add(0, hitObject.getNode());
        }
    }

    private void handleEvent() {
        root.setOnMouseMoved(e -> {
            gm.updateMousePosition(e.getSceneX(), e.getSceneY());
        });

        pauseOverlay.getContinueButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            gm.resumeGame();
        });

        pauseOverlay.getRetryButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            ViewManager.getInstance().showGameView(beatmap);
        });

        pauseOverlay.getLeaveButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            gm.removeGameSession();
            ViewManager.getInstance().showHomeView();
        });

        resultOverlay.getRetryButton().setOnMouseClicked(e -> {
            ViewManager.getInstance().showGameView(beatmap);
        });

        resultOverlay.getReplayButton().setOnMouseClicked(e -> {
            ViewManager.getInstance().showReplayView(beatmap, gm.getReplayEvents());
        });

        resultOverlay.getBackButton().setOnMouseClicked(e -> {
            ViewManager.getInstance().showHomeView();
        });

        failOverlay.getRetryButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            ViewManager.getInstance().showGameView(beatmap);
        });

        failOverlay.getLeaveButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            ViewManager.getInstance().showHomeView();
        });
    }

    private void showHitImage(HitObject hitObject, HitResult hitResult,
            boolean perfectCombo, boolean imperfectOrMissed) {
        String imagePath = "";
        switch (hitResult) {
            case PERFECT:
                if (hitObject.isComboEnd()) {
                    if (perfectCombo) {
                        imagePath = "/assets/images/hit300g.png";
                    } else if (imperfectOrMissed) {
                        imagePath = "/assets/images/hit300.png";
                    } else {
                        imagePath = "/assets/images/hit300k.png";
                    }
                } else
                    imagePath = "/assets/images/hit300.png";
                break;
            case GREAT:
                if (hitObject.isComboEnd()) {
                    if (imperfectOrMissed) {
                        imagePath = "/assets/images/hit100.png";
                    } else {
                        imagePath = "/assets/images/hit100k.png";
                    }
                } else
                    imagePath = "/assets/images/hit100.png";
                break;
            case GOOD:
                imagePath = "/assets/images/hit50.png";
                break;
        }

        if (imagePath.isEmpty())
            return;
        Image hitImage = new Image(Objects.requireNonNull(Main.class
                .getResource(imagePath)).toExternalForm());
        ImageView hitImageView = new ImageView(hitImage);
        hitImageView.setFitWidth(50);
        hitImageView.setFitHeight(50);
        hitImageView.setOpacity(0);
        hitImageView.setScaleX(0.5);
        hitImageView.setScaleY(0.5);

        hitImageView.setLayoutX(hitObject.getScreenCenterX() - hitImageView.getFitWidth() / 2);
        hitImageView.setLayoutY(hitObject.getScreenCenterY() - hitImageView.getFitHeight() / 2);

        // Add the image to the game pane
        gamePane.getChildren().add(hitImageView);
        animateHitImage(hitImageView);
    }

    private void showAdditionalSpinImage(HitObject hitObject, int additionalSpins) {
        int totalScore = 1000 * additionalSpins;

        // Create a new container for this specific spin score display
        HBox spinScoreContainer = new HBox(2);
        String scoreStr = String.valueOf(totalScore);
        int numDigits = scoreStr.length();
        ImageView[] spinDigits = new ImageView[6]; // Use local array for this display

        for (int i = 0; i < numDigits; i++) {
            int digit = Character.getNumericValue(scoreStr.charAt(i));
            spinDigits[i] = new ImageView(digitImages[digit]);
            spinDigits[i].setFitWidth(50); // Smaller than hit images
            spinDigits[i].setFitHeight(60);
            spinDigits[i].setPreserveRatio(true);
            spinScoreContainer.getChildren().add(spinDigits[i]);
        }

        // Initially hide the container
        spinScoreContainer.setOpacity(0);

        // Add the container to the game pane
        gamePane.getChildren().add(spinScoreContainer);

        // Position the container at the center of the spinner
        // Note: You'll need to calculate the container's bounds for proper centering
        Platform.runLater(() -> {
            spinScoreContainer.autosize(); // Force size calculation
            double containerWidth = spinScoreContainer.getBoundsInLocal().getWidth();
            double containerHeight = spinScoreContainer.getBoundsInLocal().getHeight();

            spinScoreContainer.setLayoutX(hitObject.getScreenCenterX() - containerWidth / 2);
            spinScoreContainer.setLayoutY(hitObject.getScreenCenterY() - containerHeight / 2);

            // Start animation after positioning
            animateSpinScore(spinScoreContainer);
        });
    }

    private void animateSpinScore(HBox spinScoreContainer) {
        // Create fade-in and scale-up animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), spinScoreContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), spinScoreContainer);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);

        // Create a gentle bounce effect
        ScaleTransition bounce = new ScaleTransition(Duration.millis(150), spinScoreContainer);
        bounce.setFromX(1.2);
        bounce.setFromY(1.2);
        bounce.setToX(1.0);
        bounce.setToY(1.0);

        // Create upward movement animation
        TranslateTransition moveUp = new TranslateTransition(Duration.millis(800), spinScoreContainer);
        moveUp.setFromY(0);
        moveUp.setToY(-50); // Move up by 50 pixels

        // Create fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), spinScoreContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Combine initial animations
        ParallelTransition initialAnimation = new ParallelTransition(fadeIn, scaleUp);

        // Create the complete sequence
        SequentialTransition fullAnimation = new SequentialTransition(
                initialAnimation,
                bounce,
                new PauseTransition(Duration.millis(200)));

        // Create final fade-out with upward movement
        ParallelTransition finalAnimation = new ParallelTransition(moveUp, fadeOut);

        // Chain the animations
        fullAnimation.setOnFinished(e -> finalAnimation.play());

        // Remove the container when animation completes
        finalAnimation.setOnFinished(e -> gamePane.getChildren().remove(spinScoreContainer));

        fullAnimation.play();
    }

    private void animateHitImage(ImageView hitImageView) {
        // Create fade-in and scale-up animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), hitImageView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), hitImageView);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1.5);
        scaleUp.setToY(1.5);

        // Create a slight bounce effect
        ScaleTransition bounce = new ScaleTransition(Duration.millis(100), hitImageView);
        bounce.setFromX(1.5);
        bounce.setFromY(1.5);
        bounce.setToX(1.3);
        bounce.setToY(1.3);

        // Create fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), hitImageView);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Combine initial animations
        ParallelTransition initialAnimation = new ParallelTransition(fadeIn, scaleUp);

        // Create the complete sequence
        SequentialTransition fullAnimation = new SequentialTransition(
                initialAnimation,
                new PauseTransition(Duration.millis(400)),
                bounce,
                fadeOut);

        // Remove the image when animation completes
        fullAnimation.setOnFinished(e -> gamePane.getChildren().remove(hitImageView));
        fullAnimation.play();
    }

    private void showMissImage(HitObject hitObject) {
        Image hitImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit0.png")).toExternalForm());
        ImageView hitImageView = new ImageView(hitImage);
        hitImageView.setFitWidth(50);
        hitImageView.setFitHeight(50);
        hitImageView.setScaleX(2.0);
        hitImageView.setScaleY(2.0);

        hitImageView.setLayoutX(hitObject.getScreenCenterX() - hitImageView.getFitWidth() / 2);
        hitImageView.setLayoutY(hitObject.getScreenCenterY() - hitImageView.getFitHeight() / 2);

        // Add the image to the game pane
        gamePane.getChildren().add(hitImageView);

        // Create fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), hitImageView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.8);

        // Create scale-down animation (different effect for miss)
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), hitImageView);
        scaleDown.setFromX(2.0);
        scaleDown.setFromY(2.0);
        scaleDown.setToX(1.2);
        scaleDown.setToY(1.2);

        // Create a shake effect for miss
        TranslateTransition shake1 = new TranslateTransition(Duration.millis(50), hitImageView);
        shake1.setFromX(0);
        shake1.setToX(-5);

        TranslateTransition shake2 = new TranslateTransition(Duration.millis(50), hitImageView);
        shake2.setFromX(-5);
        shake2.setToX(5);

        TranslateTransition shake3 = new TranslateTransition(Duration.millis(50), hitImageView);
        shake3.setFromX(5);
        shake3.setToX(0);

        SequentialTransition shakeEffect = new SequentialTransition(shake1, shake2, shake3);

        // Create fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), hitImageView);
        fadeOut.setFromValue(0.8);
        fadeOut.setToValue(0);

        // Combine initial animations
        ParallelTransition initialAnimation = new ParallelTransition(fadeIn, scaleDown);

        // Create the complete sequence with shake effect
        SequentialTransition fullAnimation = new SequentialTransition(
                initialAnimation,
                new PauseTransition(Duration.millis(100)),
                shakeEffect,
                new PauseTransition(Duration.millis(200)),
                fadeOut);

        // Remove the image when animation completes
        fullAnimation.setOnFinished(e -> gamePane.getChildren().remove(hitImageView));
        fullAnimation.play();
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
        double paneWidth = root.getWidth();
        double paneHeight = root.getHeight();
        System.out.println("Pane Width: " + paneWidth);
        System.out.println("Pane Height: " + paneHeight);
        if (paneWidth <= 0 || paneHeight <= 0) {
            return;
        }

        // 1. Calculate the masterScaleFactor.
        // This factor determines how 1 game pixel (from the 640x480 reference) scales
        // to your actual screen.
        double masterScaleFactor;
        double paneAspectRatio = paneWidth / paneHeight;

        if (paneAspectRatio > OSU_ASPECT_RATIO) { // Pane is wider than 4:3 reference (e.g., 16:9 pane)
            masterScaleFactor = paneHeight / OSU_HEIGHT; // Scale based on height (e.g., 864 / 480 = 1.8)
        } else { // Pane is narrower or equal to 4:3 reference
            masterScaleFactor = paneWidth / OSU_WIDTH; // Scale based on width
        }

        // 2. Calculate the on-screen dimensions and top-left position of the scaled
        // 640x480 reference viewport.
        // This viewport will be centered on your pane.
        double scaledRefScreenWidth = OSU_WIDTH * masterScaleFactor;
        double scaledRefScreenHeight = OSU_HEIGHT * masterScaleFactor;

        double viewportTopLeftX = (paneWidth - scaledRefScreenWidth) / 2.0;
        double viewportTopLeftY = (paneHeight - scaledRefScreenHeight) / 2.0;

        // osuPixelDiameter is the diameter in unscaled osu!pixels (relative to 512x384
        // CS definitions)
        // This calculation remains the same: (54.4 - (4.48 * CS)) is radius * 2 for
        // diameter.
        osuPixelDiameter = (54.4 - (4.48 * this.circleSize)) * 2.0;
        double unscaledOsuPixelRadius = osuPixelDiameter / 2.0;

        // Update hit object positions
        for (HitObject hitObject : gm.getHitObjects()) {
            double osuX = hitObject.getOsuX();
            double osuY = hitObject.getOsuY();

            // a. Convert osuX, osuY (from 512x384 playfield) to their position
            // within the 640x480 reference coordinate system.
            double hitObjectX_in_RefScreen = PLAYFIELD_OFFSET_X_IN_REF + osuX;
            double hitObjectY_in_RefScreen = PLAYFIELD_OFFSET_Y_IN_REF + osuY;

            // b. Scale these reference coordinates by masterScaleFactor and add viewport
            // offset
            // to find the final on-screen center position for the hit object.
            double finalObjectCenterX_onPane = viewportTopLeftX + (hitObjectX_in_RefScreen * masterScaleFactor);
            double finalObjectCenterY_onPane = viewportTopLeftY + (hitObjectY_in_RefScreen * masterScaleFactor);

            // c. Calculate the on-screen scaled radius of the hit object.
            // The visual size is determined by masterScaleFactor.
            double screenScaledRadius = unscaledOsuPixelRadius * masterScaleFactor;

            hitObject.updateVisuals(finalObjectCenterX_onPane, finalObjectCenterY_onPane, screenScaledRadius);
        }
        // Update UI element positions
        VBox topRightPanel = (VBox) uiPane.getProperties().get("topRightPanel");
        VBox bottomLeftPanel = (VBox) uiPane.getProperties().get("bottomLeftPanel");
        StackPane inputOverlayPanel = (StackPane) uiPane.getProperties().get("inputOverlayPanel");

        if (topRightPanel != null) {
            topRightPanel.setLayoutX(paneWidth * 0.85);
            topRightPanel.setLayoutY(10);
        }
        if (bottomLeftPanel != null) {
            bottomLeftPanel.setLayoutX(10);
            bottomLeftPanel.setLayoutY(paneHeight * 0.925);
        }
        if (inputOverlayPanel != null) {
            inputOverlayPanel.autosize();
            inputOverlayPanel.setLayoutX(paneWidth - 100);
            inputOverlayPanel.setLayoutY(paneHeight * 0.5 - inputOverlayPanel.getHeight() / 2);
        }
    }

    @Override
    public void init() {
        root = new StackPane();

        // Create an overlay pane for semi-transparent background
        Pane backgroundOverlay = new Pane();
        backgroundOverlay.setStyle("-fx-background-color: rgba(18, 18, 18, 0.5);");
        backgroundOverlay.prefWidthProperty().bind(root.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(root.heightProperty());

        // Add the overlay pane to the root
        root.getChildren().addAll(backgroundOverlay);

        scene.setRoot(root);
    }

    @Override
    public void setLayout() {
        // ChangeListener<Number> resizeListener = (obs, oldVal, newVal) ->
        // updateLayout();
        // root.widthProperty().addListener(resizeListener);
        // root.heightProperty().addListener(resizeListener);
    }

    @Override
    public void onShow() {

    }

    @Override
    public void update(GameEvent event) {
//        Platform.runLater(() -> handleGameEvent(event));
        handleGameEvent(event);
    }

    private void handleGameEvent(GameEvent event) {
        switch (event.getType()) {
            case ACCURACY_CHANGED:
                Double newAccuracy = event.getData(Double.class);
                if (newAccuracy != null) {
                    uiPane.updateAccuracy(newAccuracy);
                }
                break;
            case COMBO_CHANGED:
                ComboChangeEvent comboChangeEvent = event.getData(ComboChangeEvent.class);
                if (comboChangeEvent != null) {
                    uiPane.updateCombo(comboChangeEvent.getCombo());
                }
                break;
            case SCORE_CHANGED:
                ScoreChangeEvent scoreChangeEvent = event.getData(ScoreChangeEvent.class);
                if (scoreChangeEvent != null) {
                    uiPane.updateScore(scoreChangeEvent.getScore());
                }
                break;
            case HIT_OBJECT_MISSED:
                HitObjectEvent hitObjectData = event.getData(HitObjectEvent.class);
                if (hitObjectData != null) {
                    HitObject hitObject = hitObjectData.getHitObject();
                    if (hitObject != null) {
                        showMissImage(hitObject);
                    }
                }
                break;
            case HEALTH_CHANGED:
                Double newHealth = event.getData(Double.class);
                if (newHealth != null) {
                    uiPane.updateHealth(newHealth / 100.0);
                }
                break;
            case HIT_OBJECT_HIT:
                HitObjectEvent hitData = event.getData(HitObjectEvent.class);
                if (hitData != null) {
                    HitObject hitObject = hitData.getHitObject();
                    HitResult hitResult = hitData.getHitResult();
                    boolean perfectCombo = hitData.isPerfectCombo();
                    boolean imperfectOrMissed = hitData.isImperfectOrMissed();
                    if (hitObject != null) {
                        showHitImage(hitObject, hitResult, perfectCombo, imperfectOrMissed);
                    }
                }
                break;
            case ADDITIONAL_SPIN:
                AdditionalSpinEvent additionalSpinData = event.getData(AdditionalSpinEvent.class);
                if (additionalSpinData != null) {
                    HitObject hitObject = additionalSpinData.getHitObject();
                    int additionalSpins = additionalSpinData.getAdditionalSpin();
                    if (hitObject != null && additionalSpins > 0) {
                        showAdditionalSpinImage(hitObject, additionalSpins);
                    }
                }
                break;
            case INPUT_OVERLAY_CHANGED:
                InputOverlayEvent inputData = event.getData(InputOverlayEvent.class);
                if (inputData != null) {
                    uiPane.updateInputOverlay(inputData.isKey1Pressed(), inputData.isKey2Pressed());
                }
                break;
            case GAME_STARTED:
                System.out.println("show countdown here");
                break;
            case GAME_PAUSED:
                System.out.println("pausing game, show pause menu here");
                pauseOverlay.setVisible(true);
                break;
            case GAME_RESUMED:
                System.out.println("resuming game, hide pause menu here");
                pauseOverlay.setVisible(false);
                break;
            case GAME_ENDED:
                System.out.println("game ended, show result overlay here");
                GameEndEvent gameEndEvent = event.getData(GameEndEvent.class);
                if (gameEndEvent != null) {
                    resultOverlay.updateResult(gameEndEvent, beatmap);
                }
                // uiPane.setVisible(false);
                uiPane.getHideTransition().play();
                uiPane.getHideTransition().setOnFinished(e -> {
                    resultOverlay.setVisible(true);
                    resultOverlay.getShowTransition().play();
                });
                break;
            case GAME_FAILED:
                System.out.println("game failed, show fail overlay here");
                failOverlay.showFailOverlay();
                break;
        }
    }
}
