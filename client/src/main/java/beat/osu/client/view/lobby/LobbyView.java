package beat.osu.client.view.lobby;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

import beat.osu.client.controller.*;
import beat.osu.client.enums.PlaybackMode;
import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.view.lobby.component.cards.MatchCard;
import beat.osu.client.view.lobby.component.layout.NavigationBar;
import beat.osu.client.view.lobby.component.layout.TopBar;
import beat.osu.client.view.lobby.component.modals.CreateMatchModal;
import beat.osu.client.view.lobby.component.modals.JoinMatchModal;
import beat.osu.client.view.lobby.component.panels.MatchesPanel;
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
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.beatmap.responses.GetBeatmapByIdResponse;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.responses.JoinChannelResponse;
import beat.osu.shared.dto.match.responses.JoinMatchResponse;
import beat.osu.shared.dto.match.events.MatchPasswordUpdatedEvent;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LobbyView extends Page {

    private StackPane root;

    private final ConnectedUsersController connectedUsersController;
    private final ChatController chatController;
    private final SessionController sessionController;
    private final BeatmapController beatmapController;
    private final MatchController matchController;

    private PlaylistModal playlistModal;
    private Jukebox jukebox;

    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private SelectChannelModal selectChannelModal;
    private ViewUserModal viewUserModal;
    private BanchoButtons banchoButtons;

    private TopBar topBar;
    private NavigationBar navigationBar;
    private VBox mainContent;
    private MatchesPanel matchesPanel;
    
    private VBox banchoPanelsContainer;
    private CreateMatchModal createMatchModal;
    private JoinMatchModal joinMatchModal;

    public LobbyView(Stage stage, ConnectedUsersController connectedUsersController, ChatController chatController,
                     SessionController sessionController, BeatmapController beatmapController, MatchController matchController) {
        super(stage);

        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;
        this.sessionController = sessionController;
        this.beatmapController = beatmapController;
        this.matchController = matchController;

        setupView();
        handleEvent();
        setupMatchEventHandlers();
    }

    private void setupMatchEventHandlers() {
        matchController.addMatchPasswordUpdatedCallback(this::onMatchPasswordUpdated);
    }

    private void onMatchPasswordUpdated(MatchPasswordUpdatedEvent event) {
        javafx.application.Platform.runLater(() -> {
            matchesPanel.updateMatchPassword(event.getMatchId(), event.getNewPassword());
        });
    }

    @Override
    public void init() {
        root = new StackPane();

        Pane backgroundOverlay = new Pane();
        backgroundOverlay.getStyleClass().add("background-overlay");
        backgroundOverlay.prefWidthProperty().bind(root.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(backgroundOverlay);

        topBar = new TopBar();
        navigationBar = new NavigationBar();
        matchesPanel = new MatchesPanel(matchController);
        
        matchesPanel.setMatchCountUpdateCallback((filteredCount, totalCount) -> {
            topBar.updateMatchCount(filteredCount, totalCount);
        });

        playlistModal = new PlaylistModal();
        PlaylistManager.getInstance().addListener(playlistModal);

        jukebox = new Jukebox(playlistModal);
        PlaylistManager.getInstance().addListener(jukebox);

        banchoButtons = new BanchoButtons();

        onlineUsersPanel = new OnlineUsersPanel(connectedUsersController);
        selectChannelModal = new SelectChannelModal(chatController.getChannelController(), banchoButtons);
        chatPanel = new ChatPanel(chatController, selectChannelModal, onlineUsersPanel, banchoButtons);
        
        viewUserModal = new ViewUserModal(sessionController);

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
                    UserCardBehavior.STATIC
            );
            viewUserModal.updateUserCard(modalUserCard);
            viewUserModal.show();
        });

        selectChannelModal.setChatPanel(chatPanel);
        selectChannelModal.setOnlineUsersPanel(onlineUsersPanel);

        banchoPanelsContainer = new VBox();
        banchoPanelsContainer.setMaxWidth(Double.MAX_VALUE);
        banchoPanelsContainer.setMaxHeight(Double.MAX_VALUE);
        banchoPanelsContainer.setMouseTransparent(true);
        
        onlineUsersPanel.setMaxWidth(Double.MAX_VALUE);
        chatPanel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(onlineUsersPanel, Priority.ALWAYS);
        chatPanel.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        chatPanel.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        chatPanel.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        
        banchoPanelsContainer.getChildren().addAll(onlineUsersPanel, chatPanel);
        
        banchoPanelsContainer.setVisible(false);
        banchoPanelsContainer.setManaged(false);

        playlistModal.setVisible(false);

        createMatchModal = new CreateMatchModal(matchController);
        joinMatchModal = new JoinMatchModal();
        
        scene.setRoot(root);

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            scene.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        URL cssUrl = CssManager.getLobbyCssURL("LobbyView.css");
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
    }

    @Override
    public void setLayout() {
        root.getChildren().add(banchoPanelsContainer);
        StackPane.setAlignment(banchoPanelsContainer, Pos.TOP_CENTER);

        mainContent = new VBox();
        mainContent.getChildren().addAll(topBar, matchesPanel, navigationBar);
        VBox.setMargin(topBar, new Insets(0, 0, 0, 12));

        VBox.setVgrow(matchesPanel, Priority.ALWAYS);
        StackPane.setMargin(mainContent, new Insets(0, 0, ScreenManager.SCREEN_HEIGHT * 0.35, 0));
        root.getChildren().add(mainContent);
        StackPane.setAlignment(mainContent, Pos.BOTTOM_CENTER);
        mainContent.setAlignment(Pos.BOTTOM_CENTER);

        root.getChildren().addAll(playlistModal, selectChannelModal);
        StackPane.setAlignment(playlistModal, Pos.CENTER);
        StackPane.setAlignment(selectChannelModal, Pos.CENTER);

        root.getChildren().add(jukebox);
        StackPane.setAlignment(jukebox, Pos.TOP_RIGHT);

        root.getChildren().add(banchoButtons);
        StackPane.setAlignment(banchoButtons, Pos.BOTTOM_RIGHT);

        root.getChildren().add(viewUserModal);
        StackPane.setAlignment(viewUserModal, Pos.CENTER);

        root.getChildren().add(createMatchModal);
        StackPane.setAlignment(createMatchModal, Pos.CENTER);

        root.getChildren().add(joinMatchModal);
        StackPane.setAlignment(joinMatchModal, Pos.CENTER);
    }

    @Override
    public void onShow() {
        scene.setRoot(root);

        if (PlaylistManager.getInstance().isNoSongPlaying()) {
            PlaylistManager.getInstance().playRandomSong();
        }

        BgmManager.getInstance().changePlaybackMode(PlaybackMode.PLAYLIST);
        setInputManager();
        inputManager.setSfxDisabled(false);
        playlistModal.setInputManager(inputManager);

        banchoPanelsContainer.setVisible(true);
        banchoPanelsContainer.setManaged(true);
        banchoPanelsContainer.setMouseTransparent(false);

        boolean hasJoinedLobby = false;
        chatController.loadJoinedChannels();
        chatController.loadExistingPrivateChats();

        for (ChannelDto channel : chatController.getJoinedChannels()) {
            if (channel.getId() == 3) {
                hasJoinedLobby = true;
                chatPanel.getChatTabs().selectTab(channel);
                break;
            }
        }

        if (!hasJoinedLobby) {
            try {
                Result<JoinChannelResponse> response = chatController.getChannelController().joinChannel(3).get();
                if (response.isSuccess()) {
                    chatPanel.getChatTabs().selectTab(response.getValue().getChannel());
                    Toast.success("Successfully joined lobby!").show();
                } else {
                    Toast.error("Failed to join lobby: " + response.getError().getMessage()).show();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        chatPanel.show();
        matchesPanel.loadInitialMatches();
    }

    public void handleEvent() {
        playlistModal.setInputManager(inputManager);

        matchesPanel.setMatchCardClickCallback(matchCard -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            boolean hasPassword = matchCard.hasPassword();
            
            if (hasPassword) {
                joinMatchModal.showForMatch(matchCard.getMatchId(), matchCard.getMatchName());
            } else {
                joinMatch(matchCard.getMatchId(), matchCard.getMatchName());
            }
        });

        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            if (banchoButtons.getOnlineUsersButton().isOnlineUserShown()) {
                onlineUsersPanel.hide();
                banchoButtons.getOnlineUsersButton().setOnlineUsersHiddenIcon();
                showMainContent();
            } else {
                onlineUsersPanel.show();
                banchoButtons.getOnlineUsersButton().setOnlineUsersShownIcon();
                hideMainContent();
            }
        });

        navigationBar.getBackButton().setOnMouseClicked(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_BACK);
            ViewManager.getInstance().showLandingView();
        });

        navigationBar.getNewGameButton().setOnMouseClicked(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            createMatchModal.show();
        });

        navigationBar.getQuickJoinButton().setOnMouseClicked(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            quickJoinMatch();
        });

        jukebox.getMediaControls().getPlaylistButton().setOnAction(event -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            if (playlistModal.isVisible()) {
                playlistModal.hide();
                banchoPanelsContainer.setVisible(true);
                banchoPanelsContainer.setManaged(true);
                banchoPanelsContainer.setMouseTransparent(false);
                chatPanel.setVisible(true);
                if (!onlineUsersPanel.isVisible()) {
                    showMainContent();
                }
                if (!banchoButtons.isVisible()) {
                    banchoButtons.show();
                }
            } else {
                banchoPanelsContainer.setVisible(false);
                banchoPanelsContainer.setManaged(false);
                banchoPanelsContainer.setMouseTransparent(true);
                hideMainContent();
                if (banchoButtons.isVisible()) {
                    banchoButtons.hide();
                }
                playlistModal.show();
            }
        });

        createMatchModal.getCancelButton().setOnAction(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_BACK);
            createMatchModal.hide();
        });

        joinMatchModal.getCancelButton().setOnAction(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_BACK);
            joinMatchModal.hide();
        });

        joinMatchModal.getJoinGameButton().setOnAction(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            Integer matchId = joinMatchModal.getSelectedMatchId();
            String password = joinMatchModal.getPassword();

            joinMatch(matchId, password);
            joinMatchModal.hide();
        });
    }

    private void joinMatch(int matchId, String password) {
        try {
            Result<JoinMatchResponse> response = matchController.joinMatch(matchId, password).get();
            if (response.isSuccess()) {
                JoinMatchResponse joinResponse = response.getValue();
                ViewManager.getInstance().showMatchView(joinResponse.getMatch());
                Toast.success(joinResponse.getMessage()).show();
            } else {
                Toast.error("Failed to join match: " + response.getError().getMessage()).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void showMainContent() {
        mainContent.setVisible(true);
        mainContent.setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainContent);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void hideMainContent() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
        });

        fadeOut.play();
    }

    private void quickJoinMatch() {
        try {
            List<MatchCard> suitableMatches = matchesPanel.getSuitableMatchesForQuickJoin();
            
            if (suitableMatches.isEmpty()) {
                Toast.error("No suitable matches found!").show();
                return;
            }
            
            Random random = new Random();
            MatchCard selectedMatchCard = suitableMatches.get(random.nextInt(suitableMatches.size()));
            
            joinMatch(selectedMatchCard.getMatchId(), null);
            
        } catch (Exception e) {
            Toast.error("Failed to quick join: " + e.getMessage()).show();
        }
    }

    private Beatmap fetchBeatmapById(int id) {
        File tempDir = ResourceManager.getBeatmapDirectory();
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
}

