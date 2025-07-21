package beat.osu.client.view.shared.common;

import java.net.URL;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.enums.ToastType;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.CursorManager;
import beat.osu.client.helper.SceneManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.StageManager;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {
    private Stage toastStage;
    private HBox root;
    private VBox box;
    private final int slideInDelay = 500;
    private final int toastDelay = 2000;
    private final int slideOutDelay = 500;

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
        Text messageText = new Text(message);
        messageText.setFont(Font.font("Aller", 18));
        messageText.setFill(Color.WHITE);
        messageText.setWrappingWidth(MAX_TOAST_WIDTH - 80);
        messageText.setTextAlignment(TextAlignment.LEFT);

        HBox contentBox = new HBox(12);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.getChildren().addAll(messageText);

        this.box = new VBox(contentBox);
        box.setMaxWidth(MAX_TOAST_WIDTH);
        box.setAlignment(Pos.CENTER_LEFT);
        String borderColor = type.getBorderColor();

        box.setStyle(String.format(
                "-fx-background-radius: 12px; " +
                        "-fx-background-color: rgba(0, 0, 0, 0.8); " +
                        "-fx-padding: 10px 6px; " +
                        "-fx-min-width: 250px; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-border-width: 2px; " +
                        "-fx-max-width: %spx; ",
                borderColor, MAX_TOAST_WIDTH));

        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: transparent;");

        root.getChildren().removeAll();
        root.getChildren().add(box);
    }

    private void initialize() {
        toastStage = new Stage();
        toastStage.initOwner(StageManager.getStage());
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        toastStage.setY(ScreenManager.SCREEN_HEIGHT - 125);

        this.root = new HBox();

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            SceneManager.getInstance().getScene().getStylesheets().add(globalCssUrl.toExternalForm());
        }
    }

    public void show() {
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        toastStage.show();
        CursorManager.applyCursor(scene);
        animateToast();
    }

    private void animateToast() {
        double finalX = ScreenManager.SCREEN_WIDTH - MAX_TOAST_WIDTH - 20;
        toastStage.setX(finalX);

        double slideDistance = MAX_TOAST_WIDTH + 50;
        root.setTranslateX(slideDistance);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(slideInDelay), root);
        slideIn.setFromX(slideDistance);
        slideIn.setToX(0);

        PauseTransition pause = new PauseTransition(Duration.millis(toastDelay));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(slideOutDelay), root);
        slideOut.setFromX(0);
        slideOut.setToX(slideDistance);
        slideOut.setOnFinished(e -> toastStage.close());

        SequentialTransition sequence = new SequentialTransition(slideIn, pause, slideOut);
        sequence.play();
    }
}
