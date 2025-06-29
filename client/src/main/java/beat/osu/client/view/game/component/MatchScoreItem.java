package beat.osu.client.view.game.component;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Objects;

public class MatchScoreItem extends HBox {
    private final ImageView profileImageView;

    public MatchScoreItem(MatchScoreEvent event) {
        String profileImagePath = "/assets/images/avatar-guest.png";
        profileImageView = new ImageView(
                new Image(Objects.requireNonNull(Main.class.getResource(profileImagePath)).toExternalForm()));
        profileImageView.setFitHeight(50);
        profileImageView.setFitWidth(50);

        Label usernameLabel = new Label(event.getUser().getUsername());
        usernameLabel.getStyleClass().add("score-username");
        usernameLabel.setMaxWidth(150);

        String scoreString = String.format("%,d", event.getScore());
        Label scoreLabel = new Label(scoreString);
        scoreLabel.getStyleClass().add("score-value");
        VBox scoreInfo = new VBox(3);
        scoreInfo.getStyleClass().add("score-info-container");
        scoreInfo.getChildren().addAll(usernameLabel, scoreLabel);

        // Create a spacer to push accuracy to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label comboLabel = new Label(String.format("%dx", event.getCombo()));
        comboLabel.getStyleClass().add("score-combo");
        comboLabel.setAlignment(Pos.BOTTOM_RIGHT);

        this.getChildren().addAll(profileImageView, scoreInfo, spacer, comboLabel);

        setupUI();
        updateProfilePicture(event.getUser().getProfilePicture());
        loadStyles();
    }

    private void setupUI() {
        this.getStyleClass().add("score-item");
        this.setSpacing(15);
        this.setMinHeight(70);
        this.setPrefHeight(70);

        // Add margin classes for better spacing
        profileImageView.getStyleClass().add("score-profile-picture");
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getGameCssURL("MatchScoreItem.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void updateProfilePicture(byte[] profile) {
        if (profile != null && profile.length > 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(profile);
                Image userImage = new Image(bis);
                profileImageView.setImage(userImage);
            } catch (Exception e) {
                System.err.println("Could not load user profile picture: " + e.getMessage());
                setDefaultProfilePicture();
            }
        } else {
            setDefaultProfilePicture();
        }
    }

    private void setDefaultProfilePicture() {
        try {
            Image defaultImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/images/avatar-guest.png")).toExternalForm());
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default avatar: " + e.getMessage());
            profileImageView.setImage(null);
        }
    }
}
