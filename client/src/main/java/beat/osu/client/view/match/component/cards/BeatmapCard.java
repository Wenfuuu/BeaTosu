package beat.osu.client.view.match.component.cards;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.view.match.component.enums.BeatmapCardVariant;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BeatmapCard extends StackPane {

    private BeatmapCardVariant variant;

    private int beatmapId;
    private int beatmapSetId;
    private String beatmapName;
    private String version;
    private String artist;
    private String creator;

    private String length;
    private int bpm;
    private double circleSize;
    private double approachRate;
    private double overallDifficulty;
    private double hpDrainRate;
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

    private Region pinkOverlay;
    private Region orangeOverlay;

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

    public static BeatmapCard noMap(int beatmapId, int beatmapSetId, String beatmapName, String artist) {
        BeatmapCard card = new BeatmapCard(BeatmapCardVariant.NO_MAP);
        card.getStyleClass().add("no-map-card");

        card.beatmapId = beatmapId;
        card.beatmapSetId = beatmapSetId;
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
        card.length = beatmap.getBeatmapSet().getLength();
        card.bpm = beatmap.getBeatmapSet().getBpm();
        card.circleSize = beatmap.getCircleSize();
        card.approachRate = beatmap.getApproachRate();
        card.overallDifficulty = beatmap.getOverallDifficulty();
        card.hpDrainRate = beatmap.getHpDrainRate();
        card.variant = BeatmapCardVariant.AVAILABLE;
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

    private double getTitleFontSize() {
        return ScreenManager.SCREEN_HEIGHT * 0.0259;
    }

    private double getInfoFontSize() {
        return ScreenManager.SCREEN_HEIGHT * 0.02;
    }

    private double getDescriptionFontSize() {
        return ScreenManager.SCREEN_HEIGHT * 0.0185;
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
        titleLabel.setStyle("-fx-font-size: " + getTitleFontSize() + "px;");

        descriptionLabel = new Label("// Click to download this map!");
        descriptionLabel.getStyleClass().add("no-map-description");
        descriptionLabel.setStyle("-fx-font-size: " + getDescriptionFontSize() + "px;");

        VBox noMapContent = new VBox(0);
        noMapContent.getChildren().addAll(titleLabel, descriptionLabel);
        noMapContent.setPadding(new Insets(3, 0, 0, 20));

        this.setOnMouseClicked(event -> {
            String url = String.format("https://osu.ppy.sh/beatmapsets/%d#osu/%d", beatmapSetId, beatmapId);
            UrlManager.openURL(url);
        });

        this.getChildren().add(noMapContent);
    }

    private void updateChangingMapUI() {
        titleLabel = new Label("Host is changing the map.");
        titleLabel.getStyleClass().add("changing-map-title");
        titleLabel.setStyle("-fx-font-size: " + getTitleFontSize() + "px;");

        descriptionLabel = new Label("// Please wait!");
        descriptionLabel.getStyleClass().add("changing-map-description");
        descriptionLabel.setStyle("-fx-font-size: " + getDescriptionFontSize() + "px;");

        VBox changingMapContent = new VBox(0);
        changingMapContent.getChildren().addAll(titleLabel, descriptionLabel);
        changingMapContent.setPadding(new Insets(3, 0, 0, 20));

        this.getChildren().add(changingMapContent);
    }

    private void updateAvailableMapUI() {
        File imageFile = new File(this.beatmapBgPath);
        Image image = new Image(imageFile.toURI().toString());
        beatmapImageView = new ImageView(image);

        double fixedImageWidth = this.getPrefWidth() * 0.25;
        double fitHeight = this.getPrefHeight() - 1;

        beatmapImageView.setFitWidth(fixedImageWidth);
        beatmapImageView.setFitHeight(fitHeight);
        beatmapImageView.setPreserveRatio(false);

        StackPane.setAlignment(beatmapImageView, Pos.CENTER_LEFT);

        pinkOverlay = new Region();
        pinkOverlay.getStyleClass().add("pink-overlay");
        pinkOverlay.setPrefSize(this.getPrefWidth(), this.getPrefHeight());

        orangeOverlay = new Region();
        orangeOverlay.getStyleClass().add("orange-overlay");
        orangeOverlay.setPrefSize(this.getPrefWidth(), this.getPrefHeight());
        orangeOverlay.setOpacity(0.15);

        HBox contentContainer = new HBox();
        StackPane.setMargin(contentContainer, new Insets(0, 0, 0, fixedImageWidth + 10));
        contentContainer.setSpacing(10);
        HBox.setHgrow(contentContainer, Priority.NEVER);

        ImageView gamemodeImageView = new ImageView();
        try {
            Image gamemodeImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/images/misc/osu-gamemode.png")).toExternalForm());
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
        beatmapNameLabel.setStyle("-fx-font-size: " + getTitleFontSize() + "px;");

        beatmapInfoLabel = new Label(String.format("%s // %s", artist, creator));
        beatmapInfoLabel.getStyleClass().add("beatmap-info");
        beatmapInfoLabel.setStyle("-fx-font-size: " + getInfoFontSize() + "px;");

        beatmapVersionLabel = new Label(version);
        beatmapVersionLabel.getStyleClass().add("beatmap-version");
        beatmapVersionLabel.setStyle("-fx-font-size: " + getInfoFontSize() + "px;");

        beatmapStarsBox = createStarsBox();
        beatmapStarsBox.getStyleClass().add("beatmap-stars-box");
        beatmapStarsBox.setStyle("-fx-font-size: " + (getInfoFontSize() * 0.9) + "px;");

        infoBox.getChildren().addAll(beatmapNameLabel, beatmapInfoLabel, beatmapVersionLabel, beatmapStarsBox);
        contentContainer.getChildren().addAll(gamemodeBox, infoBox);

        setupHoverPopup();

        this.setOnMouseEntered(e -> {
            SfxManager.playMenuSfx(SfxType.SELECT_BEATMAP);
            transitionToOrange();
        });
        this.setOnMouseExited(e -> transitionToPink());

        this.getChildren().addAll(beatmapImageView, pinkOverlay, orangeOverlay, contentContainer);
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
        double starFontSize = getInfoFontSize() * 0.9;
        for (int i = 0; i < stars; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-font-size: " + starFontSize + "px;");
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

    private void setupHoverPopup() {
        if (version == null) {
            return;
        }

        String tooltipText = "BPM: " + bpm + "  Length: " + length + "\n"
                + "CS: " + circleSize + "  AR: " + approachRate + "  OD: " + overallDifficulty
                + "  HP: " + hpDrainRate + "\nStar Rating: " + stars + " ★";

        Tooltip playerTooltip = new Tooltip(tooltipText);
        playerTooltip.getStyleClass().add("beatmap-tooltip");

        playerTooltip.setShowDelay(Duration.millis(100));
        playerTooltip.setHideDelay(Duration.millis(100));

        Tooltip.install(this, playerTooltip);
    }

    private void transitionToPink() {
        FadeTransition pinkFadeIn = new FadeTransition(Duration.millis(200), pinkOverlay);
        pinkFadeIn.setFromValue(0.15);
        pinkFadeIn.setToValue(1.0);

        FadeTransition orangeFadeOut = new FadeTransition(Duration.millis(200), orangeOverlay);
        orangeFadeOut.setFromValue(1.0);
        orangeFadeOut.setToValue(0.15);

        ParallelTransition parallelTransition = new ParallelTransition(pinkFadeIn, orangeFadeOut);
        parallelTransition.play();
    }

    private void transitionToOrange() {
        FadeTransition pinkFadeOut = new FadeTransition(Duration.millis(200), pinkOverlay);
        pinkFadeOut.setFromValue(1.0);
        pinkFadeOut.setToValue(0.15);

        FadeTransition orangeFadeIn = new FadeTransition(Duration.millis(200), orangeOverlay);
        orangeFadeIn.setFromValue(0.15);
        orangeFadeIn.setToValue(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(pinkFadeOut, orangeFadeIn);
        parallelTransition.play();
    }
}