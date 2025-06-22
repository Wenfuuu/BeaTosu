package beat.osu.client.view.lobby;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.controller.ChatController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.controller.PrivateChatController;
import beat.osu.client.helper.*;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class LobbyView extends Page {

    private StackPane root;

    private ConnectedUsersController connectedUsersController;
    private ChannelController channelController;
    private PrivateChatController privateChatController;
    private ChatController chatController;

    private PlaylistModal playlistModalComponent;
    private Jukebox jukeboxComponent;
    private OnlineUsersPanel onlineUsersPanel;
    private ChatPanel chatPanel;
    private SelectChannelModal selectChannelModal;
    private ViewUserModal viewUserModal;
    private BanchoButtons banchoButtons;

    public LobbyView(Stage stage, ConnectedUsersController connectedUsersController, ChannelController channelController,
                     PrivateChatController privateChatController, ChatController chatController) {
        super(stage);

        this.connectedUsersController = connectedUsersController;
        this.channelController = channelController;
        this.privateChatController = privateChatController;
        this.chatController = chatController;

        setupView();
        handleEvent();
    }

    @Override
    public void init() {
        root = new StackPane();
        root.getStyleClass().add("root");

        playlistModalComponent = new PlaylistModal();
        PlaylistManager.getInstance().addListener(playlistModalComponent);

        jukeboxComponent = new Jukebox(playlistModalComponent);
        PlaylistManager.getInstance().addListener(jukeboxComponent);

        banchoButtons = new BanchoButtons();

        onlineUsersPanel = new OnlineUsersPanel(connectedUsersController);
        selectChannelModal = new SelectChannelModal(channelController, banchoButtons);
        chatPanel = new ChatPanel(chatController, selectChannelModal, onlineUsersPanel, banchoButtons);

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

        playlistModalComponent.setVisible(false);

        scene.setRoot(root);

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
        root.getChildren().addAll(playlistModalComponent, selectChannelModal);

        StackPane.setAlignment(playlistModalComponent, Pos.CENTER);
        StackPane.setAlignment(selectChannelModal, Pos.CENTER);

        root.getChildren().add(jukeboxComponent);
        StackPane.setAlignment(jukeboxComponent, Pos.TOP_RIGHT);

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
    }

    @Override
    public void onShow() {
        scene.setRoot(root);
    }

    public void handleEvent() {
        playlistModalComponent.setInputManager(inputManager);

        banchoButtons.getOnlineUsersButton().setOnMouseClicked(e -> {
            if (banchoButtons.getOnlineUsersButton().isOnlineUserShown()) {
                onlineUsersPanel.hide();
                banchoButtons.getOnlineUsersButton().setOnlineUsersHiddenIcon();
            } else {
                onlineUsersPanel.show();
                banchoButtons.getOnlineUsersButton().setOnlineUsersShownIcon();

                if (!chatPanel.isShowing()) {
                    chatPanel.show();
                    banchoButtons.getChatToggleButton().setHideIcon();
                }
            }
        });

        banchoButtons.getChatToggleButton().setOnMouseClicked(e -> {
            if (banchoButtons.getChatToggleButton().isChatVisible()) {
                chatPanel.hide();
                banchoButtons.getChatToggleButton().setShowIcon();

                if (onlineUsersPanel.isShowing()) {
                    onlineUsersPanel.hide();
                    banchoButtons.getOnlineUsersButton().setOnlineUsersHiddenIcon();
                }
            } else {
                chatPanel.show();
                banchoButtons.getChatToggleButton().setHideIcon();
            }
        });
    }
}
