package beat.osu.beatosu.view.landing;

import beat.osu.beatosu.helper.BackgroundManager;
import beat.osu.beatosu.helper.BgmManager;
import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.helper.ScreenManager;
import beat.osu.beatosu.view.Page;
import beat.osu.beatosu.view.home.HomeView;
import beat.osu.beatosu.view.landing.component.*;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

public class LandingView extends Page {

    private StackPane root;
    private TopBar topBarComponent;
    private Visualizer visualizerComponent;
    private BottomBar bottomBarComponent;
    private MediaControls mediaControlsComponent;
    private MenuButtons menuButtonsComponent;
    private LoginModal loginModalComponent;
    private RegisterModal registerModalComponent;

    private boolean isMenuPanelOpen = false;
    private TranslateTransition logoSlideOut;
    private TranslateTransition menuSlideIn;
    private TranslateTransition logoSlideIn;
    private TranslateTransition menuSlideOut;

    public LandingView(Stage stage) {
        super(stage);
        handleEvent();
    }

    private void initMenuRevealAnimations() {
        double logoTranslateX = -200; // How much the logo+rays group moves
        double menuTranslateX = 440;  // How much the menu moves in

        // Configure nodes for animation - set cache for better performance
        visualizerComponent.getLogoRayGroup().setCache(true);
        visualizerComponent.getLogoRayGroup().setCacheHint(CacheHint.SPEED);
        menuButtonsComponent.setCache(true);
        menuButtonsComponent.setCacheHint(CacheHint.SPEED);

        logoSlideOut = new TranslateTransition(Duration.millis(300),
                visualizerComponent.getLogoRayGroup());
        logoSlideOut.setToX(logoTranslateX);
        logoSlideOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        logoSlideOut.setOnFinished(e -> {
            visualizerComponent.getLogoRayGroup().setCacheHint(CacheHint.DEFAULT);
        });

        menuSlideIn = new TranslateTransition(Duration.millis(300), menuButtonsComponent);
        menuButtonsComponent.setTranslateX(0); // Initial position
        menuSlideIn.setToX(menuTranslateX);
        menuSlideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        menuSlideIn.setOnFinished(e -> {
            menuButtonsComponent.setCacheHint(CacheHint.DEFAULT);
        });

        // Logo slide in animation (when closing menu)
        logoSlideIn = new TranslateTransition(Duration.millis(300),
                visualizerComponent.getLogoRayGroup());
        logoSlideIn.setFromX(logoTranslateX); // Make sure it starts from the right position
        logoSlideIn.setToX(0);
        logoSlideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        logoSlideIn.setOnFinished(e -> {
            // After animation, revert to default cache hint
            visualizerComponent.getLogoRayGroup().setCacheHint(CacheHint.DEFAULT);
        });

        // Menu slide out animation (when closing menu)
        menuSlideOut = new TranslateTransition(Duration.millis(300), menuButtonsComponent);
        menuSlideOut.setFromX(menuTranslateX); // Make sure it starts from the right position
        menuSlideOut.setToX(0);
        menuSlideOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        menuSlideOut.setOnFinished(e -> {
            // After animation, revert to default cache hint
            menuButtonsComponent.setCacheHint(CacheHint.DEFAULT);
        });
    }

    private void toggleMenuPanel() {
        if (isMenuPanelOpen) {
            // Close menu
            logoSlideIn.play();
            menuSlideOut.play();
        } else {
            // Open menu
            logoSlideOut.play();
            menuSlideIn.play();
        }
        isMenuPanelOpen = !isMenuPanelOpen;
    }

    @Override
    public void init() {
        root = new StackPane();
        root.getStyleClass().add("main-layout");

        // --- Initialize Components ---
        mediaControlsComponent = new MediaControls();
        topBarComponent = new TopBar();
        menuButtonsComponent = new MenuButtons();
        visualizerComponent = new Visualizer();
        bottomBarComponent = new BottomBar();
        loginModalComponent = new LoginModal();
        registerModalComponent = new RegisterModal();

        visualizerComponent.getLogoRayGroup().getStyleClass().add("logo-ray-group");
        menuButtonsComponent.getStyleClass().add("menu-buttons");

        // --- Configure Components ---
        topBarComponent.addControlsToBar(mediaControlsComponent); // Add media controls to top bar
        // Set initial song title (or update it when song changes)
        topBarComponent.setSongTitle("Minato Aqua - #Aquairo Palette");

        visualizerComponent.setMenuBox(menuButtonsComponent); // Add menu buttons to visualizer

        String bgmPath = "./src/main/resources/assets/audio/audio.mp3";
        BgmManager.playBgm(bgmPath);
        if(BgmManager.getCurrentPlayer() != null) {
            visualizerComponent.setupAudioVisualization(BgmManager.getCurrentPlayer());
        } else {
            System.err.println("Failed to load BGM: " + bgmPath);
        }

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
        URL cssUrl = CssManager.getLandingCssURL("LandingView.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        try {
            BackgroundManager.setRandomBackground(scene);
        } catch (Exception e) {
            System.err.println("Error setting background: " + e.getMessage());
            root.setStyle("-fx-background-color: #121212;");
        }

        initMenuRevealAnimations();
    }

    @Override
    public void setLayout() {
        root.getChildren().add(visualizerComponent);
        topBarComponent.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(topBarComponent);
        StackPane.setAlignment(topBarComponent, Pos.TOP_CENTER);

        bottomBarComponent.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(bottomBarComponent);
        StackPane.setAlignment(bottomBarComponent, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(loginModalComponent, registerModalComponent);
        StackPane.setAlignment(loginModalComponent, Pos.CENTER_LEFT);
        StackPane.setAlignment(registerModalComponent, Pos.CENTER);
    }

    public void handleEvent() {
        // --- Media Controls Events ---
        if (BgmManager.getCurrentPlayer() != null) {
            mediaControlsComponent.getPlayButton().setOnAction(e -> BgmManager.resumeBgm());
            mediaControlsComponent.getPauseButton().setOnAction(e -> BgmManager.pauseBgm());
            mediaControlsComponent.getStopButton().setOnAction(e -> BgmManager.stopBgm());
        } else {
            // Disable media buttons if songMedia is null
            mediaControlsComponent.getPlayButton().setDisable(true);
            mediaControlsComponent.getPauseButton().setDisable(true);
            mediaControlsComponent.getStopButton().setDisable(true);
        }

        // --- Visualizer Logo Click (for menu reveal) ---
        visualizerComponent.getLogoView().setOnMouseClicked(e -> {
            System.out.println("Logo Ray Group clicked! Event: " + e);
            toggleMenuPanel();
        });

        // --- TopBar User Info Click (Show Login Modal) ---
        topBarComponent.getUserInfoBox().setOnMouseClicked(e -> {
            if (!loginModalComponent.isShowing() && !registerModalComponent.isVisible()) {
                loginModalComponent.clearFields(); // Clear previous input
                loginModalComponent.show();
            }
        });

        // --- Login Modal Events ---
        loginModalComponent.setOnLoginSuccessListener(user -> {
            if (user != null) {
                topBarComponent.updateUserInfo(user);
            }
        });

        loginModalComponent.setOnCreateAccountListener(() -> {
            loginModalComponent.hide(); // Hide login modal
            registerModalComponent.setVisible(true); // Show register modal
            registerModalComponent.toFront();
        });

        // --- Register Modal Events ---
        // The RegisterModal's own "cancel" button already handles hiding itself.
        // Handle "Create my account!" click from RegisterModal
        // (This assumes RegisterModal exposes its createButton or a specific event for it)
        // For now, let's assume RegisterModal handles its own creation logic, or you add a listener.
        // Example: registerModalComponent.getCreateButton().setOnAction(e -> { /* handle registration */ });

        // --- Click outside LoginModal to hide it ---
        root.setOnMouseClicked(e -> {
            if (loginModalComponent.isShowing()) {
                // Check if the click target is outside the loginModalComponent.
                // This requires checking if e.getTarget() is a descendant of loginModalComponent.
                javafx.scene.Node target = (javafx.scene.Node) e.getTarget();
                boolean clickedOnLoginModal = false;
                while (target != null) {
                    if (target == loginModalComponent) {
                        clickedOnLoginModal = true;
                        break;
                    }
                    target = target.getParent();
                }
                if (!clickedOnLoginModal) {
                    loginModalComponent.hide();
                }
            }
        });

        // --- Menu Buttons Events (from MenuButtons component) ---
        menuButtonsComponent.getPlayButton().setOnAction(e -> {
            System.out.println("Menu: Play clicked");
            // Add navigation or action for Play
            new HomeView(stage); // Example: Navigate to HomeView
            toggleMenuPanel(); // Hide menu after action
        });
        menuButtonsComponent.getOptionButton().setOnAction(e -> {
            System.out.println("Menu: Options clicked");
            // Add navigation or action for Options
            toggleMenuPanel(); // Hide menu after action
        });
        menuButtonsComponent.getExitButton().setOnAction(e -> {
            System.out.println("Menu: Exit clicked");
            // Platform.exit(); or stage.close();
            ((Stage) scene.getWindow()).close();
        });
    }
}