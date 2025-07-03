package beat.osu.client.view.home.component;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.shared.dto.score.ScoreDto;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.Getter;

public class ScoreItem extends HBox {

    @Getter
    private final ScoreDto score;

    private final ImageView gradeSymbol;
    private final ImageView profileImageView;

    private void updateGrade(String grade) {
        String gradeImagePath;
        if (grade.equals("SS"))
            gradeImagePath = "/assets/images/ranking-x.png";
        else
            gradeImagePath = "/assets/images/ranking-" + grade.toLowerCase() + ".png";
        Image gradeImage = new Image(Objects.requireNonNull(Main.class.getResource(gradeImagePath)).toExternalForm());
        gradeSymbol.setImage(gradeImage);
    }

    public ScoreItem(ScoreDto score) {
        this.score = score;

        gradeSymbol = new ImageView();
        updateGrade(score.getGrade());
        gradeSymbol.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.05);
        gradeSymbol.setPreserveRatio(true);

        String profileImagePath = "/assets/images/avatar-guest.png";
        profileImageView = new ImageView(
                new Image(Objects.requireNonNull(Main.class.getResource(profileImagePath)).toExternalForm()));
        profileImageView.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.058);
        profileImageView.setFitWidth(ScreenManager.SCREEN_HEIGHT * 0.058);

        Label usernameLabel = new Label(score.getUsername());
        usernameLabel.getStyleClass().add("score-username");
        usernameLabel.setFont(Font.font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.022));

        String scoreString = String.format("Score: %,d (%dx)", score.getScore(), score.getHighestCombo());
        Label scoreLabel = new Label(scoreString);
        scoreLabel.getStyleClass().add("score-value");
        scoreLabel.setFont(Font.font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.020));
        VBox scoreInfo = new VBox();
        scoreInfo.getStyleClass().add("score-info-container");
        scoreInfo.getChildren().addAll(usernameLabel, scoreLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label accuracyLabel = new Label(String.format("%.2f%%", score.getAccuracy()));
        accuracyLabel.getStyleClass().add("score-accuracy");
        accuracyLabel.setAlignment(Pos.CENTER_RIGHT);
        accuracyLabel.setFont(Font.font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.020));

        gradeSymbol.setTranslateY(-2);
        scoreInfo.setTranslateY(-2);
        accuracyLabel.setTranslateY(-3);

        this.getChildren().addAll(profileImageView, gradeSymbol, scoreInfo, spacer, accuracyLabel);

        setupUI();
        updateProfilePicture(score.getProfilePicture());
        loadStyles();
        setupHoverHandlers();
    }

    private void setupUI() {
        this.getStyleClass().add("score-item");
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.065);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.065);

        profileImageView.getStyleClass().add("score-profile-picture");
        gradeSymbol.getStyleClass().add("score-grade-symbol");
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("ScoreItem.css");
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

    private void setupHoverHandlers() {
        this.setOnMouseEntered(e -> {
            this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        });

        this.setOnMouseExited(e -> {
            this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25);");
        });
    }
}
