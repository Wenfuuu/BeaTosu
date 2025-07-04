package beat.osu.client.view.match.component.modals;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;

public class HostActionsModal extends VBox {

    private Label titleLabel;

    @Getter
    private Button transferHostButton;
    @Getter
    private Button kickPlayerButton;
    @Getter
    private Button userOptionsButton;
    @Getter
    private Button cancelButton;

    private VBox buttonsContainer;

    public HostActionsModal() {
        initializeComponents();
        setLayout();
        loadStyles();
        handleEvent();

        this.setVisible(false);
    }

    private void initializeComponents() {
        this.getStyleClass().add("root");

        titleLabel = new Label("What would you like to do with this user?");
        titleLabel.getStyleClass().add("title-label");

        transferHostButton = new Button("1. Transfer Host Privileges");
        kickPlayerButton = new Button("2. Kick the user");
        userOptionsButton = new Button("3. User Options");
        cancelButton = new Button("4. Cancel");

        transferHostButton.setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        kickPlayerButton.setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        userOptionsButton.setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        cancelButton.setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));

        transferHostButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        kickPlayerButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        userOptionsButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        cancelButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);

        transferHostButton.getStyleClass().addAll("main-button", "transfer-host-button");
        kickPlayerButton.getStyleClass().addAll("main-button", "kick-player-button");
        userOptionsButton.getStyleClass().addAll("main-button", "user-options-button");
        cancelButton.getStyleClass().addAll("main-button", "cancel-button");

        buttonsContainer = new VBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.getChildren().addAll(transferHostButton, kickPlayerButton, userOptionsButton, cancelButton);
    }

    private void setLayout() {
        this.getChildren().addAll(titleLabel, buttonsContainer);
        VBox.setMargin(buttonsContainer, new Insets(ScreenManager.SCREEN_HEIGHT * 0.1, 0, 0, 0));
    }

    private void loadStyles() {
        try {
            URL cssUrl = CssManager.getMatchCssURL("HostActionsModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load JoinMatchModal CSS: " + e.getMessage());
        }
    }

    private void handleEvent() {
        cancelButton.setOnAction(e -> {
            hide();
        });
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

    public void show(String username) {
        titleLabel.setText("What would you like to do with " + username + "?");

        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}
