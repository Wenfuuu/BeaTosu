package beat.osu.client.view.landing;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import beat.osu.client.Main;
import beat.osu.client.controller.*;
import beat.osu.client.enums.PlaybackMode;
import beat.osu.client.helper.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
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
import beat.osu.client.view.shared.bancho.modals.ProfileModal;
import beat.osu.client.view.shared.bancho.modals.SelectChannelModal;
import beat.osu.client.view.shared.bancho.modals.ViewUserModal;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.client.view.shared.jukebox.Jukebox;
import beat.osu.client.view.shared.jukebox.modals.PlaylistModal;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.responses.LogoutResponse;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.dto.beatmap.responses.GetBeatmapByIdResponse;
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
    private ProfileModal profileModal;

    private VBox banchoPanelsContainer;

    private final AuthController authController;
    private final ConnectedUsersController connectedUsersController;
    private final ChatController chatController;
    private final SessionController sessionController;
    private final BeatmapController beatmapController;

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

    public LandingView(Stage stage, AuthController authController, ConnectedUsersController connectedUsersController,
            ChatController chatController, SessionController sessionController,
            SpectateController spectateController) {
        super(stage);

        this.authController = authController;
        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;
        this.sessionController = sessionController;
        this.beatmapController = new BeatmapController();

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

    private Beatmap fetchBeatmapById(int id) {
        File tempDir = ResourceManager.getTempDirectory();
        Set<String> validBeatmapDirs = new HashSet<>();

        if (tempDir.exists() && tempDir.isDirectory()) {
            for (File file : Objects.requireNonNull(tempDir.listFiles())) {
                if (file.isDirectory()) {
                    validBeatmapDirs.add(file.getName());
                }
            }
        }

        try {
            Result<GetBeatmapByIdResponse> result = beatmapController.getBeatmapById(id).get();

            if (result.isSuccess()) {
                BeatmapDto beatmapDto = result.getValue().getBeatmap();

                String expectedDirName = String.format("%d", beatmapDto.getBeatmapSetId());

                for (String dir : validBeatmapDirs) {
                    System.out.println("Found directory: " + dir);
                }

                if (!validBeatmapDirs.contains(expectedDirName)) {
                    System.out.println("Beatmap directory not found in temp directory: " + expectedDirName);
                    return null;
                }

                BeatmapSet beatmapSet = new BeatmapSet(
                        beatmapDto.getBeatmapSetDto().getId(),
                        beatmapDto.getBeatmapSetDto().getTitle(),
                        beatmapDto.getBeatmapSetDto().getArtist(),
                        beatmapDto.getBeatmapSetDto().getCreator(),
                        beatmapDto.getBeatmapSetDto().getLength(),
                        beatmapDto.getBeatmapSetDto().getBpm());

                return new Beatmap(
                        beatmapDto.getId(),
                        beatmapDto.getBeatmapSetDto().getId(),
                        beatmapDto.getVersion(),
                        beatmapDto.getHpDrainRate(),
                        beatmapDto.getCircleSize(),
                        beatmapDto.getOverallDifficulty(),
                        beatmapDto.getApproachRate(),
                        beatmapDto.getSliderMultiplier(),
                        beatmapDto.getSliderTickRate(),
                        beatmapDto.getStarRating(),
                        beatmapSet);

            } else {
                System.err.println("Failed to fetch beatmaps: " + result.getError().getMessage());
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error fetching beatmap: " + e.getMessage());
            return null;
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
        profileModal = new ProfileModal();

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

            chatPanel.startPrivateChat(privateChat);
        });

        viewUserModal.setOnStartSpectateCallback(spectateDto -> {
            if (AuthManager.getUser().getId() == spectateDto.getPlayingUserId()) {
                Toast.error("You cannot spectate yourself!").show();
                return;
            }

            System.out.println("Player with id " + spectateDto.getPlayingUserId()
                    + " is playing beatmap with id " + spectateDto.getBeatmapId());
            Beatmap beatmap = fetchBeatmapById(spectateDto.getBeatmapId());
            if (beatmap == null) {
                Toast.error("You don't have this beatmap").show();
                return;
            }

            Toast.information("Starting spectate " + spectateDto.getPlayingUsername()).show();
            ViewManager.getInstance().showSpectateView(beatmap, spectateDto);
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
                    userCard.getExperience(),
                    userCard.getRank(),
                    userCard.getIsSupporter(),
                    UserCardBehavior.STATIC);
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

        String bgmPath = "assets/audio/nekodex-circles.mp3";
        if (BgmManager.getInstance().getCurrentPlayer() == null) {
            String extractedBgmPath = ResourceManager.extractResourceToTempAndGetPath(bgmPath, "default-bgm.mp3");
            if (extractedBgmPath != null) {
                BgmManager.getInstance().playAudio(extractedBgmPath, PlaybackMode.DEFAULT);
            } else {
                System.err.println("Failed to extract default BGM resource: " + bgmPath);
            }
        }

        if (BgmManager.getInstance().getCurrentPlayer() != null) {
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

        root.getChildren().add(profileModal);
        StackPane.setAlignment(profileModal, Pos.CENTER);
    }

    @Override
    public void onShow() {
        try {
            BackgroundManager.setRandomBackground(scene);
        } catch (Exception e) {
            System.err.println("Error setting background: " + e.getMessage());
        }
        visualizer.setupAudioVisualization(BgmManager.getInstance().getCurrentPlayer());
        scene.setRoot(root);
        setInputManager();
        playlistModal.setInputManager(inputManager);
        BgmManager.getInstance().changePlaybackMode(PlaybackMode.PLAYLIST);
    }

    public void handleEvent() {
        playlistModal.setInputManager(inputManager);

        visualizer.getLogoRayGroup().setOnMouseClicked(e -> {
            toggleMenuPanel();
        });

        topBar.setUserCardClickHandler(e -> {
            if (!loginModal.isShowing() && !registerModal.isVisible()) {
                SfxManager.playSfx("menuhit.wav");
                loginModal.clearFields();
                if (chatPanel.isVisible()) {
                    chatPanel.hide();
                    bottomBar.setFullOpacity();
                }

                if (AuthManager.isAuthenticated()) {
                    profileModal.show();
                } else {
                    loginModal.show();
                }
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

        profileModal.getSignOutButton().setOnMouseClicked(e -> {
            try {
                Result<LogoutResponse> response = authController.logout().get();

                if (response.isSuccess()) {
                    profileModal.hide();
                    AuthManager.logout();
                    topBar.resetUserCard();
                    updateBanchoButtonsVisibility();
                    SfxManager.playSfx("menuhit.wav");
                    Toast.success(response.getValue().getMessage()).show();
                } else {
                    Toast.error("Failed to sign out: " + response.getError().getMessage()).show();
                }
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
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

            if (settingsModal.isShowing()) {
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
            SfxManager.playSfx("menuhit.wav");
            showSubMenu();
        });
        menuButtons.getOptionButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            if (loginModal.isShowing())
                loginModal.hide();

            if (!settingsModal.isShowing()) {
                if (chatPanel.isVisible()) {
                    chatPanel.hide();
                    bottomBar.setFullOpacity();
                }
                settingsModal.show();
            }
        });
        menuButtons.getExitButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        subMenuButtons.getSoloButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            hideSubMenu();
            toggleMenuPanel();
            hideAllModals();
            ViewManager.getInstance().showHomeView();
        });

        subMenuButtons.getMultiButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            if (!AuthManager.isAuthenticated()) {
                Toast.error("You must be logged in to play online!").show();
                return;
            }

            hideSubMenu();
            toggleMenuPanel();
            ViewManager.getInstance().showLobbyView();
        });

        subMenuButtons.getBackButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuback.wav");
            hideSubMenu();
        });

        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
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
            SfxManager.playSfx("menuhit.wav");
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
            SfxManager.playSfx("menuhit.wav");
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