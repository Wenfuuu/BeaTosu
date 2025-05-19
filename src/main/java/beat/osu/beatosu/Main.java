package beat.osu.beatosu;

import beat.osu.beatosu.model.User;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage)  {
//        User user = new User("liman", 19);

        stage.setTitle("Hello!");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}