package beat.osu.client.view;

import beat.osu.client.Main;
import beat.osu.client.helper.InputManager;
import beat.osu.client.helper.SceneManager;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

public abstract class Page {

    protected Stage stage;
    @Getter
    protected Scene scene;
    protected InputManager inputManager;

    public abstract void init();
    public abstract void setLayout();

    private void setInputManager() {
        this.inputManager = new InputManager(scene);
    }

    public Page(Stage stage) {
        this.stage = stage;
        scene = SceneManager.instance.getScene();
        init();
        setLayout();
        setInputManager();
    }
}
