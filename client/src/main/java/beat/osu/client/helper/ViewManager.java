package beat.osu.client.helper;

import beat.osu.client.model.Beatmap;
import beat.osu.client.view.UploadPage;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.home.HomeView;
import beat.osu.client.view.landing.LandingView;
import javafx.stage.Stage;

public class ViewManager {
    private static SceneManager sceneManager;
    private static Stage primaryStage;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        sceneManager = SceneManager.getInstance(stage);
    }

    public static void showLandingView() {
        LandingView landingView = new LandingView(primaryStage);
        sceneManager.transitionToPage(landingView);
    }

    public static void showHomeView() {
        HomeView homeView = new HomeView(primaryStage);
        sceneManager.transitionToPage(homeView);
    }

    public static void showGameView(Beatmap beatmap) {
         GameView gameView = new GameView(primaryStage, beatmap);
         sceneManager.transitionToPage(gameView);
    }

    public static void showUploadPage() {
        UploadPage uploadPage = new UploadPage(primaryStage);
        sceneManager.transitionToPage(uploadPage);
    }
}
