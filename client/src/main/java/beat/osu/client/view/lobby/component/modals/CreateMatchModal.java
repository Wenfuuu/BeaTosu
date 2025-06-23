package beat.osu.client.view.lobby.component.modals;

import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.model.Song;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;

public class CreateMatchModal extends VBox {

    private Label titleLabel;

    private Label gameLabel;
    private TextField gameTextField;

    public CreateMatchModal() {
        initializeComponents();
        setLayout();
        loadStyles();

        this.setVisible(false);
    }

    private void initializeComponents() {
        this.getStyleClass().add("root");

        titleLabel = new Label("Create New Game...");
        titleLabel.getStyleClass().add("title-label");

        gameLabel = new Label("Game Name:");
        gameLabel.getStyleClass().add("game-label");

        gameTextField = new TextField();
        gameTextField.getStyleClass().add("game-input");
    }

    private void setLayout() {
        this.getChildren().add(titleLabel);

        HBox gameNameBox = new HBox();
        gameNameBox.setAlignment(Pos.CENTER_LEFT);
        gameNameBox.getStyleClass().add("game-name-box");
        gameNameBox.getChildren().addAll(gameLabel, gameTextField);
        VBox.setMargin(gameNameBox, new Insets(156, 0, 0, 0));

        this.getChildren().add(gameNameBox);
    }

    private void loadStyles() {
        try {
            URL cssUrl = CssManager.getLobbyCssURL("CreateMatchModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load CreateMatchModal CSS: " + e.getMessage());
        }
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
        });

        fadeOut.play();
    }

    public void show() {
        String username = AuthManager.getUser().getUsername();
        gameTextField.setText(username + "'s game");

        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}
