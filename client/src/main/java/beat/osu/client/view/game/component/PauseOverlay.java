package beat.osu.client.view.game.component;

import beat.osu.client.factory.ButtonFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;

@Getter
public class PauseOverlay extends StackPane {
    private final Label pauseLabel;
    private final Button continueButton;
    private final Button retryButton;
    private final Button leaveButton;

    public PauseOverlay() {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        this.setVisible(false);

        pauseLabel = new Label("PAUSED");
        pauseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setEffect(new DropShadow(10, Color.BLACK));

        continueButton = ButtonFactory.createContinueButton();
        retryButton = ButtonFactory.createRetryButton();
        leaveButton = ButtonFactory.createLeaveButton();

        VBox pauseContent = new VBox(20);
        pauseContent.getChildren().addAll(pauseLabel, continueButton, retryButton, leaveButton);
        pauseContent.setAlignment(Pos.CENTER);

        this.getChildren().add(pauseContent);
    }
}
