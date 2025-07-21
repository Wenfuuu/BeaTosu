package beat.osu.client.view.game.component.layout;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.net.URL;

public class ResultHeader extends VBox {

    private String beatmapTitle;
    private String beatmapCreator;
    private String playedBy;

    private Label beatmapTitleLabel;
    private Label beatmapCreatorLabel;
    private Label playedByLabel;

    public ResultHeader() {
        this.beatmapTitle = "";
        this.beatmapCreator = "";
        this.playedBy = "";

        initializeComponents();
        loadStyles();
        setupLayout();
    }

    private void initializeComponents() {
        beatmapTitleLabel = new Label(beatmapTitle);
        beatmapCreatorLabel = new Label(beatmapCreator);
        playedByLabel = new Label(playedBy);
    }

    private void loadStyles() {
         URL cssUrl = CssManager.getGameCssURL("ResultHeader.css");
         if (cssUrl != null) {
             this.getStylesheets().add(cssUrl.toExternalForm());
         }
    }

    private void setupLayout() {
        this.getStyleClass().add("result-top-bar");
        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT / 9);
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT / 9);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT / 9);

        beatmapTitleLabel.getStyleClass().add("beatmap-title-label");
        beatmapCreatorLabel.getStyleClass().add("beatmap-creator-label");
        playedByLabel.getStyleClass().add("played-by-label");

        beatmapTitleLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.036));
        beatmapCreatorLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.024));
        playedByLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT * 0.024));

        beatmapCreatorLabel.setTranslateY(-4);
        playedByLabel.setTranslateY(-8);

        this.getChildren().addAll(beatmapTitleLabel, beatmapCreatorLabel, playedByLabel);
    }

    public void updateLabels(String beatmapTitle, String beatmapCreator, String playedBy) {
        this.beatmapTitle = beatmapTitle;
        this.beatmapCreator = beatmapCreator;
        this.playedBy = playedBy;

        beatmapTitleLabel.setText(beatmapTitle);
        beatmapCreatorLabel.setText(beatmapCreator);
        playedByLabel.setText(playedBy);
    }
}
