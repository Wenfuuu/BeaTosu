package beat.osu.client.view.match.component.cards;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.match.component.enums.BeatmapCardVariant;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.text.html.ImageView;
import java.net.URL;

public class BeatmapCard extends HBox {

    private BeatmapCardVariant variant;

    private String beatmapId;
    private String beatmapSetId;
    private String beatmapName;
    private String artist;
    private String creator;
    private double stars;

    // Components for changing and no map variants
    private Label titleLabel;
    private Label descriptionLabel;

    // Components for available variant
    private ImageView beatmapImageView;
    private Label beatmapNameLabel;
    private Label beatmapInfoLabel;
    private Label beatmapVersionLabel;
    private HBox beatmapStarsBox;

    private BeatmapCard(BeatmapCardVariant variant) {
        this.variant = variant;
        setupUI();
    }

    public static BeatmapCard changingMap() {
        BeatmapCard card = new BeatmapCard(BeatmapCardVariant.CHANGING_MAP);
        card.getStyleClass().add("changing-map-card");

        card.updateUI();
        return card;
    }

    public static BeatmapCard noMap(String beatmapName, String artist) {
        BeatmapCard card = new BeatmapCard(BeatmapCardVariant.NO_MAP);
        card.getStyleClass().add("no-map-card");

        card.beatmapName = beatmapName;
        card.artist = artist;

        card.updateUI();
        return card;
    }

    public static BeatmapCard available(String beatmapId, String beatmapSetId, String beatmapName, String artist, String creator, double stars) {
        BeatmapCard card = new BeatmapCard(BeatmapCardVariant.AVAILABLE);
        card.getStyleClass().add("available-map-card");

        card.beatmapId = beatmapId;
        card.beatmapSetId = beatmapSetId;
        card.beatmapName = beatmapName;
        card.artist = artist;
        card.creator = creator;
        card.stars = stars;

        card.updateUI();
        return card;
    }

    private void setupUI() {
        this.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.40);
        this.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.40);
        this.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.40);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.11);
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.11);
        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.11);

        this.getStyleClass().add("beatmap-card");

        loadStyles();
    }

    private void updateUI() {
        switch (variant) {
            case NO_MAP:
                updateNoMapUI();
                break;
            case CHANGING_MAP:
                updateChangingMapUI();
                break;
            case AVAILABLE:
                updateAvailableMapUI();
                break;
            default:
                throw new IllegalArgumentException("Unknown BeatmapCardVariant: " + variant);
        }
    }

    private void updateNoMapUI() {
        titleLabel = new Label(artist + " - " + beatmapName);
        titleLabel.getStyleClass().add("no-map-title");

        descriptionLabel = new Label("// Click to download this map!");
        descriptionLabel.getStyleClass().add("no-map-description");

        VBox noMapContent = new VBox(0);
        noMapContent.getChildren().addAll(titleLabel, descriptionLabel);

        this.getChildren().add(noMapContent);
    }

    private void updateChangingMapUI() {
        titleLabel = new Label("Host is changing the map.");
        titleLabel.getStyleClass().add("changing-map-title");

        descriptionLabel = new Label("// Please wait!");
        descriptionLabel.getStyleClass().add("changing-map-description");

        VBox changingMapContent = new VBox(0);
        changingMapContent.getChildren().addAll(titleLabel, descriptionLabel);

        this.getChildren().add(changingMapContent);
    }

    private void updateAvailableMapUI() {
        if (beatmapNameLabel == null) {
            beatmapNameLabel = new Label(beatmapName);
            beatmapNameLabel.getStyleClass().add("beatmap-name");
            this.getChildren().add(beatmapNameLabel);
        } else {
            beatmapNameLabel.setText(beatmapName);
        }

        if (beatmapInfoLabel == null) {
            beatmapInfoLabel = new Label(String.format("%s // %s", artist, creator));
            beatmapInfoLabel.getStyleClass().add("beatmap-info");
            this.getChildren().add(beatmapInfoLabel);
        } else {
            beatmapInfoLabel.setText(String.format("%s // %s", artist, creator));
        }

        if (beatmapVersionLabel == null) {
            beatmapVersionLabel = new Label("Version: " + "N/A"); // Placeholder for version
            beatmapVersionLabel.getStyleClass().add("beatmap-version");
            this.getChildren().add(beatmapVersionLabel);
        } else {
            beatmapVersionLabel.setText("Version: " + "N/A"); // Placeholder for version
        }

        if (beatmapStarsBox == null) {
            beatmapStarsBox = new HBox();
            beatmapStarsBox.getStyleClass().add("beatmap-stars-box");
            this.getChildren().add(beatmapStarsBox);
        }
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getMatchCssURL("BeatmapCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}