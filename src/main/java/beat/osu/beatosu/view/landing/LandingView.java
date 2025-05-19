package beat.osu.beatosu.view.landing;

import beat.osu.beatosu.helper.ScreenManager;
import beat.osu.beatosu.view.Page;
import beat.osu.beatosu.view.landing.component.RegisterModal;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LandingView extends Page {



    public LandingView(Stage stage) {
        super(stage);

    }

    @Override
    public void init() {
        scene = new Scene(new RegisterModal(), ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
    }

    @Override
    public void setLayout() {

    }
}
