package beat.osu.beatosu;

import beat.osu.beatosu.helper.StageManager;
import beat.osu.beatosu.view.home.HomeView;
import beat.osu.beatosu.view.landing.LandingView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage)  {
        StageManager.setStage(stage);

//        new LandingView(StageManager.getStage());
        new HomeView(StageManager.getStage());
    }

    public static void main(String[] args) {
        launch();
    }
}