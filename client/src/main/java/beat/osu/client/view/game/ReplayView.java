package beat.osu.client.view.game;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.controller.UserController;
import beat.osu.client.enums.HitResult;
import beat.osu.client.events.game.AdditionalSpinEvent;
import beat.osu.client.events.game.ComboChangeEvent;
import beat.osu.client.events.game.CursorMoveEvent;
import beat.osu.client.events.game.GameEvent;
import beat.osu.client.events.game.HitObjectEvent;
import beat.osu.client.events.game.InputOverlayEvent;
import beat.osu.client.events.game.ReplayEvent;
import beat.osu.client.events.game.ScoreChangeEvent;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ReplayManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.interfaces.game.CoordinateConverter;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitObject;
import beat.osu.client.view.game.component.GameUI;
import beat.osu.client.view.game.component.PauseOverlay;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.shared.replay.EndReplayButton;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ReplayView extends Page implements GameEventListener, CoordinateConverter {
    private final double OSU_WIDTH = 640.0;
    private final double OSU_HEIGHT = 480.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    private final double PLAYFIELD_OFFSET_X_IN_REF = 64.0;
    private final double PLAYFIELD_OFFSET_Y_IN_REF = 56.0;

    private double INITIAL_OPACITY;

    private final double circleSize;
    private double osuPixelDiameter;

    private StackPane root;
    private Pane backgroundOverlay;
    private Pane replayPane;
    private GameUI uiPane;
    private PauseOverlay pauseOverlay;

    private Beatmap beatmap;
    private ArrayList<ReplayEvent> replayEvents;
    private final ReplayManager rm;

    private Image[] digitImages;
    private ArrayList<Animation> animationList;
    private ImageView cursorImage;
    private double currentMasterScaleFactor = 1.0;
    private double currentViewportTopLeftX = 0.0;
    private double currentViewportTopLeftY = 0.0;

    private double originalRecordingWidth = 0.0;
    private double originalRecordingHeight = 0.0;

    private UserController userController;

    private Integer playingUserId;
    private HBox marqueeContainer;
    private Label marqueeLabel;
    private EndReplayButton endReplayButton;
    private TranslateTransition marqueeAnimation;

    public ReplayView(Stage stage, UserController userController, Beatmap selectedBeatmap, int playingUserId, ArrayList<ReplayEvent> replayEvents) {
        super(stage);
        setupView();

        this.userController = userController;

        this.playingUserId = playingUserId;
        this.beatmap = selectedBeatmap;
        this.circleSize = selectedBeatmap.getCircleSize();
        this.replayEvents = replayEvents;
        this.rm = new ReplayManager(selectedBeatmap, replayEvents, inputManager, this);
        this.rm.addListener(this);

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

        rm.startReplay();
    }

    private void initializeUI() {
        digitImages = new Image[10];
        for (int i = 0; i < 10; i++) {
            digitImages[i] = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/score-" + i + ".png")).toExternalForm());
        }

        animationList = new ArrayList<>();

        uiPane = new GameUI();
        pauseOverlay = new PauseOverlay();
        cursorImage = new ImageView(new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/cursor.png")).toExternalForm(),
                32, 32, true, true));

        createReplayPane();
        createMarqueeText();

        root.getChildren().addAll(replayPane, uiPane, marqueeContainer, pauseOverlay);
    }

    private void createReplayPane() {
        replayPane = new Pane();
        replayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
        for (HitObject hitObject : rm.getHitObjects()) {
            // gamePane.getChildren().add(hitObject.getNode());
            replayPane.getChildren().add(0, hitObject.getNode());
        }
        replayPane.getChildren().add(cursorImage);
    }

    private void handleEvent() {
        pauseOverlay.getContinueButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            rm.resumeReplay();
        });

        pauseOverlay.getRetryButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("pause-click.wav");
            ViewManager.getInstance().showReplayView(beatmap, playingUserId, replayEvents);
        });

        pauseOverlay.getLeaveButton().setOnMouseClicked(e -> {
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
        finalAnimation.setOnFinished(e -> {
            replayPane.getChildren().remove(spinScoreContainer);
            animationList.remove(fullAnimation);
            animationList.remove(finalAnimation);
        });

        animationList.add(fullAnimation);
        animationList.add(finalAnimation);

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
        fullAnimation.setOnFinished(e -> {
            replayPane.getChildren().remove(hitImageView);
            animationList.remove(fullAnimation);
        });

        // Add to animation list BEFORE starting
        animationList.add(fullAnimation);
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
        fullAnimation.setOnFinished(e -> {
            replayPane.getChildren().remove(hitImageView);
            animationList.remove(fullAnimation);
        });

        // Add to animation list BEFORE starting
        animationList.add(fullAnimation);
        fullAnimation.play();
    }

    private void enterBreakPeriod() {
        FadeTransition fade = new FadeTransition(Duration.millis(1000), backgroundOverlay);
        fade.setFromValue(backgroundOverlay.getOpacity());
        fade.setToValue(0.5);
        fade.play();
    }

    private void exitBreakPeriod() {
        FadeTransition fade = new FadeTransition(Duration.millis(1000), backgroundOverlay);
        fade.setFromValue(backgroundOverlay.getOpacity());
        fade.setToValue(INITIAL_OPACITY);
        fade.play();
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

        // Store replay dimensions
        if (originalRecordingWidth <= 0 && originalRecordingHeight <= 0) {
            originalRecordingWidth = replayEvents.get(0).getScreenWidth();
            originalRecordingHeight = replayEvents.get(0).getScreenHeight();
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

        // Store current scaling values for coordinate conversion
        this.currentMasterScaleFactor = masterScaleFactor;
        this.currentViewportTopLeftX = viewportTopLeftX;
        this.currentViewportTopLeftY = viewportTopLeftY;

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
        backgroundOverlay = new Pane();
        backgroundOverlay.setStyle("-fx-background-color: rgba(18, 18, 18, 0.5);");
        backgroundOverlay.prefWidthProperty().bind(root.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(root.heightProperty());
        INITIAL_OPACITY = backgroundOverlay.getOpacity();

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

    private void pauseAllAnimations() {
        ArrayList<Animation> animationsCopy = new ArrayList<>(animationList);
        for (Animation animation : animationsCopy) {
            if (animation.getStatus() == Animation.Status.RUNNING) {
                animation.pause();
            }
        }
    }

    private void resumeAllAnimations() {
        ArrayList<Animation> animationsCopy = new ArrayList<>(animationList);
        for (Animation animation : animationsCopy) {
            if (animation.getStatus() == Animation.Status.PAUSED) {
                animation.play();
            }
        }

        // Clean up completed animations
        animationList.removeIf(animation -> animation.getStatus() == Animation.Status.STOPPED);
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
            case REPLAY_STARTED:
                System.out.println("Replay started");
                break;
            case REPLAY_PAUSED:
                System.out.println("Replay paused");
                pauseAllAnimations();
                pauseOverlay.setVisible(true);
                break;
            case REPLAY_RESUMED:
                System.out.println("Replay resumed");
                resumeAllAnimations();
                pauseOverlay.setVisible(false);
                break;
            case REPLAY_ENDED:
                ViewManager.getInstance().showHomeView();
                break;
            case CURSOR_MOVED:
                System.out.println("Cursor moved");
                CursorMoveEvent cursorData = event.getData(CursorMoveEvent.class);
                if (cursorData != null) {
                    double cursorX = cursorData.getX();
                    double cursorY = cursorData.getY();
                    cursorImage.setLayoutX(cursorX - cursorImage.getFitWidth() / 2);
                    cursorImage.setLayoutY(cursorY - cursorImage.getFitHeight() / 2);
                }
                break;
            case ENTER_BREAK_PERIOD:
                System.out.println("enter break period");
                enterBreakPeriod();
                break;
            case EXIT_BREAK_PERIOD:
                System.out.println("exit break period");
                exitBreakPeriod();
                break;
        }
    }

    @Override
    public double convertReplayMouseX(double replayX) {
        if (originalRecordingWidth <= 0) {
            return replayX;
        }

        double originalMasterScaleFactor;
        double originalAspectRatio = originalRecordingWidth / originalRecordingHeight;

        if (originalAspectRatio > OSU_ASPECT_RATIO) {
            originalMasterScaleFactor = originalRecordingHeight / OSU_HEIGHT;
        } else {
            originalMasterScaleFactor = originalRecordingWidth / OSU_WIDTH;
        }

        double originalScaledRefScreenWidth = OSU_WIDTH * originalMasterScaleFactor;
        double originalViewportTopLeftX = (originalRecordingWidth - originalScaledRefScreenWidth) / 2.0;

        double refX = (replayX - originalViewportTopLeftX) / originalMasterScaleFactor;

        double currentX = currentViewportTopLeftX + (refX * currentMasterScaleFactor);
        System.out.println("Converted replayY: " + replayX + " to currentY: " + currentX);

        return currentX;
    }

    @Override
    public double convertReplayMouseY(double replayY) {
        if (originalRecordingHeight <= 0) {
            return replayY;
        }

        double originalMasterScaleFactor;
        double originalAspectRatio = originalRecordingWidth / originalRecordingHeight;

        if (originalAspectRatio > OSU_ASPECT_RATIO) {
            originalMasterScaleFactor = originalRecordingHeight / OSU_HEIGHT;
        } else {
            originalMasterScaleFactor = originalRecordingWidth / OSU_WIDTH;
        }

        double originalScaledRefScreenHeight = OSU_HEIGHT * originalMasterScaleFactor;
        double originalViewportTopLeftY = (originalRecordingHeight - originalScaledRefScreenHeight) / 2.0;

        double refY = (replayY - originalViewportTopLeftY) / originalMasterScaleFactor;

        double currentY = currentViewportTopLeftY + (refY * currentMasterScaleFactor);
        System.out.println("Converted replayY: " + replayY + " to currentY: " + currentY);

        return currentY;
    }

    private void createMarqueeText() {
        marqueeContainer = new HBox();
        marqueeContainer.setAlignment(Pos.TOP_CENTER);
        marqueeContainer.setPrefHeight(stage.getHeight());
        marqueeContainer.setPrefWidth(stage.getWidth());
        marqueeContainer.setPadding(new Insets(ScreenManager.SCREEN_HEIGHT * 0.13, 0, 0, 0));

        userController.getUsernameById(playingUserId).thenAccept(result -> {
            Platform.runLater(() -> {
                String username = "Unknown Player";
                if (result.isSuccess()) {
                    username = result.getValue().getUsername();
                } else {
                    System.err.println("Failed to fetch username: " + result.getError().getMessage());
                }

                String marqueeText = String.format("REPLAY MODE - Watching %s play %s - %s [%s]",
                        username,
                        beatmap.getBeatmapSet().getArtist(),
                        beatmap.getBeatmapSet().getTitle(),
                        beatmap.getVersion());

                marqueeLabel = new Label(marqueeText);
                marqueeLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Aller Light'; -fx-font-size: " + ScreenManager.SCREEN_HEIGHT * 0.02 + "px;");

                endReplayButton = new EndReplayButton();

                endReplayButton.setOnMouseClicked(e -> {
                    SfxManager.playSfx("pause-click.wav");
                    ViewManager.getInstance().showHomeView();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox.setMargin(endReplayButton, new Insets(48, 12, 0, 12));

                marqueeContainer.getChildren().addAll(marqueeLabel, spacer, endReplayButton);
                StackPane.setAlignment(marqueeContainer, Pos.CENTER);
                setupMarqueeAnimation();
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                String marqueeText = String.format("REPLAY MODE - Watching replay of %s - %s [%s]",
                        beatmap.getBeatmapSet().getArtist(),
                        beatmap.getBeatmapSet().getTitle(),
                        beatmap.getVersion());

                marqueeLabel = new Label(marqueeText);
                marqueeLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Aller Light'; -fx-font-size: 20px;");

                endReplayButton = new EndReplayButton();
                endReplayButton.setBackground(new Background(new BackgroundFill(Color.color(1, 0.2, 0.2, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
                
                endReplayButton.setOnMouseClicked(e -> {
                    SfxManager.playSfx("pause-click.wav");
                    ViewManager.getInstance().showHomeView();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox.setMargin(endReplayButton, new Insets(48, 0, 0, 0));

                marqueeContainer.getChildren().addAll(marqueeLabel, spacer, endReplayButton);
                StackPane.setAlignment(marqueeContainer, Pos.CENTER);
                setupMarqueeAnimation();
            });
            return null;
        });
    }

    private void setupMarqueeAnimation() {
        Platform.runLater(() -> {
            marqueeContainer.applyCss();
            marqueeContainer.layout();
            marqueeLabel.applyCss();
            marqueeLabel.layout();

            double containerWidth = root.getWidth();
            double labelWidth = marqueeLabel.getBoundsInLocal().getWidth();

            marqueeAnimation = new TranslateTransition(Duration.seconds(15), marqueeLabel);
            marqueeAnimation.setFromX(containerWidth);
            marqueeAnimation.setToX(-labelWidth);
            marqueeAnimation.setCycleCount(TranslateTransition.INDEFINITE);
            marqueeAnimation.setInterpolator(Interpolator.LINEAR);

            marqueeAnimation.play();
        });
    }
}
