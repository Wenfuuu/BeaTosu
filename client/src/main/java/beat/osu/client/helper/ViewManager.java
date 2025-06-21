package beat.osu.client.helper;

import java.util.ArrayList;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.controller.PrivateChatController;
import beat.osu.client.events.game.GameEndEvent;
import beat.osu.client.events.game.ReplayEvent;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.home.HomeView;
import beat.osu.client.view.landing.LandingView;
import beat.osu.client.view.lobby.LobbyView;
import beat.osu.client.view.replay.ReplayView;
import beat.osu.client.view.shared.bancho.buttons.BanchoButtons;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.client.view.shared.bancho.modals.SelectChannelModal;
import beat.osu.client.view.shared.bancho.modals.ViewUserModal;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.shared.jukebox.Jukebox;
import beat.osu.client.view.shared.jukebox.modals.PlaylistModal;
import beat.osu.client.view.upload.UploadPage;
import javafx.stage.Stage;

public class ViewManager {
    private static SceneManager sceneManager;
    private static Stage primaryStage;

    private LandingView landingView;
    private HomeView homeView;

    // Controllers
    private ConnectedUsersController connectedUsersController;
    private ChannelController channelController;
    private PrivateChatController privateChatController;

    // Global Components
    private BanchoButtons banchoButtons;
    private Jukebox jukebox;
    private PlaylistModal playlistModal;
    private OnlineUsersPanel onlineUsersPanel;
    private SelectChannelModal selectChannelModal;
    private ChatPanel chatPanel;
    private ViewUserModal viewUserModal;

    private static ViewManager instance;

    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    private ViewManager() {
        primaryStage = StageManager.getStage();
        sceneManager = SceneManager.getInstance();

        initializeControllers();
        initializeGlobalComponents();
    }

    public void initializeHomeView() {
        if (homeView == null) {
            homeView = new HomeView(primaryStage);
        }
    }

    public void showLandingView() {
        if (landingView == null) {
            landingView = new LandingView(
                    primaryStage, banchoButtons, jukebox, playlistModal, onlineUsersPanel, selectChannelModal, chatPanel, viewUserModal
            );
        } else {
            landingView.onShow();
        }
        sceneManager.transitionToPage(landingView);
    }

    public void showHomeView() {
        if (homeView == null) {
            homeView = new HomeView(primaryStage);
        } else {
            homeView.onShow();
        }
        sceneManager.transitionToPage(homeView);
    }

    public void showGameView(Beatmap beatmap) {
        GameView gameView = new GameView(primaryStage, beatmap);
        sceneManager.transitionToPage(gameView);
    }

    public void showReplayView(Beatmap beatmap,
                                      ArrayList<ReplayEvent> replayEvents,
                                      GameEndEvent gameEndEvent) {
        ReplayView replayView = new ReplayView(primaryStage, beatmap,
                replayEvents, gameEndEvent);
        sceneManager.transitionToPage(replayView);
    }

    public void showUploadPage() {
        UploadPage uploadPage = new UploadPage(primaryStage);
        sceneManager.transitionToPage(uploadPage);
    }

    public void showLobbyView() {
        LobbyView lobbyView = new LobbyView(primaryStage);
        sceneManager.transitionToPage(lobbyView);
    }

    private void initializeControllers() {
        connectedUsersController = new ConnectedUsersController();
        channelController = new ChannelController();
        privateChatController = new PrivateChatController();
    }

    private void initializeGlobalComponents() {
        banchoButtons = new BanchoButtons();

        playlistModal = new PlaylistModal();
        PlaylistManager.getInstance().addListener(playlistModal);

        jukebox = new Jukebox(playlistModal);
        PlaylistManager.getInstance().addListener(jukebox);

        onlineUsersPanel = new OnlineUsersPanel(connectedUsersController);
        selectChannelModal = new SelectChannelModal(channelController, banchoButtons);
        chatPanel = new ChatPanel(channelController, privateChatController, selectChannelModal, onlineUsersPanel, banchoButtons);
        viewUserModal = new ViewUserModal();

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
    }
}
