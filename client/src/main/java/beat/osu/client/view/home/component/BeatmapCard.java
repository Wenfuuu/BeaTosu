package beat.osu.client.view.home.component;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class BeatmapCard extends StackPane {
    private static final double CARD_HEIGHT = 70;
    
    @Getter
    private final Beatmap beatmap;
    private Consumer<BeatmapCard> onClickCallback;
    private boolean isSelected = false;

    public BeatmapCard(Beatmap beatmap) {
        this.beatmap = beatmap;
        
        initializeCard();
        loadStyles();
        setupEventHandlers();
    }

    private void initializeCard() {
        this.setPrefHeight(CARD_HEIGHT);
        this.getStyleClass().add("beatmap-card");
        
        // Create layers
        HBox backgroundLayer = createBackgroundLayer();
        HBox overlayLayer = createOverlayLayer();
        HBox contentLayer = createContentLayer();
        
        this.getChildren().addAll(backgroundLayer, overlayLayer, contentLayer);
    }

    private HBox createBackgroundLayer() {
        HBox backgroundLayer = new HBox();
        backgroundLayer.setPrefHeight(CARD_HEIGHT);
        BackgroundManager.setBeatmapBackground(backgroundLayer);
        return backgroundLayer;
    }

    private HBox createOverlayLayer() {
        HBox overlayLayer = new HBox();
        overlayLayer.setPrefHeight(CARD_HEIGHT);
        overlayLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        return overlayLayer;
    }

    private HBox createContentLayer() {
        HBox contentLayer = new HBox();
        contentLayer.setPrefHeight(CARD_HEIGHT);
        contentLayer.setPickOnBounds(false);
        contentLayer.getStyleClass().add("beatmap-content");

        VBox textInfo = createTextInfo();
        contentLayer.getChildren().add(textInfo);

        return contentLayer;
    }

    private VBox createTextInfo() {
        VBox textInfo = new VBox(2);
        textInfo.setPadding(new Insets(10, 0, 0, 10));
        textInfo.setPrefWidth(350);

        Label titleLabel = new Label(beatmap.getBeatmapSet().getTitle());
        titleLabel.getStyleClass().add("title");

        String artist = String.format("%s // %s", 
            beatmap.getBeatmapSet().getArtist(),
            beatmap.getBeatmapSet().getCreator());
        Label artistLabel = new Label(artist);

        Label versionLabel = new Label(beatmap.getVersion());

        HBox starsBox = createStarsBox();

        textInfo.getChildren().addAll(titleLabel, artistLabel, versionLabel, starsBox);
        return textInfo;
    }

    private HBox createStarsBox() {
        HBox starsBox = new HBox(2);
        for (int i = 0; i < beatmap.getStarRating(); i++) {
            Label star = new Label("★");
            starsBox.getChildren().add(star);
        }
        return starsBox;
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("BeatmapCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("BeatmapCard CSS file not found!");
        }
    }

    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            if (onClickCallback != null) {
                onClickCallback.accept(this);
            }
        });
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (selected) {
            this.getStyleClass().add("selected");
        } else {
            this.getStyleClass().remove("selected");
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setOnClickCallback(Consumer<BeatmapCard> callback) {
        this.onClickCallback = callback;
    }

    public void parseBeatmapIfNeeded() throws IOException {
        OsuParser.parseBeatmap(beatmap);
    }
}
