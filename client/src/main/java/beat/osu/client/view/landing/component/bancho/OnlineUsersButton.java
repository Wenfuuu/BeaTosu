package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class OnlineUsersButton extends Button {
    private ImageView onlineUsersOnIcon;
    private ImageView onlineUsersOffIcon;

    @Getter
    private boolean isOnlineUserShown = false;

    public OnlineUsersButton() {
        super();

        URL cssUrl = CssManager.getLandingCssURL("BanchoButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.getStyleClass().add("bancho-button");

        try {
            onlineUsersOnIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/online_users_on.png")).toExternalForm()));
            setupImageView(onlineUsersOnIcon);

            onlineUsersOffIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/online_users_off.png")).toExternalForm()));
            setupImageView(onlineUsersOffIcon);

            this.setGraphic(onlineUsersOffIcon);

        } catch (Exception e) {
            System.err.println("Failed to load chat toggle icons: " + e.getMessage());
            this.setText("Chat");
        }
    }

    private void setupImageView(ImageView view) {
        view.setPreserveRatio(true);
        view.setFitHeight(27);
        view.setSmooth(true);
    }

    public void setOnlineUsersShownIcon() {
        this.isOnlineUserShown = true;
        this.setGraphic(onlineUsersOnIcon);
    }

    public void setOnlineUsersHiddenIcon() {
        this.isOnlineUserShown = false;
        this.setGraphic(onlineUsersOffIcon);
    }
}