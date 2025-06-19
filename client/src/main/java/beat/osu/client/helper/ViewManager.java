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
    private static HomeView homeView;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        sceneManager = SceneManager.getInstance(stage);
    }

    public static void initializeHomeView() {
        if (homeView == null) {
            homeView = new HomeView(primaryStage);
        }
    }

    public static void showLandingView() {
        LandingView landingView = new LandingView(primaryStage);
        sceneManager.transitionToPage(landingView);
    }

    public static void showHomeView() {
        if (homeView == null) {
            homeView = new HomeView(primaryStage);
        }else{
            homeView.onShow();
        }
        sceneManager.transitionToPage(homeView);
    }

    public static void showGameView(Beatmap beatmap) {
        GameView gameView = new GameView(primaryStage, beatmap);
        sceneManager.transitionToPage(gameView);
    }

    public static void showReplayView(Beatmap beatmap,
                                      ArrayList<ReplayEventData> replayEvents,
                                      GameEndData gameEndData) {
        ReplayView replayView = new ReplayView(primaryStage, beatmap,
                replayEvents, gameEndData);
        sceneManager.transitionToPage(replayView);
    }

    public static void showUploadPage() {
        UploadPage uploadPage = new UploadPage(primaryStage);
        sceneManager.transitionToPage(uploadPage);
    }

    public static void showLobbyView() {
        LobbyView lobbyView = new LobbyView(primaryStage);
        sceneManager.transitionToPage(lobbyView);
    }
}
