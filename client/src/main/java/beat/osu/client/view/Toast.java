package beat.osu.client.view;

import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.StageManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {
    private Stage toastStage;
    private HBox root;
    private VBox box;
    private final int fadeInDelay = 500;
    private final int toastDelay = 2000;
    private final int fadeOutDelay = 500;

    private final double MAX_TOAST_WIDTH = 400;

    public Toast(String message) {
        initialize();
        setText(message);
    }

    private void setText(String message) {
        Text text = new Text(message);
        text.setFont(Font.font("Nunito", 18));
        text.setFill(Color.BLACK);
        text.setWrappingWidth(MAX_TOAST_WIDTH - 60);

        this.box = new VBox(text);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-radius: 5px; -fx-background-color: rgba(255, 255, 255, 1); " +
                "-fx-padding: 10px 30px; -fx-min-width: 210px; " +
                "-fx-effect: dropshadow(gaussian, grey, 15.0, 0.5, 0, 0);");

        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: transparent;");
        root.setOpacity(0);

        root.getChildren().removeAll();
        root.getChildren().add(box);
    }

    private void initialize() {
        toastStage = new Stage();
        toastStage.initOwner(StageManager.getStage());
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        toastStage.setX(ScreenManager.SCREEN_WIDTH - MAX_TOAST_WIDTH - 50);
        toastStage.setY(0);

        this.root = new HBox();
    }

    public void show() {
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        toastStage.show();
        animateToast();
    }

    private void animateToast() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(fadeInDelay), toastStage.getScene().getRoot());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(toastDelay));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(fadeOutDelay), toastStage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> toastStage.close());

        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();
    }
}
