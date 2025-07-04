package beat.osu.client.view.match;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.controller.ChatController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.controller.MatchController;
import beat.osu.client.controller.SessionController;
import beat.osu.client.enums.PlaybackMode;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.view.match.component.cards.BeatmapCard;
import beat.osu.client.view.match.component.layout.TopBar;
import beat.osu.client.view.match.component.modals.ChangePasswordModal;
import beat.osu.client.view.match.component.modals.HostActionsModal;
import beat.osu.client.view.match.component.modals.SelectBeatmapModal;
import beat.osu.client.view.match.component.panels.MatchSlotPanel;
import beat.osu.client.view.shared.bancho.buttons.BanchoButtons;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.client.view.shared.bancho.modals.SelectChannelModal;
import beat.osu.client.view.shared.bancho.modals.ViewUserModal;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.responses.JoinChannelResponse;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.HostChangedEvent;
import beat.osu.shared.dto.match.events.HostLeftEvent;
import beat.osu.shared.dto.match.events.MatchBeatmapUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchChangingBeatmapUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchNameUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchStartedEvent;
import beat.osu.shared.dto.match.events.MatchWinConditionUpdatedEvent;
import beat.osu.shared.dto.match.events.PlayerKickedEvent;
import beat.osu.shared.dto.match.events.PlayerStatusUpdatedEvent;
import beat.osu.shared.dto.match.events.SlotChangedEvent;
import beat.osu.shared.dto.match.events.UserJoinedMatchEvent;
import beat.osu.shared.dto.match.events.UserLeftMatchEvent;
import beat.osu.shared.dto.match.responses.ChangeMatchSlotResponse;
import beat.osu.shared.dto.match.responses.KickPlayerResponse;
import beat.osu.shared.dto.match.responses.LeaveMatchResponse;
import beat.osu.shared.dto.match.responses.TransferHostResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchNameResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchPasswordResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchWinConditionResponse;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.MatchWinCondition;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MatchView extends Page {

    private enum BlueButtonState {
        HIDDEN,
        READY,
        NOT_READY,
        START_GAME,
        FORCE_START_GAME
    }

    private StackPane root;

    private Integer matchId;
    private String matchName;
    private String matchPassword;
    private boolean inProgress;
    private boolean isChangingBeatmap;
    private int maxPlayerCount;

    private Beatmap beatmap;

    private MatchWinCondition winCondition;

    private final ConnectedUsersController connectedUsersController;
    private final ChatController chatController;
    private final MatchController matchController;
    private final SessionController sessionController;
    private final BeatmapController beatmapController;

    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private SelectChannelModal selectChannelModal;
    private ViewUserModal viewUserModal;
    private BanchoButtons banchoButtons;

    private TopBar topBar;
    private VBox mainContent;
    private VBox banchoPanelsContainer;

    private HostActionsModal hostActionsModal;
    private ChangePasswordModal changePasswordModal;
    private SelectBeatmapModal selectBeatmapModal;

    // left panel components
    private MatchSlotPanel matchSlotPanel;
    private Button leaveMatchButton;

    // Right panel components
    private Label gameNameLabel;
    private Button changePasswordButton;
    private TextField gameNameTextField;
    private PauseTransition changeGameNameTransition;
    private Label beatmapLabel;
    private Button changeBeatmapButton;
    private BeatmapCard beatmapCard;
    private VBox rightContent;
    private Label winConditionLabel;
    private ComboBox<String> winConditionComboBox;

    private Button blueButton;
    private BlueButtonState currentBlueButtonState = BlueButtonState.HIDDEN;

    private boolean isHost;
    private MatchPlayerDto selectedPlayerForHostAction;

    private final MatchDto matchDto;

    public MatchView(Stage stage, MatchDto matchDto, ConnectedUsersController connectedUsersController, ChatController chatController,
                     MatchController matchController, SessionController sessionController, BeatmapController beatmapController) {
        super(stage);

        this.matchDto = matchDto;
        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;
        this.matchController = matchController;
        this.sessionController = sessionController;
        this.beatmapController = beatmapController;

        this.matchId = matchDto.getId();
        this.matchName = matchDto.getName();
        this.matchPassword = matchDto.getPassword();
        this.inProgress = matchDto.isInProgress();
        this.isChangingBeatmap = matchDto.isChangingBeatmap();
        this.maxPlayerCount = matchDto.getMaxPlayerCount();
        this.beatmap = convertBeatmapDtoToBeatmap(matchDto.getBeatmap());
        this.winCondition = matchDto.getWinCondition();

        setupView();
        inputManager.setSfxDisabled(false);

        handleEvent();
        updateBlueButtonState();
        setupInputFieldSounds();
    }

    private void setupInputFieldSounds() {
        gameNameTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
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

        hostActionsModal = new HostActionsModal();
        changePasswordModal = new ChangePasswordModal();
        selectBeatmapModal = new SelectBeatmapModal(beatmapController);
        selectBeatmapModal.setMatchController(matchController);
        selectBeatmapModal.setMatchId(matchId);

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

        scene.setRoot(root);

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            scene.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        URL cssUrl = CssManager.getMatchCssURL("MatchView.css");
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

        matchSlotPanel = new MatchSlotPanel(maxPlayerCount, new ArrayList<>(matchDto.getPlayers()));
        matchSlotPanel.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.43);
        matchSlotPanel.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.43);
        matchSlotPanel.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.43);

        updateHostStatus();
        updateSlotCardCallback();

        leaveMatchButton = new Button("Leave Match");
        leaveMatchButton.getStyleClass().add("leave-match-button");

        VBox leftPanel = new VBox(matchSlotPanel);
        leftPanel.getStyleClass().add("left-panel");
        leftPanel.setMinWidth(ScreenManager.SCREEN_WIDTH / 2);
        leftPanel.setMaxWidth(ScreenManager.SCREEN_WIDTH / 2);
        leftPanel.setPrefWidth(ScreenManager.SCREEN_WIDTH / 2);

        gameNameLabel = new Label("Game Name");
        gameNameLabel.getStyleClass().add("game-name-label");

        changePasswordButton = new Button("Change Password...");
        changePasswordButton.getStyleClass().add("change-password-button");
        changePasswordButton.setVisible(false);

        HBox gameBox = new HBox(40);
        gameBox.getChildren().addAll(gameNameLabel, changePasswordButton);
        gameBox.setAlignment(Pos.CENTER_LEFT);

        gameNameTextField = new TextField(matchName);
        gameNameTextField.getStyleClass().add("game-name-text-field");
        gameNameTextField.setEditable(false);
        VBox.setMargin(gameNameTextField, new Insets(16, 20, 0, 0));

        changeGameNameTransition = new PauseTransition(Duration.millis(500));

        beatmapLabel = new Label("Beatmap");
        beatmapLabel.getStyleClass().add("beatmap-label");

        changeBeatmapButton = new Button("Change...");
        changeBeatmapButton.getStyleClass().add("change-beatmap-button");
        changeBeatmapButton.setVisible(false);

        HBox beatmapBox = new HBox(36);
        beatmapBox.getChildren().addAll(beatmapLabel, changeBeatmapButton);
        beatmapBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(beatmapBox, new Insets(40, 0, 20, 0));

        winConditionLabel = new Label("Win Condition:");
        winConditionLabel.getStyleClass().add("win-condition-label");

        winConditionComboBox = new ComboBox<>();
        winConditionComboBox.getStyleClass().add("dark-combo-box");
        winConditionComboBox.getItems().addAll(MatchWinCondition.getAllDisplayNames());
        winConditionComboBox.getSelectionModel().select(winCondition.getDisplayName());
        winConditionComboBox.setDisable(true);
        winConditionComboBox.setOpacity(1.0); 

        HBox winConditionBox = new HBox(20);
        winConditionBox.getChildren().addAll(winConditionLabel, winConditionComboBox);
        winConditionBox.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(winConditionBox, new Insets(20, 40, 0, 0));

        blueButton = new Button("Ready");
        blueButton.getStyleClass().add("ready-button");

        boolean beatmapExists = ResourceManager.beatmapSetDirectoryExists(beatmap.getBeatmapSetId());
        
        if (isChangingBeatmap) {
            beatmapCard = BeatmapCard.changingMap();
        } else if (beatmapExists) {
            beatmapCard = BeatmapCard.available(beatmap);
        } else {
            beatmapCard = BeatmapCard.noMap(beatmap.getBeatmapId(), beatmap.getBeatmapSetId(),
                beatmap.getBeatmapSet().getTitle(), beatmap.getBeatmapSet().getArtist());
        }

        rightContent = new VBox(gameBox, gameNameTextField, beatmapBox, beatmapCard, winConditionBox);
        rightContent.setPadding(new Insets(24, 0, 10, ScreenManager.SCREEN_WIDTH * 0.1));

        rightContent.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.43);
        rightContent.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.43);
        rightContent.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.43);

        VBox rightPanel = new VBox(rightContent);
        rightPanel.getStyleClass().add("right-panel");

        rightPanel.setMinWidth(ScreenManager.SCREEN_WIDTH / 2);
        rightPanel.setMaxWidth(ScreenManager.SCREEN_WIDTH / 2);
        rightPanel.setPrefWidth(ScreenManager.SCREEN_WIDTH / 2);

        HBox matchContent = new HBox(leftPanel, rightPanel);

        HBox buttonContainer = new HBox(ScreenManager.SCREEN_WIDTH * 0.045);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(12, 36, 12, 36));
        
        double buttonWidth = (ScreenManager.SCREEN_WIDTH * 0.955 - 72) / 2;
        
        leaveMatchButton.setPrefWidth(buttonWidth);
        leaveMatchButton.setMaxWidth(buttonWidth);
        leaveMatchButton.setMinWidth(buttonWidth);
        
        blueButton.setPrefWidth(buttonWidth);
        blueButton.setMaxWidth(buttonWidth);
        blueButton.setMinWidth(buttonWidth);
        
        buttonContainer.getChildren().addAll(leaveMatchButton, blueButton);

        mainContent = new VBox();
        mainContent.getChildren().addAll(topBar, matchContent, buttonContainer);

        VBox.setVgrow(matchContent, Priority.ALWAYS);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        StackPane.setMargin(mainContent, new Insets(0, 0, ScreenManager.SCREEN_HEIGHT * 0.35, 0));
        root.getChildren().add(mainContent);
        StackPane.setAlignment(mainContent, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(selectChannelModal);
        StackPane.setAlignment(selectChannelModal, Pos.CENTER);

        root.getChildren().add(banchoButtons);
        StackPane.setAlignment(banchoButtons, Pos.BOTTOM_RIGHT);

        root.getChildren().add(viewUserModal);
        StackPane.setAlignment(viewUserModal, Pos.CENTER);

        root.getChildren().add(hostActionsModal);
        StackPane.setAlignment(hostActionsModal, Pos.CENTER);

        root.getChildren().add(changePasswordModal);
        StackPane.setAlignment(changePasswordModal, Pos.CENTER);

        root.getChildren().add(selectBeatmapModal);
        StackPane.setAlignment(selectBeatmapModal, Pos.CENTER);
        
        updateUIBasedOnRole();
        updateEventHandlingBasedOnRole();
    }

    @Override
    public void onShow() {
        scene.setRoot(root);

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
                    Toast.error(response.getError().getMessage()).show();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        chatPanel.show();

        matchController.updatePlayerStatus(matchId, PlayerStatus.NOT_READY).thenAccept(result -> {
            if (result.isSuccess()) {
                System.out.println("Successfully updated status to NOT_READY");
            } else {
                Toast.error(result.getError().getMessage()).show();
            }
        });
        updateBlueButtonState();

        if (PlaylistManager.getInstance().getCurrentSong().getId() != beatmap.getBeatmapSetId()) {
            try {
                OsuParser.parseBeatmap(beatmap);
                BgmManager.getInstance().playPreviewBgm(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            BgmManager.getInstance().changePlaybackMode(PlaybackMode.PREVIEW);
        }
    }

    public void handleEvent() {
        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
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

        leaveMatchButton.setOnMouseEntered(e -> {
            SfxManager.playSfx("menuhover.wav");
        });

        leaveMatchButton.setOnMouseClicked(e -> {
            SfxManager.playSfx("menuback.wav");
            try {
                Result<LeaveMatchResponse> response = matchController.leaveMatch(matchId).get();
                if (response.isSuccess()) {
                    LeaveMatchResponse leaveMatchResponse = response.getValue();
                    Toast.success(leaveMatchResponse.getMessage()).show();
                    ViewManager.getInstance().showLobbyView();
                } else {
                    Toast.error("Failed to leave match: " + response.getError().getMessage()).show();
                }
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });

        blueButton.setOnMouseEntered(e -> {
            SfxManager.playSfx("menuhover.wav");
        });

        blueButton.setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            PlayerStatus currentStatus = getCurrentUserStatus();
            BlueButtonState currentState = currentBlueButtonState;
            
            if (currentStatus == PlayerStatus.NOT_READY) {
                matchController.updatePlayerStatus(matchId, PlayerStatus.READY).thenAccept(result -> {
                    if (result.isSuccess()) {
                        System.out.println("Successfully updated status to: READY");
                    } else {
                        Toast.error("Failed to update status: " + result.getError().getMessage()).show();
                    }
                });
            } else if (currentStatus == PlayerStatus.READY) {
                if (isHost) {
                    switch (currentState) {
                        case NOT_READY:
                            matchController.updatePlayerStatus(matchId, PlayerStatus.NOT_READY).thenAccept(result -> {
                                if (result.isSuccess()) {
                                    System.out.println("Successfully updated status to: NOT_READY");
                                } else {
                                    Toast.error("Failed to update status: " + result.getError().getMessage()).show();
                                }
                            });
                            break;
                            
                        case START_GAME:
                        case FORCE_START_GAME:
                            matchController.startMatch(matchId).thenApply(response -> {
                                if (response.isSuccess()) {
                                    System.out.println("Successfully start match: " + response.getValue().getMessage());
                                } else {
                                    System.err.println("Failed to start match: " + response.getError().getMessage());
                                    Toast.error("Failed to start match: " + response.getError().getMessage()).show();
                                }
                                return null;
                            });
                            break;
                            
                        default:
                            System.err.println("Unexpected ready button state for host: " + currentState);
                            break;
                    }
                } else {
                    matchController.updatePlayerStatus(matchId, PlayerStatus.NOT_READY).thenAccept(result -> {
                        if (result.isSuccess()) {
                            System.out.println("Successfully updated status to: NOT_READY");
                        } else {
                            Toast.error("Failed to update status: " + result.getError().getMessage()).show();
                        }
                    });
                }
            }
        });

        hostActionsModal.getTransferHostButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            MatchPlayerDto selectedPlayer = selectedPlayerForHostAction;
            if (selectedPlayer != null) {
                try {
                    Result<TransferHostResponse> result = matchController.transferHost(matchId, selectedPlayer.getUserId()).get();
                    TransferHostResponse transferHostResponse = result.getValue();
                    if (result.isSuccess()) {
                        hostActionsModal.hide();
                        Toast.success(transferHostResponse.getMessage()).show();
                    } else {
                        Toast.error("Failed to kick player: " + result.getError().getMessage()).show();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                Toast.error("No player selected for kicking.").show();
            }
        });

        hostActionsModal.getKickPlayerButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            MatchPlayerDto selectedPlayer = selectedPlayerForHostAction;
            if (selectedPlayer != null) {
                try {
                    Result<KickPlayerResponse> result = matchController.kickPlayerFromMatch(matchId, selectedPlayer.getUserId()).get();
                    if (result.isSuccess()) {
                        hostActionsModal.hide();
                        Toast.success("Player " + selectedPlayer.getUser().getUsername() + " has been kicked.").show();
                    } else {
                        Toast.error("Failed to kick player: " + result.getError().getMessage()).show();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                Toast.error("No player selected for kicking.").show();
            }
        });

        hostActionsModal.getUserOptionsButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            MatchPlayerDto selectedPlayer = selectedPlayerForHostAction;
            if (selectedPlayer != null) {
                UserCard userCard = new UserCard(
                        selectedPlayer.getUserId(),
                        selectedPlayer.getUser().getUsername(),
                        selectedPlayer.getUser().getCountryCode(),
                        selectedPlayer.getUser().getProfilePicture(),
                        selectedPlayer.getUser().getPerformance(),
                        selectedPlayer.getUser().getAccuracy(),
                        selectedPlayer.getUser().getPlayCount(),
                        selectedPlayer.getUser().getLevel(),
                        selectedPlayer.getUser().getRank(),
                        selectedPlayer.getUser().isSupporter(),
                        UserCardBehavior.STATIC
                );
                viewUserModal.updateUserCard(userCard);
                hostActionsModal.hide();
                viewUserModal.show();
            } else {
                Toast.error("No player selected for user options.").show();
            }
        });

        changePasswordButton.setOnMouseEntered(e -> {
            SfxManager.playSfx("menuhover.wav");
        });

        changePasswordButton.setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            changePasswordModal.show();
        });

        changeBeatmapButton.setOnMouseEntered(e -> {
            SfxManager.playSfx("menuhover.wav");
        });

        changeBeatmapButton.setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            selectBeatmapModal.setInputManager(inputManager);
            selectBeatmapModal.setCurrentMatchBeatmap(beatmap);
            selectBeatmapModal.setOnBeatmapSelectedCallback(selectedBeatmap -> {
                try {
                    Result<beat.osu.shared.dto.match.responses.UpdateMatchBeatmapResponse> result = 
                        matchController.updateMatchBeatmap(matchId, selectedBeatmap.getBeatmapId()).get();
                    
                    if (result.isSuccess()) {
                        Toast.success("Beatmap updated successfully!").show();
                        System.out.println("Successfully updated beatmap to: " + selectedBeatmap.getBeatmapSet().getTitle() + " [" + selectedBeatmap.getVersion() + "]");
                    } else {
                        Toast.error(result.getError().getMessage()).show();
                    }
                } catch (Exception ex) {
                    Toast.error(ex.getMessage()).show();
                }
            });
            selectBeatmapModal.show();
        });

        changePasswordModal.getConfirmButton().setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            try {
                String newPassword = changePasswordModal.getPassword();
                if (newPassword == null || newPassword.isEmpty()) {
                    Toast.error("Password cannot be empty.").show();
                    return;
                }

                Result<UpdateMatchPasswordResponse> result = matchController.updateMatchPassword(matchId, newPassword).get();

                if (result.isSuccess()) {
                    changePasswordModal.hide();
                    Toast.success(result.getValue().getMessage()).show();
                } else {
                    Toast.error(result.getError().getMessage()).show();
                }
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });

        setupMatchCallbacks();
    }

    private void setupMatchCallbacks() {
        matchController.addUserJoinedMatchCallback(this::onUserJoinedMatch);
        matchController.addUserLeftMatchCallback(this::onUserLeftMatch);
        matchController.addPlayerKickedCallback(this::onPlayerKicked);
        matchController.addMatchStartedCallback(this::onMatchStarted);
        matchController.addHostChangedCallback(this::onHostChanged);
        matchController.addHostLeftCallback(this::onHostLeft);
        matchController.addSlotChangedCallback(this::onSlotChanged);
        matchController.addMatchNameUpdatedCallback(this::onMatchNameUpdated);
        matchController.addMatchBeatmapUpdatedCallback(this::onMatchBeatmapUpdated);
        matchController.addMatchChangingBeatmapUpdatedCallback(this::onMatchChangingBeatmapUpdated);
        matchController.addMatchWinConditionUpdatedCallback(this::onMathWinConditionUpdated);
        matchController.addPlayerStatusUpdatedCallback(this::onPlayerStatusUpdated);
    }

    private void onUserJoinedMatch(UserJoinedMatchEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.addPlayer(event.getMatchPlayer());
                updateBlueButtonState();
            });
        }
    }

    private void onUserLeftMatch(UserLeftMatchEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.removePlayer(event.getUserId());
                updateBlueButtonState();
            });
        }
    }

    private void onPlayerKicked(PlayerKickedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.removePlayer(event.getKickedUserId());

                if (event.getKickedUserId() == AuthManager.getUser().getId()) {
                    ViewManager.getInstance().showLobbyView();
                    Toast.error("You have been kicked from the match.").show();
                } else {
                    updateBlueButtonState();
                }
            });
        }
    }

    private void onMatchStarted(MatchStartedEvent event) {
        if (event.getMatchId() == this.matchId) {
            List<MatchPlayerDto> players = event.getMatchDto().getPlayers();
            UserDto currentUser = AuthManager.getUser();
            MatchPlayerDto player = players.stream()
                    .filter(p -> p.getUserId() == currentUser.getId())
                    .findFirst()
                    .orElse(null);
            System.out.println("Match started, player status is " + (player != null ? player.getStatus() : "not found"));
            if (player == null) return;

            for (MatchPlayerDto matchPlayer : players) {
                if (matchPlayer.getStatus() == PlayerStatus.READY) matchPlayer.setStatus(PlayerStatus.PLAYING);
                System.out.println("notifying player status update for user: " + matchPlayer.getUser().getUsername()
                        + ", status: " + matchPlayer.getStatus());
                PlayerStatusUpdatedEvent updatedEvent = new PlayerStatusUpdatedEvent(
                        matchId, matchPlayer.getUserId(), matchPlayer.getStatus());
                onPlayerStatusUpdated(updatedEvent);
            }

            boolean isCurrentUserPlaying = player.getStatus() == PlayerStatus.PLAYING;
            if (!isCurrentUserPlaying) return;
            Platform.runLater(() -> {
                BgmManager.getInstance().stopBgm();
//                ViewManager.getInstance().setCurrentMatchDto(event.getMatchDto());
                ViewManager.getInstance().showGameView(beatmap, true);
            });
        }
    }

    private void onHostChanged(HostChangedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.updateHost(event.getNewHostUserId(), event.getPreviousHostUserId());
                updateHostStatus();
                updateBlueButtonState();
                updateUIBasedOnRole();
                updateEventHandlingBasedOnRole();
                updateSlotCardCallback();
            });
        }
    }

    private void onHostLeft(HostLeftEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.hostLeft(event.getPreviousHostUserId(), event.getNewHostUserId());
                updateHostStatus();
                updateBlueButtonState();
                updateUIBasedOnRole();
                updateEventHandlingBasedOnRole();
                updateSlotCardCallback();
            });
        }
    }

    private void onSlotChanged(SlotChangedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.movePlayerToSlot(event.getUserId(), event.getOldSlotIndex(), event.getNewSlotIndex());
            });
        }
    }

    private void onMatchNameUpdated(MatchNameUpdatedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                if (!isHost) {
                    matchName = event.getNewName();
                    gameNameTextField.setText(matchName);
                }
            });
        }
    }

    private void onMatchBeatmapUpdated(MatchBeatmapUpdatedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                beatmap = convertBeatmapDtoToBeatmap(event.getNewBeatmapDto());
                this.isChangingBeatmap = false;
                updateBeatmapCard();
                
                boolean beatmapExists = ResourceManager.beatmapSetDirectoryExists(beatmap.getBeatmapSetId());
                if (!beatmapExists) {
                    PlayerStatus currentStatus = getCurrentUserStatus();
                    if (currentStatus != PlayerStatus.NO_MAP) {
                        matchController.updatePlayerStatus(matchId, PlayerStatus.NO_MAP).thenAccept(result -> {
                            if (!result.isSuccess()) {
                                System.err.println("Failed to update player status to NO_MAP: " + result.getError().getMessage());
                            }
                        });
                    }
                } else {
                    if (PlaylistManager.getInstance().getCurrentSong().getId() != beatmap.getBeatmapSetId()) {
                        try {
                            OsuParser.parseBeatmap(beatmap);
                            BgmManager.getInstance().playPreviewBgm(true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
                updateBlueButtonState();
            });
        }
    }

    private void onMatchChangingBeatmapUpdated(MatchChangingBeatmapUpdatedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                this.isChangingBeatmap = event.isChangingBeatmap();
                updateBeatmapCard();
            });
        }
    }

    private void onMathWinConditionUpdated(MatchWinConditionUpdatedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                if (!isHost) {
                    winCondition = event.getNewWinCondition();
                    winConditionComboBox.getSelectionModel().select(winCondition.getDisplayName());
                }
            });
        }
    }

    private void onPlayerStatusUpdated(PlayerStatusUpdatedEvent event) {
        if (event.getMatchId() == this.matchId) {
            Platform.runLater(() -> {
                matchSlotPanel.updatePlayerStatus(event.getUserId(), event.getNewStatus());
                
                updateBlueButtonState();
            });
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

    private void updateSlotCardCallback() {
        if (isHost) {
            matchSlotPanel.setSlotCardClickCallback(card -> {
                if (card.getUser() != null) {
                    MatchPlayerDto selected = matchSlotPanel.getPlayerByUserId(card.getUser().getId());
                    selectedPlayerForHostAction = selected;
                    if (selected != null && selected.getRole().equals(PlayerRole.PLAYER)) {
                        hostActionsModal.show(selected.getUser().getUsername());
                    }
                } else {
                    handleSlotChange(card.getMatchSlotIndex());
                }
            });
        } else {
            matchSlotPanel.setSlotCardClickCallback(card -> {
                if (card.getUser() != null) {
                    int currentUserId = AuthManager.getUser().getId();
                    if (card.getUser().getId() == currentUserId) {
                        return;
                    }
                    
                    UserCard modalUserCard = new UserCard(
                            card.getUser().getId(),
                            card.getUser().getUsername(),
                            card.getUser().getCountryCode(),
                            card.getUser().getProfilePicture(),
                            card.getUser().getPerformance(),
                            card.getUser().getAccuracy(),
                            card.getUser().getPlayCount(),
                            card.getUser().getLevel(),
                            card.getUser().getRank(),
                            card.getUser().isSupporter(),
                            UserCardBehavior.STATIC
                    );
                    viewUserModal.updateUserCard(modalUserCard);
                    viewUserModal.show();
                } else {
                    handleSlotChange(card.getMatchSlotIndex());
                }
            });
        }
    }

    private void handleSlotChange(int targetSlotIndex) {
        int currentUserId = AuthManager.getUser().getId();
        
        MatchPlayerDto currentPlayer = matchSlotPanel.getPlayerByUserId(currentUserId);
                
        if (currentPlayer == null) {
            Toast.error("You are not in this match").show();
            return;
        }
        
        int currentSlotIndex = currentPlayer.getMatchSlotIndex();
        
        if (currentSlotIndex == targetSlotIndex) {
            Toast.error("You are already in that slot").show();
            return;
        }
        
        if (!matchSlotPanel.isSlotEmpty(targetSlotIndex)) {
            Toast.error("That slot is already occupied").show();
            return;
        }

        try {
            Result<ChangeMatchSlotResponse> result = matchController.changeMatchSlot(matchId, targetSlotIndex).get();
            if (result.isSuccess()) {
                System.out.println(result.getValue().getMessage());
            } else {
                Toast.error("Failed to change slot: " + result.getError().getMessage()).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUIBasedOnRole() {
        topBar.updateSubtitle(isHost);
        gameNameTextField.setEditable(isHost);
        changePasswordButton.setVisible(isHost);
        changeBeatmapButton.setVisible(isHost);
        winConditionComboBox.setDisable(!isHost);
        winConditionComboBox.setOpacity(1.0);
    }

    private void updateEventHandlingBasedOnRole() {
        if (isHost) {
            changeGameNameTransition.setOnFinished(e -> {
                try {
                    String newGameName = gameNameTextField.getText().trim();
                    Result<UpdateMatchNameResponse> result = matchController.updateMatchName(matchId, newGameName).get();

                    if (result.isSuccess()) {
                        System.out.println(result.getValue().getMessage());
                    } else {
                        Toast.error("Failed to update match password: " + result.getError().getMessage()).show();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            });

            gameNameTextField.textProperty().addListener((obs, oldVal, newVal) -> {
                changeGameNameTransition.pause();
                changeGameNameTransition.play();
            });

            winConditionComboBox.setOnAction(e -> {
                System.out.println("Win condition changed to: " + winConditionComboBox.getValue());
                try {
                    MatchWinCondition newWinCondition = MatchWinCondition.fromString(winConditionComboBox.getValue());
                    Result<UpdateMatchWinConditionResponse> result = matchController.updateMatchWinCondition(matchId, newWinCondition).get();

                    if (result.isSuccess()) {
                        System.out.println(result.getValue().getMessage());
                    } else {
                        Toast.error("Failed to update match win condition: " + result.getError().getMessage()).show();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } else {
            changeGameNameTransition.setOnFinished(null);

            gameNameTextField.textProperty().removeListener((obs, oldVal, newVal) -> {
                changeGameNameTransition.pause();
                changeGameNameTransition.play();
            });

            winConditionComboBox.setOnAction(null);
        }
    }

    private void updateHostStatus() {
        int currentUserId = AuthManager.getUser().getId();
        this.isHost = matchSlotPanel.isUserHost(currentUserId);
    }

    private PlayerStatus getCurrentUserStatus() {
        int currentUserId = AuthManager.getUser().getId();
        return matchSlotPanel.getPlayerStatus(currentUserId);
    }

    private int getReadyPlayersCount() {
        return (int) matchSlotPanel.getPlayers().stream()
                .filter(player -> player.getStatus() == PlayerStatus.READY)
                .count();
    }

    private int getTotalPlayersCount() {
        return matchSlotPanel.getPlayerCount();
    }

    private boolean areAllPlayersReady() {
        List<MatchPlayerDto> players = matchSlotPanel.getPlayers();
        return !players.isEmpty() && players.stream()
                .allMatch(player -> player.getStatus() == PlayerStatus.READY);
    }

    private BlueButtonState determineBlueButtonState() {
        PlayerStatus currentStatus = getCurrentUserStatus();
        
        switch (currentStatus) {
            case NO_MAP:
            case PLAYING:
            case FINISHED:
                return BlueButtonState.HIDDEN;
                
            case NOT_READY:
                return BlueButtonState.READY;
                
            case READY:
                if (isHost) {
                    int readyCount = getReadyPlayersCount();

                    if (readyCount == 1) {
                        return BlueButtonState.NOT_READY;
                    } else if (areAllPlayersReady()) {
                        return BlueButtonState.START_GAME;
                    } else {
                        return BlueButtonState.FORCE_START_GAME;
                    }
                } else {
                    return BlueButtonState.NOT_READY;
                }
                
            default:
                return BlueButtonState.HIDDEN;
        }
    }

    private void applyBlueButtonState(BlueButtonState state) {
        Platform.runLater(() -> {
            switch (state) {
                case HIDDEN:
                    blueButton.setVisible(false);
                    break;

                case READY:
                    blueButton.setVisible(true);
                    blueButton.setText("Ready");
                    break;

                case NOT_READY:
                    blueButton.setVisible(true);
                    blueButton.setText("Not Ready");
                    break;

                case START_GAME:
                    blueButton.setVisible(true);
                    blueButton.setText("Start Game!");
                    break;

                case FORCE_START_GAME:
                    blueButton.setVisible(true);
                    int readyCount = getReadyPlayersCount();
                    int totalCount = getTotalPlayersCount();
                    blueButton.setText("Force Start Game! (" + readyCount + "/" + totalCount + ")");
                    break;
            }
        });
    }

    private Beatmap convertBeatmapDtoToBeatmap(BeatmapDto beatmapDto) {
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
    }
    
    private void updateBeatmapCard() {
        boolean beatmapExists = ResourceManager.beatmapSetDirectoryExists(beatmap.getBeatmapSetId());
        
        BeatmapCard newBeatmapCard;
        if (isChangingBeatmap) {
            newBeatmapCard = BeatmapCard.changingMap();
        } else if (beatmapExists) {
            newBeatmapCard = BeatmapCard.available(beatmap);
        } else {
            newBeatmapCard = BeatmapCard.noMap(beatmap.getBeatmapId(), beatmap.getBeatmapSetId(),
                    beatmap.getBeatmapSet().getTitle(), beatmap.getBeatmapSet().getArtist());
        }
        
        int beatmapCardIndex = rightContent.getChildren().indexOf(beatmapCard);
        if (beatmapCardIndex != -1) {
            rightContent.getChildren().set(beatmapCardIndex, newBeatmapCard);
            beatmapCard = newBeatmapCard;
        }
    }

    private void updateBlueButtonState() {
        updateHostStatus();
        BlueButtonState newState = determineBlueButtonState();
        
        if (newState != currentBlueButtonState) {
            currentBlueButtonState = newState;
            applyBlueButtonState(newState);
        }
    }
}
