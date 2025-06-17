package beat.osu.client.view.game.component;

import beat.osu.client.factory.ButtonFactory;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class FailOverlay extends StackPane {
    private final Label pauseLabel;
    private final Button retryButton;
    private final Button leaveButton;

    public void showFailOverlay() {
        this.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), this);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    public FailOverlay() {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        this.setVisible(false);

        pauseLabel = new Label("FAILED");
        pauseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setEffect(new DropShadow(10, Color.BLACK));

        retryButton = ButtonFactory.createRetryButton();
        leaveButton = ButtonFactory.createLeaveButton();

        VBox pauseContent = new VBox(20);
        pauseContent.getChildren().addAll(pauseLabel, retryButton, leaveButton);
        pauseContent.setAlignment(Pos.CENTER);

        this.getChildren().add(pauseContent);
    }
}
