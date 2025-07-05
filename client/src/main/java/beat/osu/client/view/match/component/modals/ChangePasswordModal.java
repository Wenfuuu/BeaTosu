package beat.osu.client.view.match.component.modals;

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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;

public class ChangePasswordModal extends VBox {

    private Label titleLabel;

    private HBox passwordBox;
    private Label passwordLabel;
    private PasswordField passwordField;

    @Getter
    private Button confirmButton;
    @Getter
    private Button cancelButton;

    private VBox buttonsContainer;

    @Getter
    private Integer selectedMatchId;
    @Getter
    private String selectedMatchName;

    public ChangePasswordModal() {
        initializeComponents();
        setLayout();
        loadStyles();
        handleEvent();
        setupInputFieldSounds();

        this.setVisible(false);
    }

    private void setupInputFieldSounds() {
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playMenuSfx(SfxType.KEY_DELETE);
            } else {
                SfxManager.playMenuSfx(SfxType.KEY_PRESS);
            }
        });
    }

    private void initializeComponents() {
        this.getStyleClass().add("root");

        titleLabel = new Label("Enter a new password...");
        titleLabel.getStyleClass().add("title-label");

        passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("password-label");

        passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-input");

        confirmButton = new Button("1. OK");
        cancelButton = new Button("2. Cancel");

        confirmButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));
        cancelButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));

        confirmButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        cancelButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);

        confirmButton.getStyleClass().addAll("main-button", "confirm-button");
        cancelButton.getStyleClass().addAll("main-button", "cancel-button");

        buttonsContainer = new VBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.getChildren().addAll(confirmButton, cancelButton);
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
            URL cssUrl = CssManager.getMatchCssURL("ChangePasswordModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load ChangePasswordModal CSS: " + e.getMessage());
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

    private void handleEvent() {
        cancelButton.setOnAction(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_BACK);
            hide();
        });
    }

    public String getPassword() {
        return passwordField.getText();
    }
}
