package beat.osu.client.view.game.component.cards;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Objects;

public class MatchScoreItem extends HBox {
    private final ImageView profileImageView;

    public MatchScoreItem(MatchScoreEvent event) {
        String profileImagePath = "/assets/images/avatar-guest.png";
        profileImageView = new ImageView(
                new Image(Objects.requireNonNull(Main.class.getResource(profileImagePath)).toExternalForm()));
        profileImageView.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.058);
        profileImageView.setFitWidth(ScreenManager.SCREEN_HEIGHT * 0.058);
        HBox.setMargin(profileImageView, new Insets(0, 8, 0, 0));

        String username = event.getUser().getUsername();
        System.out.println("Match player status " + event.getMatchPlayer().getStatus() + " for user " + username);
        if (event.getMatchPlayer().getStatus() == PlayerStatus.EXITED) {
            username = username + " [Quit]";
        }

        Label usernameLabel = new Label(username);
        usernameLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.023));

        if (event.getMatchPlayer().getStatus() == PlayerStatus.EXITED || event.getMatchPlayer().getStatus() == PlayerStatus.FAILED) {
            usernameLabel.getStyleClass().add("score-username-exited");
        } else {
            usernameLabel.getStyleClass().add("score-username");
        }

        String scoreString = String.format("%,d", event.getScore());
        Label scoreLabel = new Label(scoreString);
        scoreLabel.getStyleClass().add("score-value");
        scoreLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.019));

        Region vSpacer = new Region();
        HBox.setHgrow(vSpacer, Priority.ALWAYS);

        Label comboLabel = new Label(String.format("%dx", event.getCombo()));
        comboLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.018));
        comboLabel.getStyleClass().add("score-combo");
        comboLabel.setAlignment(Pos.BOTTOM_RIGHT);

        HBox bottomInfo = new HBox(0);
        bottomInfo.getChildren().addAll(scoreLabel, vSpacer, comboLabel);

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        VBox scoreInfo = new VBox(2);
        scoreInfo.getStyleClass().add("score-info-container");
        scoreInfo.getChildren().addAll(usernameLabel, hSpacer, bottomInfo);
        HBox.setHgrow(scoreInfo, Priority.ALWAYS);

        this.getChildren().addAll(profileImageView, scoreInfo);

        setupUI();
        updateProfilePicture(event.getUser().getProfilePicture());
        loadStyles();
    }

    private void setupUI() {
        this.getStyleClass().add("score-item");
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.065);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.065);

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
