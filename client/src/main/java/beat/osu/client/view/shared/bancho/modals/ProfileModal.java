package beat.osu.client.view.shared.bancho.modals;

import beat.osu.client.controller.AuthController;
import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.*;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;

public class ProfileModal extends VBox {

    private UserCard userCard;

    @Getter
    private Button signOutButton;
    private Button closeButton;
    private VBox buttonsContainer;

    public ProfileModal() {
        initializeComponents();
        setLayout();
        setupStyling();

        this.setVisible(false);
    }

    private void initializeComponents() {
        userCard = new UserCard();

        signOutButton = new Button("1. Sign Out");
        closeButton = new Button("2. Close");

        signOutButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        closeButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);

        signOutButton.getStyleClass().addAll("modal-button", "sign-out-button");
        closeButton.getStyleClass().addAll("modal-button", "close-button");

        closeButton.setOnMouseClicked(event -> {
            SfxManager.playMenuSfx(SfxType.MENU_BACK);
            hide();
        });

        buttonsContainer = new VBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.getChildren().addAll(signOutButton, closeButton);
    }

    private void setLayout() {
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(new Insets(40, 0, 0, 15));
        this.setSpacing(30);

        this.getChildren().addAll(userCard, buttonsContainer);
        this.getStyleClass().add("profile-modal");
    }

    private void setupStyling() {
        try {
            URL cssUrl = CssManager.getSharedCssURL("ProfileModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            // System.err.println("Could not load ProfileModal CSS: " + e.getMessage());
        }
    }

    public void show() {
        if (!AuthManager.isAuthenticated()) {
            Toast.error("User is not authenticated!");
            return;
        }

        UserDto user = AuthManager.getUser();
        UserCard newUserCard = new UserCard(
                user.getId(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfilePicture(),
                user.getPerformance(),
                user.getAccuracy(),
                user.getPlayCount(),
                user.getRank(),
                user.getLevel(),
                user.getExperience(),
                user.isSupporter(),
                UserCardBehavior.STATIC
        );

        this.getChildren().remove(userCard);
        this.userCard = newUserCard;
        this.getChildren().add(0, userCard);

        this.setVisible(true);
        this.setManaged(true);
        this.setOpacity(0);
        this.toFront();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
            this.setManaged(false);
        });
        fadeOut.play();
    }
}
