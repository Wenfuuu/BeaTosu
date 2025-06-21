package beat.osu.client.helper;

import java.util.ArrayList;

import beat.osu.client.events.game.GameEndEvent;
import beat.osu.client.events.game.ReplayEvent;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.home.HomeView;
import beat.osu.client.view.landing.LandingView;
import beat.osu.client.view.lobby.LobbyView;
import beat.osu.client.view.replay.ReplayView;
import beat.osu.client.view.upload.UploadPage;
import javafx.stage.Stage;

public class ViewManager {
    private static SceneManager sceneManager;
    private static Stage primaryStage;

    private LandingView landingView;
    private HomeView homeView;
    private LobbyView lobbyView;

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

        initializeHomeView();
    }

    public void initializeHomeView() {
        if (homeView == null) {
            homeView = new HomeView(primaryStage);
        }
    }

    public void showLandingView() {
        if (landingView == null) {
            landingView = new LandingView(primaryStage);
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
        if (lobbyView == null) {
            lobbyView = new LobbyView(primaryStage);
        } else {
            lobbyView.onShow();
        }
        sceneManager.transitionToPage(lobbyView);
    }
}