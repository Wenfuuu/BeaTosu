package beat.osu.client.view.landing;

import beat.osu.client.Main;
import beat.osu.client.helper.*;
import beat.osu.client.view.Page;
import beat.osu.client.view.Toast;
import beat.osu.client.view.home.HomeView;
import beat.osu.client.view.landing.component.bancho.buttons.BanchoButtons;
import beat.osu.client.view.landing.component.bancho.panels.ChatPanel;
import beat.osu.client.view.landing.component.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.landing.component.controls.MediaControls;
import beat.osu.client.view.landing.component.controls.MenuButtons;
import beat.osu.client.view.landing.component.controls.SubMenuButtons;
import beat.osu.client.view.landing.component.layout.BottomBar;
import beat.osu.client.view.landing.component.layout.TopBar;
import beat.osu.client.view.landing.component.modals.LoginModal;
import beat.osu.client.view.landing.component.modals.RegisterModal;
import beat.osu.client.view.landing.component.ui.Visualizer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.URL;

public class LandingView extends Page {

    private StackPane root;
    private TopBar topBarComponent;
    private Visualizer visualizerComponent;
    private BottomBar bottomBarComponent;
    private MediaControls mediaControlsComponent;
    private MenuButtons menuButtonsComponent;
    private SubMenuButtons subMenuButtonsComponent;
    private LoginModal loginModalComponent;
    private RegisterModal registerModalComponent;

    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private BanchoButtons banchoButtons;

    private double visualizerSize;

    private boolean isMenuPanelOpen = false;
    private boolean isSubMenuOpen = false;
    private TranslateTransition logoSlideOut;
    private TranslateTransition menuSlideIn;
    private TranslateTransition logoSlideIn;
    private TranslateTransition menuSlideOut;

    private FadeTransition menuFadeOut;
    private FadeTransition menuFadeIn;
    private FadeTransition subMenuFadeIn;
    private FadeTransition subMenuFadeOut;

    public LandingView(Stage stage) {
        super(stage);
        handleEvent();

        Toast.information("Hi, Welcome to BeaTosu!").show();
    }

    private void initMenuRevealAnimations() {
        double logoTranslateX = -this.visualizerSize / 3.5;
        double menuTranslateX = this.visualizerSize / 1.4;

        visualizerComponent.getLogoRayGroup().setCache(true);
        visualizerComponent.getLogoRayGroup().setCacheHint(CacheHint.SPEED);
        menuButtonsComponent.setCache(true);
        menuButtonsComponent.setCacheHint(CacheHint.SPEED);
        subMenuButtonsComponent.setCache(true);
        subMenuButtonsComponent.setCacheHint(CacheHint.SPEED);

        logoSlideOut = new TranslateTransition(Duration.millis(300),
                visualizerComponent.getLogoRayGroup());
        logoSlideOut.setToX(logoTranslateX);
        logoSlideOut.setInterpolator(Interpolator.EASE_OUT);
        logoSlideOut.setOnFinished(e -> {
            visualizerComponent.getLogoRayGroup().setCacheHint(CacheHint.DEFAULT);
        });

        menuSlideIn = new TranslateTransition(Duration.millis(300), menuButtonsComponent);
        menuButtonsComponent.setTranslateX(0);
        menuSlideIn.setToX(menuTranslateX);
        menuSlideIn.setInterpolator(Interpolator.EASE_OUT);
        menuSlideIn.setOnFinished(e -> {
            menuButtonsComponent.setCacheHint(CacheHint.DEFAULT);
        });

        logoSlideIn = new TranslateTransition(Duration.millis(300),
                visualizerComponent.getLogoRayGroup());
        logoSlideIn.setFromX(logoTranslateX);
        logoSlideIn.setToX(0);
        logoSlideIn.setInterpolator(Interpolator.EASE_OUT);
        logoSlideIn.setOnFinished(e -> {
            visualizerComponent.getLogoRayGroup().setCacheHint(CacheHint.DEFAULT);
        });

        menuSlideOut = new TranslateTransition(Duration.millis(300), menuButtonsComponent);
        menuSlideOut.setFromX(menuTranslateX);
        menuSlideOut.setToX(0);
        menuSlideOut.setInterpolator(Interpolator.EASE_OUT);
        menuSlideOut.setOnFinished(e -> {
            menuButtonsComponent.setCacheHint(CacheHint.DEFAULT);
        });

        subMenuButtonsComponent.setTranslateX(menuTranslateX);

        menuFadeOut = new FadeTransition(Duration.millis(300), menuButtonsComponent);
        menuFadeOut.setFromValue(1.0);
        menuFadeOut.setToValue(0.0);
        
        menuFadeIn = new FadeTransition(Duration.millis(300), menuButtonsComponent);
        menuFadeIn.setFromValue(0.0);
        menuFadeIn.setToValue(1.0);
        
        subMenuFadeIn = new FadeTransition(Duration.millis(300), subMenuButtonsComponent);
        subMenuFadeIn.setFromValue(0.0);
        subMenuFadeIn.setToValue(1.0);
        
        subMenuFadeOut = new FadeTransition(Duration.millis(300), subMenuButtonsComponent);
        subMenuFadeOut.setFromValue(1.0);
        subMenuFadeOut.setToValue(0.0);
    }

    private void toggleMenuPanel() {
        if (isSubMenuOpen) {
            hideSubMenu();
        } else if (isMenuPanelOpen) {
            BackgroundManager.setDarkBackground(scene, false);

            logoSlideIn.play();
            menuSlideOut.play();
            isMenuPanelOpen = false;

            updateBanchoButtonsVisibility();
        } else {
            BackgroundManager.setDarkBackground(scene, true);

            logoSlideOut.play();
            menuSlideIn.play();
            isMenuPanelOpen = true;

            updateBanchoButtonsVisibility();
        }
    }

    private void showSubMenu() {
        System.out.println(">>> showSubMenu() called - isMenuPanelOpen: " + isMenuPanelOpen);
        if (isMenuPanelOpen) {
            subMenuButtonsComponent.setVisible(true);
            subMenuButtonsComponent.setManaged(true);
            menuButtonsComponent.setOpacity(1.0);
            subMenuButtonsComponent.setCacheHint(CacheHint.DEFAULT);

            TranslateTransition menuTranslateOut = new TranslateTransition(Duration.millis(0), menuButtonsComponent);
            menuTranslateOut.setFromX(this.visualizerSize / 1.4);
            menuTranslateOut.setToX(0);

            ParallelTransition switchMenu = new ParallelTransition(menuFadeOut, subMenuFadeIn, menuTranslateOut);
            switchMenu.play();

            isMenuPanelOpen = false;
            isSubMenuOpen = true;

            updateBanchoButtonsVisibility();
        }
    }

    private void hideSubMenu() {
        if (isSubMenuOpen) {
            subMenuButtonsComponent.setVisible(false);
            subMenuButtonsComponent.setManaged(false);
            menuButtonsComponent.setOpacity(0.0);
            subMenuButtonsComponent.setCacheHint(CacheHint.DEFAULT);

            menuButtonsComponent.setTranslateX(this.visualizerSize / 1.4);
            ParallelTransition switchMenu = new ParallelTransition(menuFadeIn, subMenuFadeOut);
            switchMenu.play();

            isSubMenuOpen = false;
            isMenuPanelOpen = true;

            updateBanchoButtonsVisibility();
        }
    }

    private void updateBanchoButtonsVisibility() {
        boolean shouldShow = AuthManager.isAuthenticated() && (isMenuPanelOpen || isSubMenuOpen);
        banchoButtons.setVisible(shouldShow);
        banchoButtons.setManaged(shouldShow);
    }

    @Override
    public void init() {
        root = new StackPane();
        root.getStyleClass().add("main-layout");

        this.visualizerSize = ScreenManager.SCREEN_HEIGHT * 0.6;

        mediaControlsComponent = new MediaControls();
        topBarComponent = new TopBar();
        menuButtonsComponent = new MenuButtons();
        subMenuButtonsComponent = new SubMenuButtons();
        visualizerComponent = new Visualizer(this.visualizerSize);
        bottomBarComponent = new BottomBar();
        loginModalComponent = new LoginModal();
        registerModalComponent = new RegisterModal();

        onlineUsersPanel = new OnlineUsersPanel();
        chatPanel = new ChatPanel();
        banchoButtons = new BanchoButtons();

        banchoButtons.setVisible(false);
        banchoButtons.setManaged(false);

        visualizerComponent.getLogoRayGroup().getStyleClass().add("logo-ray-group");
        menuButtonsComponent.getStyleClass().add("menu-buttons");
        subMenuButtonsComponent.getStyleClass().add("menu-buttons");

        topBarComponent.addControlsToBar(mediaControlsComponent);
        topBarComponent.setSongTitle("nekodex - circles!");

        visualizerComponent.setMenuBox(menuButtonsComponent);
        visualizerComponent.setSubMenuBox(subMenuButtonsComponent);

        subMenuButtonsComponent.setVisible(false);
        subMenuButtonsComponent.setManaged(false);
        subMenuButtonsComponent.setOpacity(0.0);

        String bgmPath = "/assets/audio/nekodex-circles.mp3";
        URL bgmUrl = Main.class.getResource(bgmPath);
        if (bgmUrl != null && BgmManager.getCurrentPlayer() == null) {
            BgmManager.playBgm(bgmUrl);
        }

        if(BgmManager.getCurrentPlayer() != null) {
            visualizerComponent.setupAudioVisualization(BgmManager.getCurrentPlayer());
        } else {
            System.err.println("Failed to load BGM: " + bgmPath);
        }

        scene.setRoot(root);
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

        root.getChildren().add(onlineUsersPanel);
        StackPane.setAlignment(onlineUsersPanel, Pos.TOP_CENTER);

        root.getChildren().add(chatPanel);
        StackPane.setAlignment(chatPanel, Pos.BOTTOM_CENTER);

        root.getChildren().add(banchoButtons);
        StackPane.setAlignment(banchoButtons, Pos.BOTTOM_RIGHT);
    }

    public void handleEvent() {
        if (BgmManager.getCurrentPlayer() != null) {
            mediaControlsComponent.getPlayButton().setOnAction(e -> BgmManager.resumeBgm());
            mediaControlsComponent.getPauseButton().setOnAction(e -> BgmManager.pauseBgm());
            mediaControlsComponent.getStopButton().setOnAction(e -> BgmManager.stopBgm());
        } else {
            mediaControlsComponent.getPlayButton().setDisable(true);
            mediaControlsComponent.getPauseButton().setDisable(true);
            mediaControlsComponent.getStopButton().setDisable(true);
        }

        visualizerComponent.getLogoRayGroup().setOnMouseClicked(e -> {
            toggleMenuPanel();
        });

        topBarComponent.getUserInfoBox().setOnMouseClicked(e -> {
            if (!loginModalComponent.isShowing() && !registerModalComponent.isVisible()) {
                loginModalComponent.clearFields();
                loginModalComponent.show();
            }
        });

        loginModalComponent.setOnLoginSuccessListener(user -> {
            if (user != null) {
                topBarComponent.updateUserInfo(user);
                updateBanchoButtonsVisibility();
            }
        });

        loginModalComponent.setOnCreateAccountListener(() -> {
            loginModalComponent.hide(); 
            registerModalComponent.setVisible(true); 
            registerModalComponent.toFront();
        });

        root.setOnMouseClicked(e -> {
            if (loginModalComponent.isShowing()) {
                Node target = (Node) e.getTarget();
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

        menuButtonsComponent.getPlayButton().setOnMouseClicked(e -> {
            showSubMenu();
        });
        menuButtonsComponent.getOptionButton().setOnMouseClicked(e -> {
            toggleMenuPanel();
        });
        menuButtonsComponent.getExitButton().setOnMouseClicked(e -> {
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        subMenuButtonsComponent.getSoloButton().setOnMouseClicked(e -> {
//            new HomeView(stage);
            ViewManager.showHomeView();
            hideSubMenu();
        });
        
        subMenuButtonsComponent.getMultiButton().setOnMouseClicked(e -> {
            new HomeView(stage);
            hideSubMenu();
        });
        
        subMenuButtonsComponent.getBackButton().setOnMouseClicked(e -> {
            hideSubMenu();
        });

        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            banchoButtons.toggleOnlineUsers(onlineUsersPanel, chatPanel, topBarComponent, bottomBarComponent);
        });

        banchoButtons.getChatToggleButton().setOnMouseClicked(e -> {
            banchoButtons.toggleChat(chatPanel, bottomBarComponent);
        });

        banchoButtons.getAutoHideButton().setOnMouseClicked(e -> {
            banchoButtons.toggleAutoHide();
        });

        banchoButtons.getShowTickerButton().setOnMouseClicked(e -> {
            banchoButtons.toggleTicker();
        });
    }
}