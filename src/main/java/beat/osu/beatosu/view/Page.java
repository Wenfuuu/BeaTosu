package beat.osu.beatosu.view;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public abstract class Page {

    protected Stage stage;
    protected Scene scene;

    public abstract void init();
    public abstract void setLayout();

    public void showPage(){
        stage.setScene(scene);
//        stage.getIcons().add(new Image(Objects.requireNonNull(getClass()
//                .getResource("/assets/logo/osu_logo.png")).toExternalForm()));
        stage.setTitle("BeaTOsu!");
        stage.setFullScreenExitHint("");
//        stage.setResizable(false);
        stage.setFullScreen(true);
        stage.show();
    }

    public Page(Stage stage) {
        this.stage = stage;
        init();
        setLayout();
        showPage();
    }
}
