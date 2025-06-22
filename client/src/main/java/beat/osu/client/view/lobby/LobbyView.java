package beat.osu.client.view.lobby;

import java.net.URL;

import beat.osu.client.controller.ChatController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.view.lobby.component.layout.TopBar;
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
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LobbyView extends Page {

    private StackPane root;

    private final ConnectedUsersController connectedUsersController;
    private final ChatController chatController;

    private PlaylistModal playlistModal;
    private Jukebox jukebox;

    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private SelectChannelModal selectChannelModal;
    private ViewUserModal viewUserModal;
    private BanchoButtons banchoButtons;

    private Button backbutton;

    private TopBar topBar;

    public LobbyView(Stage stage, ConnectedUsersController connectedUsersController, ChatController chatController) {
        super(stage);

        this.connectedUsersController = connectedUsersController;
        this.chatController = chatController;

        setupView();
        handleEvent();
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

        playlistModal = new PlaylistModal();
        PlaylistManager.getInstance().addListener(playlistModal);

        jukebox = new Jukebox(playlistModal);
        PlaylistManager.getInstance().addListener(jukebox);

        banchoButtons = new BanchoButtons();

        onlineUsersPanel = new OnlineUsersPanel(connectedUsersController);
        selectChannelModal = new SelectChannelModal(chatController.getChannelController(), banchoButtons);
        chatPanel = new ChatPanel(chatController, selectChannelModal, onlineUsersPanel, banchoButtons);
        chatPanel.show();

        viewUserModal = new ViewUserModal();

        viewUserModal.setOnStartChatCallback(privateChat -> {
            if (AuthManager.getUser().getId() == privateChat.getOtherUserId()) {
                Toast.error("You cannot start a chat with yourself!").show();
                return;
            }

            chatPanel.startPrivateChat(privateChat.getOtherUserId(), privateChat.getOtherUserName());
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

        playlistModal.setVisible(false);
        
        backbutton = new Button("Back");

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
        root.getChildren().add(topBar);
        StackPane.setAlignment(topBar, Pos.TOP_CENTER);

        root.getChildren().addAll(playlistModal, selectChannelModal);

        StackPane.setAlignment(playlistModal, Pos.CENTER);
        StackPane.setAlignment(selectChannelModal, Pos.CENTER);

        root.getChildren().add(jukebox);
        StackPane.setAlignment(jukebox, Pos.TOP_RIGHT);

        root.getChildren().add(onlineUsersPanel);
        StackPane.setAlignment(onlineUsersPanel, Pos.TOP_CENTER);
        onlineUsersPanel.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(chatPanel);
        StackPane.setAlignment(chatPanel, Pos.TOP_CENTER);
        chatPanel.setMaxWidth(Double.MAX_VALUE);
        StackPane.setMargin(chatPanel, new Insets(ScreenManager.SCREEN_HEIGHT * 0.65, 0, 0, 0));

        root.getChildren().add(banchoButtons);
        StackPane.setAlignment(banchoButtons, Pos.BOTTOM_RIGHT);

        root.getChildren().add(viewUserModal);
        StackPane.setAlignment(viewUserModal, Pos.CENTER);

        root.getChildren().add(backbutton);
        StackPane.setAlignment(backbutton, Pos.BOTTOM_LEFT);
    }

    @Override
    public void onShow() {
        scene.setRoot(root);
        setInputManager();
        playlistModal.setInputManager(inputManager);

        chatController.getChannelController().joinChannel(3).thenAccept(result -> {
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    chatPanel.getChatTabs().selectTab(result.getValue().getChannel());
                    Toast.success("Successfully joined lobby!").show();
                }
            });
        });
    }

    public void handleEvent() {
        playlistModal.setInputManager(inputManager);

        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            if (banchoButtons.getOnlineUsersButton().isOnlineUserShown()) {
                onlineUsersPanel.hide();
                banchoButtons.getOnlineUsersButton().setOnlineUsersHiddenIcon();
            } else {
                onlineUsersPanel.show();
                banchoButtons.getOnlineUsersButton().setOnlineUsersShownIcon();
            }
        });

        backbutton.setOnMouseClicked(e -> {
            ViewManager.getInstance().showLandingView();
        });

        jukebox.getMediaControls().getPlaylistButton().setOnAction(event -> {
            if (playlistModal.isVisible()) {
                playlistModal.hide();
                chatPanel.show();
                if (!banchoButtons.isVisible()) {
                    banchoButtons.show();
                }
            } else {
                chatPanel.hide();
                if (banchoButtons.isVisible()) {
                    banchoButtons.hide();
                }
                playlistModal.show();
            }
        });
    }
}
