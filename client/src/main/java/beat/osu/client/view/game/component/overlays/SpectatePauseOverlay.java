package beat.osu.client.view.game.component.overlays;

import beat.osu.client.helper.ScreenManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Getter;

public class SpectatePauseOverlay extends StackPane {
    @Getter
    private final Label pauseLabel;
    private final Label escapeLabel;

    public SpectatePauseOverlay() {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        this.setVisible(false);

        pauseLabel = new Label("Host Paused");
        pauseLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT / 15));
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setEffect(new DropShadow(10, Color.BLACK));

        escapeLabel = new Label("Press ESC to exit");
        escapeLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT / 30));
        escapeLabel.setTextFill(Color.WHITE);

        VBox pauseContent = new VBox(20);
        pauseContent.getChildren().addAll(pauseLabel, escapeLabel);
        pauseContent.setAlignment(Pos.CENTER);

        this.getChildren().add(pauseContent);
    }
}
