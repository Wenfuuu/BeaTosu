package beat.osu.client.view.game.component.panels;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Objects;

public class ScorePanel extends HBox {

    private final Image[] digitImages;

    private int score;

    private ImageView scoreImageView;
    private HBox digitContainer;
    private ImageView[] scoreDigits;

    public ScorePanel(Image[] digitImages) {
        this.digitImages = digitImages;
        this.score = 0;

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        Image scoreImage = new Image(Objects.requireNonNull(Main.class.getResource("/assets/score/score_and_hits.png")).toExternalForm());
        scoreImageView = new ImageView(scoreImage);
        scoreImageView.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.046);
        scoreImageView.setPreserveRatio(true);
        scoreImageView.setSmooth(true);

        scoreDigits = new ImageView[8];
        digitContainer = new HBox(3);
        for (int i = 0; i < 8; i++) {
            scoreDigits[i] = new ImageView(digitImages[0]);
            scoreDigits[i].setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.06);
            scoreDigits[i].setPreserveRatio(true);
            digitContainer.setAlignment(Pos.CENTER);
            digitContainer.getChildren().add(scoreDigits[i]);
        }
    }

    private void setupLayout() {
        this.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.45);
        this.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.45);
        this.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.45);

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.093);
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.093);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.093);

        HBox.setMargin(scoreImageView, new Insets(0, ScreenManager.SCREEN_WIDTH * 0.05, 0, ScreenManager.SCREEN_WIDTH * 0.015));

        this.getStyleClass().add("score-panel");

        this.getChildren().addAll(scoreImageView, digitContainer);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getGameCssURL("ScorePanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public void updateScore(long score) {
        String scoreStr = String.format("%08d", Math.min(score, 99999999L));
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(scoreStr.charAt(i));
            scoreDigits[i].setImage(digitImages[digit]);
        }
    }
}
