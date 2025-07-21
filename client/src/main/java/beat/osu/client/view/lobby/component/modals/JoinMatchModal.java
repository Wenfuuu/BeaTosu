package beat.osu.client.view.lobby.component.modals;

import java.net.URL;

import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class JoinMatchModal extends VBox {

    private Label titleLabel;

    private HBox passwordBox;
    private Label passwordLabel;
    private PasswordField passwordField;

    @Getter
    private Button joinGameButton;
    @Getter
    private Button cancelButton;

    private VBox buttonsContainer;

    @Getter
    private Integer selectedMatchId;
    @Getter
    private String selectedMatchName;

    public JoinMatchModal() {
        initializeComponents();
        setLayout();
        loadStyles();

        this.setVisible(false);
    }

    private void initializeComponents() {
        this.getStyleClass().add("root");

        titleLabel = new Label("Joining this game requires a password...");
        titleLabel.getStyleClass().add("title-label");

        passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("password-label");

        passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-input");

        joinGameButton = new Button("1. Join Game");
        cancelButton = new Button("2. Cancel");

        joinGameButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));
        cancelButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));

        joinGameButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        cancelButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);

        joinGameButton.getStyleClass().addAll("main-button", "join-game-button");
        cancelButton.getStyleClass().addAll("main-button", "cancel-button");

        buttonsContainer = new VBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.getChildren().addAll(joinGameButton, cancelButton);
    }

    private void setLayout() {
        this.getChildren().add(titleLabel);

        passwordBox = new HBox();
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        passwordBox.getStyleClass().add("password-box");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        this.getChildren().addAll(passwordBox, buttonsContainer);
        VBox.setMargin(passwordBox, new Insets(216, 0, 0, 0));
        VBox.setMargin(buttonsContainer, new Insets(92, 0, 0, 0));
    }

    private void loadStyles() {
        try {
            URL cssUrl = CssManager.getLobbyCssURL("JoinMatchModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            // System.err.println("Could not load JoinMatchModal CSS: " + e.getMessage());
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
        passwordField.clear();

        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void showForMatch(Integer matchId, String matchName) {
        this.selectedMatchId = matchId;
        this.selectedMatchName = matchName;

        passwordBox.setVisible(true);
        passwordBox.setManaged(true);

        show();
    }

    public String getPassword() {
        return passwordField.getText();
    }
}
