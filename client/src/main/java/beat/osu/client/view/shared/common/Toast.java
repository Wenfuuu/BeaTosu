package beat.osu.client.view.shared.common;

import beat.osu.client.enums.ToastType;
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
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    private final ToastType type;

    public static Toast success(String message) {
        return new Toast(message, ToastType.SUCCESS);
    }

    public static Toast information(String message) {
        return new Toast(message, ToastType.INFORMATION);
    }

    public static Toast error(String message) {
        return new Toast(message, ToastType.ERROR);
    }

    private Toast(String message, ToastType type) {
        this.type = type;
        initialize();
        setText(message);
    }

    private void setText(String message) {
        Text iconText = new Text(type.getIcon());
        iconText.setFont(Font.font("Nunito", FontWeight.BOLD, 20));
        iconText.setFill(Color.web(type.getTextColor()));

        Text messageText = new Text(message);
        messageText.setFont(Font.font("Nunito", 18));
        messageText.setFill(Color.WHITE);
        messageText.setWrappingWidth(MAX_TOAST_WIDTH - 80);
        messageText.setTextAlignment(TextAlignment.LEFT);

        HBox contentBox = new HBox(12);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.getChildren().addAll(iconText, messageText);

        this.box = new VBox(contentBox);
        box.setMaxWidth(MAX_TOAST_WIDTH);
        box.setAlignment(Pos.CENTER_LEFT);
        String backgroundColor = type.getBackgroundColor();
        String shadowColor = getShadowColor(type);

        box.setStyle(String.format(
                "-fx-background-radius: 8px; " +
                        "-fx-background-color: %s; " +
                        "-fx-padding: 16px 20px; " +
                        "-fx-min-width: 250px; " +
                        "-fx-max-width: %spx; " +
                        "-fx-effect: dropshadow(gaussian, %s, 10.0, 0.3, 0, 2);",
                backgroundColor, MAX_TOAST_WIDTH, shadowColor
        ));

        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: transparent;");
        root.setOpacity(0);

        root.getChildren().removeAll();
        root.getChildren().add(box);
    }

    private String getShadowColor(ToastType type) {
        switch (type) {
            case SUCCESS: return "rgba(76, 175, 80, 0.3)";
            case INFORMATION: return "rgba(33, 150, 243, 0.3)";
            case ERROR: return "rgba(244, 67, 54, 0.3)";
            default: return "rgba(0, 0, 0, 0.3)";
        }
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
