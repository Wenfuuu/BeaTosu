package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Objects;

public class ChatToggleButton extends Button {

    private ImageView showChatIcon;
    private ImageView hideChatIcon;
    private boolean isChatVisible = false;

    public ChatToggleButton() {
        super();

        URL cssUrl = CssManager.getLandingCssURL("BanchoButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.getStyleClass().add("bancho-button");

        try {
            showChatIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/show_chat.png")).toExternalForm()));
            setupImageView(showChatIcon);

            hideChatIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/hide_chat.png")).toExternalForm()));
            setupImageView(hideChatIcon);

            this.setGraphic(showChatIcon);

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

    public void setHideIcon() {
        this.isChatVisible = true;
        this.setGraphic(hideChatIcon);
    }

    public void setShowIcon() {
        this.isChatVisible = false;
        this.setGraphic(showChatIcon);
    }

    public boolean isChatVisible() {
        return isChatVisible;
    }
}
