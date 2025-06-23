package beat.osu.client.view.landing;

import java.io.File;
import java.net.URL;

import beat.osu.client.Main;
import beat.osu.client.controller.ChatController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.controller.SessionController;
import beat.osu.client.controller.SpectateController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.view.landing.component.controls.MenuButtons;
import beat.osu.client.view.landing.component.controls.SubMenuButtons;
import beat.osu.client.view.landing.component.layout.BottomBar;
import beat.osu.client.view.landing.component.layout.TopBar;
import beat.osu.client.view.landing.component.modals.LoginModal;
import beat.osu.client.view.landing.component.modals.RegisterModal;
import beat.osu.client.view.landing.component.modals.SettingsModal;
import beat.osu.client.view.landing.component.ui.Visualizer;
import beat.osu.client.view.shared.bancho.buttons.BanchoButtons;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.client.view.shared.bancho.modals.SelectChannelModal;
import beat.osu.client.view.shared.bancho.modals.ViewUserModal;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.client.view.shared.jukebox.Jukebox;
import beat.osu.client.view.shared.jukebox.modals.PlaylistModal;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class LandingView extends Page {

    private StackPane root;

    private TopBar topBar;
    private BottomBar bottomBar;

    private Visualizer visualizer;

    private MenuButtons menuButtons;
    private SubMenuButtons subMenuButtons;

    private LoginModal loginModal;
    private RegisterModal registerModal;
    private PlaylistModal playlistModal;
    private SettingsModal settingsModal;

    private Jukebox jukebox;

    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private SelectChannelModal selectChannelModal;
    private BanchoButtons banchoButtons;
    private ViewUserModal viewUserModal;

    private VBox banchoPanelsContainer;

    private final ConnectedUsersController connectedUsersController;
    private final ChatController chatController;
    private final SessionController sessionController;
    private final SpectateController spectateController;

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

    public LandingView(Stage stage, ConnectedUsersController connectedUsersController,
                       ChatController chatController, SessionController sessionController,
                       SpectateController spectateController) {
        super(stage);

        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;
        this.sessionController = sessionController;
        this.spectateController = spectateController;

        setupView();
        handleEvent();

        Toast.information("Hi, Welcome to BeaTosu!").show();
    }

    private void initMenuRevealAnimations() {
        double logoTranslateX = -this.visualizerSize / 3.5;
        double menuTranslateX = this.visualizerSize / 1.4;

        visualizer.getLogoRayGroup().setCache(true);
        visualizer.getLogoRayGroup().setCacheHint(CacheHint.SPEED);
        menuButtons.setCache(true);
        menuButtons.setCacheHint(CacheHint.SPEED);
        subMenuButtons.setCache(true);
        subMenuButtons.setCacheHint(CacheHint.SPEED);

        logoSlideOut = new TranslateTransition(Duration.millis(300),
                visualizer.getLogoRayGroup());
        logoSlideOut.setToX(logoTranslateX);
        logoSlideOut.setInterpolator(Interpolator.EASE_OUT);
        logoSlideOut.setOnFinished(e -> {
            visualizer.getLogoRayGroup().setCacheHint(CacheHint.DEFAULT);
        });

        menuSlideIn = new TranslateTransition(Duration.millis(300), menuButtons);
        menuButtons.setTranslateX(0);
        menuSlideIn.setToX(menuTranslateX);
        menuSlideIn.setInterpolator(Interpolator.EASE_OUT);
        menuSlideIn.setOnFinished(e -> {
            menuButtons.setCacheHint(CacheHint.DEFAULT);
        });

        logoSlideIn = new TranslateTransition(Duration.millis(300),
                visualizer.getLogoRayGroup());
        logoSlideIn.setFromX(logoTranslateX);
        logoSlideIn.setToX(0);
        logoSlideIn.setInterpolator(Interpolator.EASE_OUT);
        logoSlideIn.setOnFinished(e -> {
            visualizer.getLogoRayGroup().setCacheHint(CacheHint.DEFAULT);
        });

        menuSlideOut = new TranslateTransition(Duration.millis(300), menuButtons);
        menuSlideOut.setFromX(menuTranslateX);
        menuSlideOut.setToX(0);
        menuSlideOut.setInterpolator(Interpolator.EASE_OUT);
        menuSlideOut.setOnFinished(e -> {
            menuButtons.setCacheHint(CacheHint.DEFAULT);
        });

        subMenuButtons.setTranslateX(menuTranslateX);

        menuFadeOut = new FadeTransition(Duration.millis(300), menuButtons);
        menuFadeOut.setFromValue(1.0);
        menuFadeOut.setToValue(0.0);

        menuFadeIn = new FadeTransition(Duration.millis(300), menuButtons);
        menuFadeIn.setFromValue(0.0);
        menuFadeIn.setToValue(1.0);

        subMenuFadeIn = new FadeTransition(Duration.millis(300), subMenuButtons);
        subMenuFadeIn.setFromValue(0.0);
        subMenuFadeIn.setToValue(1.0);

        subMenuFadeOut = new FadeTransition(Duration.millis(300), subMenuButtons);
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
        if (isMenuPanelOpen) {
            subMenuButtons.setVisible(true);
            subMenuButtons.setManaged(true);
            menuButtons.setOpacity(1.0);
            subMenuButtons.setCacheHint(CacheHint.DEFAULT);

            TranslateTransition menuTranslateOut = new TranslateTransition(Duration.millis(0), menuButtons);
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
            subMenuButtons.setVisible(false);
            subMenuButtons.setManaged(false);
            menuButtons.setOpacity(0.0);
            subMenuButtons.setCacheHint(CacheHint.DEFAULT);

            menuButtons.setTranslateX(this.visualizerSize / 1.4);
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

    private void hideAllModals() {
        if (loginModal.isShowing()) {
            loginModal.hide();
        }
        if (registerModal.isVisible()) {
            registerModal.setVisible(false);
        }
        if (settingsModal.isShowing()) {
            settingsModal.hide();
        }
        if (playlistModal.isVisible()) {
            playlistModal.setVisible(false);
        }
    }

    @Override
    public void init() {
        root = new StackPane();
        root.getStyleClass().add("main-layout");

        this.visualizerSize = ScreenManager.SCREEN_HEIGHT * 0.65;

        menuButtons = new MenuButtons();
        subMenuButtons = new SubMenuButtons();
        topBar = new TopBar();
        bottomBar = new BottomBar();
        loginModal = new LoginModal(topBar);
        registerModal = new RegisterModal();
        settingsModal = new SettingsModal();

        visualizer = new Visualizer(this.visualizerSize);
        PlaylistManager.getInstance().addListener(visualizer);

        playlistModal = new PlaylistModal();
        PlaylistManager.getInstance().addListener(playlistModal);
        
        jukebox = new Jukebox(playlistModal);
        PlaylistManager.getInstance().addListener(jukebox);

        banchoButtons = new BanchoButtons();
        banchoButtons.setVisible(false);
        banchoButtons.setManaged(false);

        onlineUsersPanel = new OnlineUsersPanel(connectedUsersController);
        selectChannelModal = new SelectChannelModal(chatController.getChannelController(), banchoButtons);
        chatPanel = new ChatPanel(chatController, selectChannelModal, onlineUsersPanel, banchoButtons);

        viewUserModal = new ViewUserModal(sessionController);

        banchoPanelsContainer = new VBox();
        banchoPanelsContainer.setVisible(false);
        banchoPanelsContainer.setManaged(false);
        banchoPanelsContainer.setMaxWidth(Double.MAX_VALUE);
        banchoPanelsContainer.setMaxHeight(Double.MAX_VALUE);
        banchoPanelsContainer.setMouseTransparent(false);
        
        onlineUsersPanel.setMaxWidth(Double.MAX_VALUE);
        chatPanel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(onlineUsersPanel, Priority.ALWAYS);
        chatPanel.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        chatPanel.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        chatPanel.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        
        banchoPanelsContainer.getChildren().addAll(onlineUsersPanel, chatPanel);

        viewUserModal.setOnStartChatCallback(privateChat -> {
            if (AuthManager.getUser().getId() == privateChat.getOtherUserId()) {
                Toast.error("You cannot start a chat with yourself!").show();
                return;
            }

            chatPanel.startPrivateChat(privateChat.getOtherUserId(), privateChat.getOtherUserName());
        });

        viewUserModal.setOnStartSpectateCallback(spectateDto -> {
            if (AuthManager.getUser().getId() == spectateDto.getPlayingUserId()) {
                Toast.error("You cannot spectate yourself!").show();
                return;
            }

            System.out.println("Player with id " + spectateDto.getPlayingUserId()
                    + " is playing beatmap with id " + spectateDto.getBeatmapId());
//            ViewManager.getInstance().showSpectateView(spectateDto, spectateController);
        });

        onlineUsersPanel.setUserCardClickCallback(userCard -> {
            UserCard modalUserCard = new UserCard(
                    userCard.getUserId(),
                    userCard.getUsername(),
                    userCard.getCountryCode(),
                    userCard.getProfilePicture(),
                    userCard.getPerformance(),
                    userCard.getAccuracy(),
                    userCard.getPlayCount(),
                    userCard.getLevel(),
                    userCard.getRank(),
                    userCard.getIsSupporter(),
                    UserCardBehavior.STATIC
            );
            viewUserModal.updateUserCard(modalUserCard);
            viewUserModal.show();
        });

        selectChannelModal.setChatPanel(chatPanel);
        selectChannelModal.setOnlineUsersPanel(onlineUsersPanel);

        visualizer.getLogoRayGroup().getStyleClass().add("logo-ray-group");
        menuButtons.getStyleClass().add("menu-buttons");
        subMenuButtons.getStyleClass().add("menu-buttons");

        visualizer.setMenuBox(menuButtons);
        visualizer.setSubMenuBox(subMenuButtons);

        subMenuButtons.setVisible(false);
        subMenuButtons.setManaged(false);
        subMenuButtons.setOpacity(0.0);

        playlistModal.setVisible(false);

        String bgmPath = "/assets/audio/nekodex-circles.mp3";
        URL bgmUrl = Main.class.getResource(bgmPath);
        if (bgmUrl != null && BgmManager.getInstance().getCurrentPlayer() == null) {
            try {
                File bgmFile = new File(bgmUrl.toURI());
                BgmManager.getInstance().playDefaultBgm(bgmFile);
            } catch (Exception e) {
                System.err.println("Failed to convert BGM URL to File: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if(BgmManager.getInstance().getCurrentPlayer() != null) {
            visualizer.setupAudioVisualization(BgmManager.getInstance().getCurrentPlayer());
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
        root.getChildren().add(visualizer);
        topBar.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(topBar);
        StackPane.setAlignment(topBar, Pos.TOP_CENTER);

        bottomBar.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(bottomBar);
        StackPane.setAlignment(bottomBar, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(loginModal, registerModal, settingsModal, playlistModal, selectChannelModal);
        StackPane.setAlignment(loginModal, Pos.CENTER_LEFT);
        StackPane.setAlignment(settingsModal, Pos.CENTER_LEFT);
        StackPane.setAlignment(registerModal, Pos.CENTER);
        StackPane.setAlignment(playlistModal, Pos.CENTER);
        StackPane.setAlignment(selectChannelModal, Pos.CENTER);

        root.getChildren().add(jukebox);
        StackPane.setAlignment(jukebox, Pos.TOP_RIGHT);

        root.getChildren().add(banchoPanelsContainer);
        StackPane.setAlignment(banchoPanelsContainer, Pos.TOP_CENTER);

        root.getChildren().add(banchoButtons);
        StackPane.setAlignment(banchoButtons, Pos.BOTTOM_RIGHT);

        root.getChildren().add(viewUserModal);
        StackPane.setAlignment(viewUserModal, Pos.CENTER);
    }

    @Override
    public void onShow() {
        visualizer.setupAudioVisualization(BgmManager.getInstance().getCurrentPlayer());
        scene.setRoot(root);
        setInputManager();
        playlistModal.setInputManager(inputManager);
    }

    public void handleEvent() {
        playlistModal.setInputManager(inputManager);

        visualizer.getLogoRayGroup().setOnMouseClicked(e -> {
            toggleMenuPanel();
        });

        topBar.setUserCardClickHandler(e -> {
            if (!loginModal.isShowing() && !registerModal.isVisible()) {
                loginModal.clearFields();
                if (chatPanel.isVisible()) {
                    chatPanel.hide();
                    bottomBar.setFullOpacity();
                }
                loginModal.show();
            }
        });

        loginModal.setOnLoginSuccessListener(user -> {
            if (user != null) {
                topBar.updateUserInfo(user);
                updateBanchoButtonsVisibility();
            }
        });

        loginModal.setOnCreateAccountListener(() -> {
            loginModal.hide();
            registerModal.setVisible(true);
            registerModal.toFront();
        });

        root.setOnMouseClicked(e -> {
            if (loginModal.isShowing()) {
                Node target = (Node) e.getTarget();
                boolean clickedOnLoginModal = false;
                while (target != null) {
                    if (target == loginModal) {
                        clickedOnLoginModal = true;
                        break;
                    }
                    target = target.getParent();
                }
                if (!clickedOnLoginModal) {
                    loginModal.hide();
                }
            }

            if(settingsModal.isShowing()) {
                Node target = (Node) e.getTarget();
                boolean clickedOnSettingsModal = false;
                while (target != null) {
                    if (target == settingsModal) {
                        clickedOnSettingsModal = true;
                        break;
                    }
                    target = target.getParent();
                }
                if (!clickedOnSettingsModal) {
                    settingsModal.hide();
                }
            }

            if (chatPanel.isShowing() && !onlineUsersPanel.isShowing()) {
                Node target = (Node) e.getTarget();
                boolean clickedOnChatPanel = false;
                boolean clickedOnBanchoButtons = false;

                while (target != null) {
                    if (target == chatPanel) {
                        clickedOnChatPanel = true;
                        break;
                    }
                    if (target == banchoButtons) {
                        clickedOnBanchoButtons = true;
                        break;
                    }
                    target = target.getParent();
                }

                if (!clickedOnChatPanel && !clickedOnBanchoButtons) {
                    chatPanel.hide();
                    bottomBar.setFullOpacity();
                    banchoButtons.getChatToggleButton().setShowIcon();
                    updateBanchoPanelsMouseTransparency();
                }
            }
        });

        menuButtons.getPlayButton().setOnMouseClicked(e -> {
            showSubMenu();
        });
        menuButtons.getOptionButton().setOnMouseClicked(e -> {
            if(loginModal.isShowing()) loginModal.hide();

            if(!settingsModal.isShowing()) {
                if (chatPanel.isVisible()) {
                    chatPanel.hide();
                    bottomBar.setFullOpacity();
                }
                settingsModal.show();
            }
        });
        menuButtons.getExitButton().setOnMouseClicked(e -> {
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        subMenuButtons.getSoloButton().setOnMouseClicked(e -> {
            hideSubMenu();
            toggleMenuPanel();
            hideAllModals();
            ViewManager.getInstance().showHomeView();
        });

        subMenuButtons.getMultiButton().setOnMouseClicked(e -> {
            if (!AuthManager.isAuthenticated()) {
                Toast.error("You must be logged in to play online!").show();
                return;
            }

            hideSubMenu();
            toggleMenuPanel();
            ViewManager.getInstance().showLobbyView();
        });

        subMenuButtons.getBackButton().setOnMouseClicked(e -> {
            hideSubMenu();
        });

        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            if (banchoButtons.getOnlineUsersButton().isOnlineUserShown()) {
                onlineUsersPanel.hide();
                topBar.setFullOpacity();
                banchoButtons.getOnlineUsersButton().setOnlineUsersHiddenIcon();
            } else {
                onlineUsersPanel.show();
                topBar.setLowOpacity();
                banchoButtons.getOnlineUsersButton().setOnlineUsersShownIcon();

                if (!chatPanel.isShowing()) {
                    chatPanel.show();
                    bottomBar.setLowOpacity();
                    banchoButtons.getChatToggleButton().setHideIcon();
                }
            }
            updateBanchoPanelsMouseTransparency();
        });

        banchoButtons.getChatToggleButton().setOnMouseClicked(e -> {
            if (banchoButtons.getChatToggleButton().isChatVisible()) {
                chatPanel.hide();
                bottomBar.setFullOpacity();
                banchoButtons.getChatToggleButton().setShowIcon();

                if (onlineUsersPanel.isShowing()) {
                    onlineUsersPanel.hide();
                    topBar.setFullOpacity();
                    banchoButtons.getOnlineUsersButton().setOnlineUsersHiddenIcon();
                }
            } else {
                chatPanel.show();
                bottomBar.setLowOpacity();
                banchoButtons.getChatToggleButton().setHideIcon();
            }
            updateBanchoPanelsMouseTransparency();
        });

        jukebox.getMediaControls().getPlaylistButton().setOnAction(event -> {
            if (playlistModal.isVisible()) {
                playlistModal.hide();
                if (!banchoButtons.isVisible()) {
                    banchoButtons.show();
                }
            } else {
                if (chatPanel.isShowing()) {
                    chatPanel.hide();
                    banchoButtons.getChatToggleButton().setShowIcon();
                    bottomBar.setFullOpacity();
                }
                if (banchoButtons.isVisible()) {
                    banchoButtons.hide();
                }
                playlistModal.show();
            }
        });
    }

    private void updateBanchoPanelsMouseTransparency() {
        boolean shouldBeTransparent = !chatPanel.isShowing() && !onlineUsersPanel.isShowing();
        banchoPanelsContainer.setMouseTransparent(shouldBeTransparent);
    }
}