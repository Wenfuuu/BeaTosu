package beat.osu.client.view.replay;

import beat.osu.client.Main;
import beat.osu.client.enums.HitResult;
import beat.osu.client.events.game.*;
import beat.osu.client.helper.*;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitObject;
import beat.osu.client.view.game.component.GameUI;
import beat.osu.client.view.game.component.PauseOverlay;
import beat.osu.client.view.shared.common.Page;
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

import java.util.ArrayList;
import java.util.Objects;

public class ReplayView extends Page implements GameEventListener {
    private final double OSU_WIDTH = 640.0;
    private final double OSU_HEIGHT = 480.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    private final double PLAYFIELD_OFFSET_X_IN_REF = 64.0;
    private final double PLAYFIELD_OFFSET_Y_IN_REF = 56.0;

    private final double circleSize;
    private double osuPixelDiameter;

    private StackPane root;
    private Pane replayPane;
    private GameUI uiPane;
    private PauseOverlay pauseOverlay;

    private Beatmap beatmap;
    private ArrayList<ReplayEventData> replayEvents;
    private final ReplayManager rm;
    private final GameEndData gameEndData;

    private Image[] digitImages;

    public ReplayView(Stage stage, Beatmap selectedBeatmap,
                      ArrayList<ReplayEventData> replayEvents,
                      GameEndData gameEndData) {
        super(stage);
        this.beatmap = selectedBeatmap;
        this.circleSize = selectedBeatmap.getCircleSize();
        this.replayEvents = replayEvents;
        this.rm = new ReplayManager(selectedBeatmap, replayEvents, inputManager);
        this.rm.addObserver(this);
        this.gameEndData = gameEndData;

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);

        initializeUI();
        loadBackground();
        handleEvent();
        updateLayout();
        BgmManager.prepareGameBgm();

        rm.startReplay();
    }

    private void initializeUI() {
        digitImages = new Image[10];
        for (int i = 0; i < 10; i++) {
            digitImages[i] = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/score-" + i + ".png")).toExternalForm());
        }

        uiPane = new GameUI();
        pauseOverlay = new PauseOverlay();
        createReplayPane();

        root.getChildren().addAll(replayPane, uiPane, pauseOverlay);
    }

    private void createReplayPane() {
        replayPane = new Pane();
        replayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
        for (HitObject hitObject : rm.getHitObjects()) {
            // gamePane.getChildren().add(hitObject.getNode());
            replayPane.getChildren().add(0, hitObject.getNode());
        }
    }

    private void handleEvent() {
        pauseOverlay.getContinueButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            rm.resumeReplay();
        });

        pauseOverlay.getRetryButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            ViewManager.showReplayView(beatmap, replayEvents, gameEndData);
        });

        pauseOverlay.getLeaveButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            ViewManager.showHomeView();
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
        replayPane.getChildren().add(hitImageView);
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
        replayPane.getChildren().add(spinScoreContainer);

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
        finalAnimation.setOnFinished(e -> replayPane.getChildren().remove(spinScoreContainer));

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
        fullAnimation.setOnFinished(e -> replayPane.getChildren().remove(hitImageView));
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
        replayPane.getChildren().add(hitImageView);

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
        fullAnimation.setOnFinished(e -> replayPane.getChildren().remove(hitImageView));
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

        double masterScaleFactor;
        double paneAspectRatio = paneWidth / paneHeight;

        if (paneAspectRatio > OSU_ASPECT_RATIO) {
            masterScaleFactor = paneHeight / OSU_HEIGHT;
        } else {
            masterScaleFactor = paneWidth / OSU_WIDTH;
        }

        double scaledRefScreenWidth = OSU_WIDTH * masterScaleFactor;
        double scaledRefScreenHeight = OSU_HEIGHT * masterScaleFactor;

        double viewportTopLeftX = (paneWidth - scaledRefScreenWidth) / 2.0;
        double viewportTopLeftY = (paneHeight - scaledRefScreenHeight) / 2.0;

        osuPixelDiameter = (54.4 - (4.48 * this.circleSize)) * 2.0;
        double unscaledOsuPixelRadius = osuPixelDiameter / 2.0;

        for (HitObject hitObject : rm.getHitObjects()) {
            double osuX = hitObject.getOsuX();
            double osuY = hitObject.getOsuY();

            double hitObjectX_in_RefScreen = PLAYFIELD_OFFSET_X_IN_REF + osuX;
            double hitObjectY_in_RefScreen = PLAYFIELD_OFFSET_Y_IN_REF + osuY;

            double finalObjectCenterX_onPane = viewportTopLeftX + (hitObjectX_in_RefScreen * masterScaleFactor);
            double finalObjectCenterY_onPane = viewportTopLeftY + (hitObjectY_in_RefScreen * masterScaleFactor);

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

    }

    @Override
    public void onShow() {

    }

    @Override
    public void update(GameEvent event) {
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
                ComboChangeData comboChangeData = event.getData(ComboChangeData.class);
                if (comboChangeData != null) {
                    uiPane.updateCombo(comboChangeData.getCombo());
                }
                break;
            case SCORE_CHANGED:
                ScoreChangeData scoreChangeData = event.getData(ScoreChangeData.class);
                if (scoreChangeData != null) {
                    uiPane.updateScore(scoreChangeData.getScore());
                }
                break;
            case HIT_OBJECT_MISSED:
                HitObjectEventData hitObjectData = event.getData(HitObjectEventData.class);
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
                HitObjectEventData hitData = event.getData(HitObjectEventData.class);
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
                AdditionalSpinEventData additionalSpinData = event.getData(AdditionalSpinEventData.class);
                if (additionalSpinData != null) {
                    HitObject hitObject = additionalSpinData.getHitObject();
                    int additionalSpins = additionalSpinData.getAdditionalSpin();
                    if (hitObject != null && additionalSpins > 0) {
                        showAdditionalSpinImage(hitObject, additionalSpins);
                    }
                }
                break;
            case INPUT_OVERLAY_CHANGED:
                InputOverlayData inputData = event.getData(InputOverlayData.class);
                if (inputData != null) {
                    uiPane.updateInputOverlay(inputData.isKey1Pressed(), inputData.isKey2Pressed());
                }
                break;
            case REPLAY_STARTED:
                System.out.println("Replay started");
                break;
            case REPLAY_PAUSED:
                System.out.println("Replay paused");
                pauseOverlay.setVisible(true);
                break;
            case REPLAY_RESUMED:
                System.out.println("Replay resumed");
                pauseOverlay.setVisible(false);
                break;
            case REPLAY_ENDED:
                ViewManager.showHomeView();
                break;
        }
    }
}
