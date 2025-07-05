package beat.osu.client.helper;

import beat.osu.client.Main;
import beat.osu.client.view.shared.common.Page;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

@Getter
public class SceneManager {
    private final Stage stage;
    private Scene scene;
    private boolean isFirstTransition = true;

    private static SceneManager instance;

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager(StageManager.getStage());
        }
        return instance;
    }

    private SceneManager(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(new StackPane(), ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
        setupStage();
    }

    private void setupStage() {
        stage.setTitle("BeaTOsu!");
        stage.setFullScreenExitHint("");
//        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
//        stage.setResizable(false);
        stage.setFullScreen(true);

        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/logo/osu_logo.png")).toExternalForm()));
    }

    public void transitionToPage(Page newPage) {
        if (isFirstTransition) {
            Scene newScene = newPage.getScene();
            stage.setScene(newScene);
            stage.show();

            this.scene = newScene;
            isFirstTransition = false;
        } else {
            Parent newRoot = newPage.getScene().getRoot();

            // Update stylesheets
//            Scene newScene = newPage.getScene();
//            scene.getStylesheets().clear();
//            scene.getStylesheets().addAll(newScene.getStylesheets());

            scene.setRoot(newRoot);
        }
        applyCursor(scene);
    }

    private void applyCursor(Scene scene) {
        try {
            Image cursorImage = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/cursor.png")).toExternalForm());
            scene.setCursor(new ImageCursor(cursorImage,
                    cursorImage.getWidth() / 2, cursorImage.getHeight() / 2));
        } catch (Exception e) {
            System.err.println("Failed to load cursor: " + e.getMessage());
        }
    }
}