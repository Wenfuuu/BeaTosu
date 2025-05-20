package beat.osu.beatosu.view.landing;

import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.helper.ScreenManager;
import beat.osu.beatosu.view.Page;
import beat.osu.beatosu.view.landing.component.*;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;

public class LandingView extends Page {

    private StackPane root;
    private BorderPane mainLayout;

    private TopBar topBarComponent;
    private Visualizer visualizerComponent;
    private BottomBar bottomBarComponent;
    private MediaControls mediaControlsComponent;
    private MenuButtons menuButtonsComponent;
    private LoginModal loginModalComponent;
    private RegisterModal registerModalComponent;

    private MediaPlayer songMedia;

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
        // Assuming visualizerComponent.getLogoView() and menuButtonsComponent are ready
        // These values might need to be dynamically calculated based on component sizes if not fixed
        double logoTranslateX = -150; // How much the logo moves
        double menuTranslateX = 180;  // How much the menu moves in from the side of the logo

        // Logo slides out (left)
        logoSlideOut = new TranslateTransition(Duration.millis(300), visualizerComponent.getLogoView());
        logoSlideOut.setToX(logoTranslateX);

        menuSlideIn = new TranslateTransition(Duration.millis(300), menuButtonsComponent);
        menuButtonsComponent.setTranslateX(0); // Start behind the logo
        menuSlideIn.setToX(menuTranslateX);

        // Reverse animations
        logoSlideIn = new TranslateTransition(Duration.millis(300), visualizerComponent.getLogoView());
        logoSlideIn.setToX(0); // Back to center

        menuSlideOut = new TranslateTransition(Duration.millis(300), menuButtonsComponent);
        menuSlideOut.setToX(0); // Slide back behind to logo
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
        root.getStyleClass().add("root");

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-layout");

        // --- Initialize Components ---
        mediaControlsComponent = new MediaControls();
        topBarComponent = new TopBar();
        menuButtonsComponent = new MenuButtons();
        visualizerComponent = new Visualizer();
        bottomBarComponent = new BottomBar();
        loginModalComponent = new LoginModal();
        registerModalComponent = new RegisterModal();

        // --- Configure Components ---
        topBarComponent.addControlsToBar(mediaControlsComponent); // Add media controls to top bar
        // Set initial song title (or update it when song changes)
        topBarComponent.setSongTitle("Minato Aqua - #Aquairo Palette");

        visualizerComponent.setMenuBox(menuButtonsComponent); // Add menu buttons to visualizer

        // --- Initialize Media Player ---
        File songFile = new File("./src/main/resources/assets/audio/audio.mp3"); // Path to your audio file
        if (songFile.exists()) {
            Media song = new Media(songFile.toURI().toString());
            songMedia = new MediaPlayer(song);
            songMedia.setAutoPlay(true);
            songMedia.setVolume(0.2);

            // Configure visualizer with media player
            visualizerComponent.setupAudioVisualization(songMedia);
        } else {
            System.err.println("Audio file not found: " + songFile.getAbsolutePath());
            // Optionally create a dummy MediaPlayer or handle the absence of audio
        }

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
        URL cssUrl = CssManager.getLandingCssURL("LandingView.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        // Initialize menu reveal animations
        initMenuRevealAnimations();
    }

    @Override
    public void setLayout() {
        // --- Main Layout Structure ---
        mainLayout.setTop(topBarComponent);
        mainLayout.setCenter(visualizerComponent);
        mainLayout.setBottom(bottomBarComponent);

        // --- Root Structure (StackPane for layers) ---
        root.getChildren().add(mainLayout);
        root.getChildren().addAll(loginModalComponent, registerModalComponent);
        StackPane.setAlignment(loginModalComponent, Pos.CENTER_LEFT);
    }

    public void handleEvent() {
        // --- Media Controls Events ---
        if (songMedia != null) {
            mediaControlsComponent.getPlayButton().setOnAction(e -> songMedia.play());
            mediaControlsComponent.getPauseButton().setOnAction(e -> songMedia.pause());
            mediaControlsComponent.getStopButton().setOnAction(e -> songMedia.stop());
            // TODO: Add event handlers for prev, next, options, playlist buttons
        } else {
            // Disable media buttons if songMedia is null
            mediaControlsComponent.getPlayButton().setDisable(true);
            mediaControlsComponent.getPauseButton().setDisable(true);
            mediaControlsComponent.getStopButton().setDisable(true);
        }

        // --- Visualizer Logo Click (for menu reveal) ---
        visualizerComponent.getLogoView().setOnMouseClicked(e -> toggleMenuPanel());

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
                topBarComponent.updateUserInfo(user); // Update TopBar with logged-in user
                // LoginModal hides itself on success
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
