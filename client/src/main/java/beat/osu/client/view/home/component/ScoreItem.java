package beat.osu.client.view.home.component;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.shared.dto.score.ScoreDto;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

public class ScoreItem extends HBox {

    @Getter
    private ScoreDto score;
    @Setter
    private Consumer<ScoreItem> onSelectionCallback;

    private final ImageView gradeSymbol;
    private final ImageView profilePicture;

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
        gradeSymbol.setFitHeight(40);
        gradeSymbol.setPreserveRatio(true);

        String profileImagePath = "/assets/images/avatar-guest.png";
        profilePicture = new ImageView(
                new Image(Objects.requireNonNull(Main.class.getResource(profileImagePath)).toExternalForm()));
        profilePicture.setFitHeight(50);
        profilePicture.setPreserveRatio(true);

        Label usernameLabel = new Label(score.getUsername());
        usernameLabel.getStyleClass().add("score-username");

        String scoreString = String.format("Score: %,d (%dx)", score.getScore(), score.getHighestCombo());
        Label scoreLabel = new Label(scoreString);
        scoreLabel.getStyleClass().add("score-value");
        VBox scoreInfo = new VBox(3);
        scoreInfo.getStyleClass().add("score-info-container");
        scoreInfo.getChildren().addAll(usernameLabel, scoreLabel);

        // Create a spacer to push accuracy to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label accuracyLabel = new Label(String.format("%.2f%%", score.getAccuracy()));
        accuracyLabel.getStyleClass().add("score-accuracy");
        accuracyLabel.setAlignment(Pos.CENTER_RIGHT);

        this.getChildren().addAll(profilePicture, gradeSymbol, scoreInfo, spacer, accuracyLabel);

        setupUI();
        loadStyles();
        setupEventHandlers();
    }

    private void setupUI() {
        this.getStyleClass().add("score-item");
        this.setSpacing(15);
        this.setMinHeight(70);
        this.setPrefHeight(70);

        // Add margin classes for better spacing
        profilePicture.getStyleClass().add("score-profile-picture");
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

    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            if (onSelectionCallback != null) {
                onSelectionCallback.accept(this);
            }
            // ViewManager.getInstance().showScorePage(score);
        });
    }
}
