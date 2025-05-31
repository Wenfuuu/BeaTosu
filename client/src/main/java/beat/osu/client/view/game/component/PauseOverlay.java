package beat.osu.client.view.game.component;

import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.SfxManager;
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
    private final Button backButton;

    private Button createPauseButton() {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");
            SfxManager.playSfx("pause-hover.wav");
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: transparent;");
        });

        return button;
    }

    public PauseOverlay() {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        this.setVisible(false);

        pauseLabel = new Label("PAUSED");
        pauseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setEffect(new DropShadow(10, Color.BLACK));

        continueButton = createPauseButton();
        BackgroundManager.setPauseButtonBackground(continueButton, "pause-continue.png");

        retryButton = createPauseButton();
        BackgroundManager.setPauseButtonBackground(retryButton, "pause-retry.png");

        backButton = createPauseButton();
        BackgroundManager.setPauseButtonBackground(backButton, "pause-back.png");

        VBox pauseContent = new VBox(20);
        pauseContent.getChildren().addAll(pauseLabel, continueButton, retryButton, backButton);
        pauseContent.setAlignment(Pos.CENTER);

        this.getChildren().add(pauseContent);
    }
}
