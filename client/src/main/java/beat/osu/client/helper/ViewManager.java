package beat.osu.client.helper;

import java.util.ArrayList;

import beat.osu.client.controller.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.game.SpectateView;
import beat.osu.client.view.home.HomeView;
import beat.osu.client.view.landing.LandingView;
import beat.osu.client.view.lobby.LobbyView;
import beat.osu.client.view.game.ReplayView;
import beat.osu.client.view.match.MatchView;
import beat.osu.client.view.upload.UploadPage;
import beat.osu.client.events.game.ReplayEvent;
import beat.osu.shared.dto.game.SpectateDto;
import beat.osu.shared.dto.match.MatchDto;
import javafx.stage.Stage;

public class ViewManager {
    private static SceneManager sceneManager;
    private static Stage primaryStage;

    private LandingView landingView;
    private HomeView homeView;
    private LobbyView lobbyView;

    private BeatmapController beatmapController;
    private ConnectedUsersController connectedUsersController;
    private ChannelController channelController;
    private PrivateChatController privateChatController;
    private ChatController chatController;
    private SessionController sessionController;
    private SpectateController spectateController;
    private MatchController matchController;

    private static volatile ViewManager instance;

    public static ViewManager getInstance() {
        if (instance == null) {
            synchronized (ViewManager.class) {
                if (instance == null) {
                    instance = new ViewManager();
                }
            }
        }
        return instance;
    }

    private ViewManager() {
        primaryStage = StageManager.getStage();
        sceneManager = SceneManager.getInstance();

        initializeControllers();
        initializeViews();
    }

    public void initializeViews() {
        landingView = new LandingView(primaryStage, connectedUsersController, chatController, sessionController, spectateController);
        homeView = new HomeView(primaryStage);
        lobbyView = new LobbyView(primaryStage, connectedUsersController, chatController, sessionController, spectateController, matchController);
    }

    public void showLandingView() {
        landingView.onShow();
        sceneManager.transitionToPage(landingView);
    }

    public void showHomeView() {
        homeView.onShow();
        sceneManager.transitionToPage(homeView);
    }

    public void showLobbyView() {
        lobbyView.onShow();
        sceneManager.transitionToPage(lobbyView);
    }

    public void showMatchView(MatchDto matchDto) {
        MatchView matchView = new MatchView(primaryStage, matchDto, connectedUsersController, chatController, matchController, sessionController, beatmapController);
        matchView.onShow();
        sceneManager.transitionToPage(matchView);
    }

    public void showGameView(Beatmap beatmap, boolean isMultiplayer) {
        GameView gameView = new GameView(primaryStage, beatmap, isMultiplayer);
        sceneManager.transitionToPage(gameView);
    }

    public void showReplayView(Beatmap beatmap, ArrayList<ReplayEvent> replayEvents) {
        ReplayView replayView = new ReplayView(primaryStage, beatmap, replayEvents);
        sceneManager.transitionToPage(replayView);
    }

    public void showSpectateView(Beatmap beatmap, SpectateDto spectateDto) {
        SpectateView spectateView = new SpectateView(primaryStage, beatmap, spectateDto, spectateController);
        sceneManager.transitionToPage(spectateView);
    }

    public void showUploadPage() {
        UploadPage uploadPage = new UploadPage(primaryStage);
        sceneManager.transitionToPage(uploadPage);
    }

    private void initializeControllers() {
        beatmapController = new BeatmapController();
        connectedUsersController = new ConnectedUsersController();
        channelController = new ChannelController();
        privateChatController = new PrivateChatController();
        chatController = new ChatController(channelController, privateChatController);
        sessionController = new SessionController();
        spectateController = new SpectateController();
        matchController = new MatchController();
    }
}