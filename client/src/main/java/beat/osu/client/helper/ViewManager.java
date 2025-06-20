package beat.osu.client.helper;

import beat.osu.client.events.game.GameEndData;
import beat.osu.client.events.game.ReplayEventData;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.lobby.LobbyView;
import beat.osu.client.view.replay.ReplayView;
import beat.osu.client.view.upload.UploadPage;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.home.HomeView;
import beat.osu.client.view.landing.LandingView;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ViewManager {
    private static SceneManager sceneManager;
    private static Stage primaryStage;

    private static LandingView landingView;
    private static HomeView homeView;

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
                                      ArrayList<ReplayEventData> replayEvents,
                                      GameEndData gameEndData) {
        ReplayView replayView = new ReplayView(primaryStage, beatmap,
                replayEvents, gameEndData);
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
}
