package beat.osu.client.view.match;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.controller.ChatController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.controller.MatchController;
import beat.osu.client.controller.SessionController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.view.match.component.cards.BeatmapCard;
import beat.osu.client.view.match.component.layout.TopBar;
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
import beat.osu.shared.dto.beatmap.responses.GetBeatmapByIdResponse;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MatchView extends Page {

    private StackPane root;

    private Integer matchId;
    private String matchName;
    private String matchPassword;
    private boolean inProgress;
    private int maxPlayerCount;

    private int beatmapId;
    private String beatmapName;

    private String winCondition;

    private List<MatchPlayerDto> players;


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

    // left panel components
    private MatchSlotPanel matchSlotPanel;
    private Button leaveMatchButton;

    // Right panel components
    private Label gameNameLabel;
    private Button changePasswordButton;
    private TextField gameNameTextField;
    private Label beatmapLabel;
    private Button changeBeatmapButton;
    private BeatmapCard beatmapCard;
    private Label winConditionLabel;
    private ComboBox<String> winConditionComboBox;

    private Button readyButton;

    public MatchView(Stage stage, MatchDto matchDto, ConnectedUsersController connectedUsersController, ChatController chatController,
                     MatchController matchController, SessionController sessionController, BeatmapController beatmapController) {
        super(stage);

        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;
        this.matchController = matchController;
        this.sessionController = sessionController;
        this.beatmapController = beatmapController;

        this.matchId = matchDto.getId();
        this.matchName = matchDto.getName();
        this.matchPassword = matchDto.getPassword();
        this.inProgress = matchDto.isInProgress();
        this.maxPlayerCount = matchDto.getMaxPlayerCount();
        this.beatmapId = matchDto.getBeatmapId();
        this.beatmapName = matchDto.getBeatmapName();
        this.winCondition = matchDto.getWinCondition();
        this.players = matchDto.getPlayers();

        setupView();
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

        // Mock data
        List<MatchPlayerDto> matchPlayers = List.of(
                createMatchPlayer(0, 1, "SamplePlayer", "sample@osu.com", "JP", 4200, 98.76, 3000, 101, 500, true, PlayerRole.PLAYER, PlayerStatus.READY),
                createMatchPlayer(1, 2, "RhythmMaster", "rhythm@osu.com", "US", 5100, 99.12, 4800, 115, 300, false, PlayerRole.PLAYER, PlayerStatus.NOT_READY),
                createMatchPlayer(2, 3, "SpeedyBeat", "speedy@osu.com", "KR", 4600, 97.88, 3700, 107, 420, true, PlayerRole.HOST, PlayerStatus.READY),
                createMatchPlayer(3, 4, "AimQueen", "aimqueen@osu.com", "RU", 4300, 96.50, 3500, 99, 550, false, PlayerRole.PLAYER, PlayerStatus.NO_MAP),
                createMatchPlayer(4, 5, "TapGod", "tapgod@osu.com", "CN", 4950, 98.22, 3900, 105, 470, true, PlayerRole.PLAYER, PlayerStatus.READY),
                createMatchPlayer(5, 6, "SliderPro", "slider@osu.com", "DE", 4700, 96.40, 3400, 102, 360, false, PlayerRole.PLAYER, PlayerStatus.NOT_READY),
                createMatchPlayer(6, 7, "ClickWizard", "clickwizard@osu.com", "FR", 4600, 97.10, 3800, 108, 410, true, PlayerRole.PLAYER, PlayerStatus.READY),
                createMatchPlayer(7, 8, "BeatCrusher", "crusher@osu.com", "AU", 4400, 95.75, 3100, 100, 390, false, PlayerRole.PLAYER, PlayerStatus.READY),
                createMatchPlayer(8, 9, "NoScopeTapper", "noscope@osu.com", "CA", 5000, 98.90, 5000, 110, 280, true, PlayerRole.PLAYER, PlayerStatus.NOT_READY),
                createMatchPlayer(9, 10, "TimingKing", "timing@osu.com", "BR", 4650, 97.50, 3700, 106, 440, false, PlayerRole.PLAYER, PlayerStatus.READY),
                createMatchPlayer(10, 11, "Rhythmical", "rhythmical@osu.com", "SE", 4500, 96.85, 3600, 104, 330, true, PlayerRole.PLAYER, PlayerStatus.NO_MAP),
                createMatchPlayer(11, 12, "FlashBeat", "flash@osu.com", "IN", 4800, 97.60, 3900, 109, 410, false, PlayerRole.PLAYER, PlayerStatus.READY)
        );

        matchSlotPanel = new MatchSlotPanel(maxPlayerCount, matchPlayers);
        matchSlotPanel.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.43);
        matchSlotPanel.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.43);
        matchSlotPanel.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.43);

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

        HBox gameBox = new HBox(40);
        gameBox.getChildren().addAll(gameNameLabel, changePasswordButton);
        gameBox.setAlignment(Pos.CENTER_LEFT);

        gameNameTextField = new TextField(matchName);
        gameNameTextField.getStyleClass().add("game-name-text-field");
        VBox.setMargin(gameNameTextField, new Insets(16, 20, 0, 0));

        beatmapLabel = new Label("Beatmap");
        beatmapLabel.getStyleClass().add("beatmap-label");

        changeBeatmapButton = new Button("Change...");
        changeBeatmapButton.getStyleClass().add("change-beatmap-button");

        HBox beatmapBox = new HBox(36);
        beatmapBox.getChildren().addAll(beatmapLabel, changeBeatmapButton);
        beatmapBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(beatmapBox, new Insets(40, 0, 20, 0));

        winConditionLabel = new Label("Win Condition:");
        winConditionLabel.getStyleClass().add("win-condition-label");

        winConditionComboBox = new ComboBox<>();
        winConditionComboBox.getStyleClass().add("dark-combo-box");
        winConditionComboBox.getItems().addAll("score", "accuracy", "combo");
        winConditionComboBox.getSelectionModel().selectFirst();

        HBox winConditionBox = new HBox(20);
        winConditionBox.getChildren().addAll(winConditionLabel, winConditionComboBox);
        winConditionBox.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(winConditionBox, new Insets(20, 40, 0, 0));

        readyButton = new Button("Ready");
        readyButton.getStyleClass().add("ready-button");

        Beatmap beatmap = fetchBeatmapById(5103482);
        BeatmapCard card = BeatmapCard.available(beatmap);

        VBox rightContent = new VBox(gameBox, gameNameTextField, beatmapBox, card, winConditionBox);
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

        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(12, 36, 12, 36));
        
        double buttonWidth = (ScreenManager.SCREEN_WIDTH - 72 - 20) / 2;
        
        leaveMatchButton.setPrefWidth(buttonWidth);
        leaveMatchButton.setMaxWidth(buttonWidth);
        leaveMatchButton.setMinWidth(buttonWidth);
        
        readyButton.setPrefWidth(buttonWidth);
        readyButton.setMaxWidth(buttonWidth);
        readyButton.setMinWidth(buttonWidth);
        
        buttonContainer.getChildren().addAll(leaveMatchButton, readyButton);

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
    }

    @Override
    public void onShow() {
        scene.setRoot(root);

        banchoPanelsContainer.setVisible(true);
        banchoPanelsContainer.setManaged(true);
        banchoPanelsContainer.setMouseTransparent(false);
        chatPanel.setVisible(true);
    }

    public void handleEvent() {
        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
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

                System.out.println("Expected dir name: " + expectedDirName);

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
                        beatmapDto.getBeatmapSetDto().getBpm()
                );

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
                        beatmapSet
                );

            } else {
                System.err.println("Failed to fetch beatmaps: " + result.getError().getMessage());
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error fetching beatmap: " + e.getMessage());
            return null;
        }
    }

    private MatchPlayerDto createMatchPlayer(
            int slotIndex,
            int userId,
            String username,
            String email,
            String countryCode,
            int performance,
            double accuracy,
            int playCount,
            int level,
            int rank,
            boolean isSupporter,
            PlayerRole role,
            PlayerStatus status
    ) {
        UserDto user = new UserDto(
                userId,
                username,
                email,
                countryCode,
                null,
                performance,
                accuracy,
                playCount,
                level,
                rank,
                isSupporter
        );

        return new MatchPlayerDto(
                userId,
                matchId,
                user.getId(),
                user,
                role,
                status,
                slotIndex
        );
    }
}
