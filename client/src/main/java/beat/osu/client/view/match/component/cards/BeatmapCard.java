package beat.osu.client.view.match.component.cards;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.view.match.component.enums.BeatmapCardVariant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class BeatmapCard extends StackPane {

    private BeatmapCardVariant variant;

    private int beatmapId;
    private int beatmapSetId;
    private String beatmapName;
    private String version;
    private String artist;
    private String creator;
    private double stars;
    private String beatmapBgPath;

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

    public static BeatmapCard available(Beatmap beatmap) {
        BeatmapCard card = new BeatmapCard(BeatmapCardVariant.AVAILABLE);
        card.getStyleClass().add("available-map-card");

        card.beatmapId = beatmap.getBeatmapId();
        card.beatmapSetId = beatmap.getBeatmapSetId();
        card.beatmapName = beatmap.getBeatmapSet().getTitle();
        card.version = beatmap.getVersion();
        card.artist = beatmap.getBeatmapSet().getArtist();
        card.creator = beatmap.getBeatmapSet().getCreator();
        card.stars = beatmap.getStarRating();

        try {
            OsuParser.parseBeatmap(beatmap);
            card.beatmapBgPath = card.getBgImagePath(beatmap.getBeatmapSetId(), OsuParser.getBgFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        noMapContent.setPadding(new Insets(3, 0, 0, 20));

        this.getChildren().add(noMapContent);
    }

    private void updateChangingMapUI() {
        titleLabel = new Label("Host is changing the map.");
        titleLabel.getStyleClass().add("changing-map-title");

        descriptionLabel = new Label("// Please wait!");
        descriptionLabel.getStyleClass().add("changing-map-description");

        VBox changingMapContent = new VBox(0);
        changingMapContent.getChildren().addAll(titleLabel, descriptionLabel);
        changingMapContent.setPadding(new Insets(3, 0, 0, 20));

        this.getChildren().add(changingMapContent);
    }

    private void updateAvailableMapUI() {
        File imageFile = new File(this.beatmapBgPath);
        Image image = new Image(imageFile.toURI().toString());
        beatmapImageView = new ImageView(image);

        beatmapImageView.setPreserveRatio(true);
        double fitHeight = this.getPrefHeight() - 1;
        beatmapImageView.setFitHeight(fitHeight);

        double aspectRatio = image.getWidth() / image.getHeight();
        double calculatedWidth = fitHeight * aspectRatio;

        StackPane.setAlignment(beatmapImageView, Pos.CENTER_LEFT);

        Region overlay = new Region();
        overlay.getStyleClass().add("beatmap-overlay");
        overlay.setPrefSize(this.getPrefWidth(), this.getPrefHeight());

        HBox contentContainer = new HBox();
        StackPane.setMargin(contentContainer, new Insets(0, 0, 0, calculatedWidth + 10));
        contentContainer.setSpacing(10);
        HBox.setHgrow(contentContainer, Priority.NEVER);

        ImageView gamemodeImageView = new ImageView();
        try {
            Image gamemodeImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/gamemode/osu-gamemode.png")).toExternalForm());
            gamemodeImageView.setImage(gamemodeImage);
            gamemodeImageView.setFitHeight(32);
            gamemodeImageView.setFitWidth(32);
        } catch (Exception e) {
            System.err.println("Could not load gamemode icon: " + e.getMessage());
            gamemodeImageView.setImage(null);
        }

        VBox gamemodeBox = new VBox(gamemodeImageView);
        gamemodeBox.setAlignment(Pos.TOP_CENTER);
        gamemodeBox.getStyleClass().add("gamemode-box");

        VBox infoBox = new VBox(-2);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(4, 0, 4, 0));
        HBox.setHgrow(infoBox, Priority.NEVER);

        beatmapNameLabel = new Label(beatmapName);
        beatmapNameLabel.getStyleClass().add("beatmap-name-label");

        beatmapInfoLabel = new Label(String.format("%s // %s", artist, creator));
        beatmapInfoLabel.getStyleClass().add("beatmap-info");

        beatmapVersionLabel = new Label(version);
        beatmapVersionLabel.getStyleClass().add("beatmap-version");

        beatmapStarsBox = createStarsBox();
        beatmapStarsBox.getStyleClass().add("beatmap-stars-box");

        infoBox.getChildren().addAll(beatmapNameLabel, beatmapInfoLabel, beatmapVersionLabel, beatmapStarsBox);
        contentContainer.getChildren().addAll(gamemodeBox, infoBox);

        this.getChildren().addAll(beatmapImageView, overlay, contentContainer);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getMatchCssURL("BeatmapCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private HBox createStarsBox() {
        HBox starsBox = new HBox(8);
        for (int i = 0; i < stars; i++) {
            Label star = new Label("★");
            starsBox.getChildren().add(star);
        }
        return starsBox;
    }

    private String getBgImagePath(int beatmapSetId, String gameBg) {
        File tempDir = ResourceManager.getTempDirectory();
        File beatmapDir = new File(tempDir, String.valueOf(beatmapSetId));
        File imageFile = new File(beatmapDir, gameBg);
        return imageFile.getAbsolutePath();
    }
}