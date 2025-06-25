package beat.osu.client.view.match;

import beat.osu.client.controller.ChatController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.controller.MatchController;
import beat.osu.client.controller.SessionController;
import beat.osu.client.helper.*;
import beat.osu.client.view.match.component.layout.TopBar;
import beat.osu.client.view.shared.bancho.buttons.BanchoButtons;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.client.view.shared.bancho.modals.SelectChannelModal;
import beat.osu.client.view.shared.bancho.modals.ViewUserModal;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;

public class MatchView extends Page {

    private StackPane root;

    private Integer matchId;
    private String matchName;
    private String matchPassword;
    private String status;
    private int maxPlayerCount;

    private int beatmapId;
    private String beatmapName;

    private String winCondition;

    private List<MatchPlayerDto> players;


    private final ConnectedUsersController connectedUsersController;
    private final ChatController chatController;
    private final MatchController matchController;
    private final SessionController sessionController;


    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private SelectChannelModal selectChannelModal;
    private ViewUserModal viewUserModal;
    private BanchoButtons banchoButtons;

    private TopBar topBar;
    private VBox mainContent;
    private VBox banchoPanelsContainer;

    public MatchView(Stage stage, MatchDto matchDto, ConnectedUsersController connectedUsersController,
                     ChatController chatController,MatchController matchController, SessionController sessionController) {
        super(stage);

        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;
        this.matchController = matchController;
        this.sessionController = sessionController;

        this.matchId = matchDto.getId();
        this.matchName = matchDto.getName();
        this.matchPassword = matchDto.getPassword();
        this.status = matchDto.getStatus();
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

        mainContent = new VBox();
        mainContent.getChildren().addAll(topBar);
        VBox.setMargin(topBar, new Insets(0, 0, 0, 0));

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
}
