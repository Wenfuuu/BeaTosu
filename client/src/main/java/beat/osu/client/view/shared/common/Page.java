package beat.osu.client.view.shared.common;

import beat.osu.client.helper.InputManager;
import beat.osu.client.helper.SceneManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

public abstract class Page {

    protected Stage stage;
    @Getter
    protected Scene scene;
    protected InputManager inputManager;

    public abstract void init();
    public abstract void setLayout();
    public abstract void onShow();

    private void setInputManager() {
        this.inputManager = new InputManager(scene);
    }

    public Page(Stage stage) {
        this.stage = stage;
        scene = SceneManager.getInstance().getScene();
    }

    protected void setupView() {
        init();
        setLayout();
        setInputManager();
    }
}
