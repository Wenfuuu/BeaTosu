package beat.osu.client.view;

import beat.osu.client.Main;
import beat.osu.client.helper.InputManager;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public abstract class Page {

    protected Stage stage;
    protected Scene scene;
    protected InputManager inputManager;

    public abstract void init();
    public abstract void setLayout();

    private void setInputManager() {
        this.inputManager = new InputManager(scene);
    }

    private void showPage(){
        Image cursorImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/cursor.png")).toExternalForm());
        scene.setCursor(new ImageCursor(cursorImage, cursorImage.getWidth() / 2, cursorImage.getHeight() / 2));

        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/logo/osu_logo.png")).toExternalForm()));
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
        setInputManager();
        showPage();
    }
}
