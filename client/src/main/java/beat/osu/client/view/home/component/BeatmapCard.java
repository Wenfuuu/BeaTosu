package beat.osu.client.view.home.component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

import beat.osu.client.Main;
import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
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
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

public class BeatmapCard extends StackPane {

    private final double DEFAULT_OPACITY = 1.0;
    private final double SELECTED_OPACITY = 0.75;

    @Getter
    private final Beatmap beatmap;
    @Setter
    private Consumer<BeatmapCard> onClickCallback;
    @Getter
    private boolean isSelected = false;

    // UI Components
    private ImageView beatmapImageView;
    private Label beatmapNameLabel;
    private Label beatmapInfoLabel;
    private Label beatmapVersionLabel;
    private HBox beatmapStarsBox;
    private Region pinkOverlay;
    private Region orangeOverlay;
    private String beatmapBgPath;

    public BeatmapCard(Beatmap beatmap) {
        this.beatmap = beatmap;

        setupUI();
        loadStyles();
        setupEventHandlers();
        setupBackground();
    }

    private void setupUI() {
        this.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.40);
        this.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.40);
        this.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.40);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.11);
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.11);
        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.11);

        this.getStyleClass().add("beatmap-card");
    }

    private void setupBackground() {
        try {
            OsuParser.parseBeatmap(beatmap);
            this.beatmapBgPath = getBgImagePath(beatmap.getBeatmapSetId(), OsuParser.getBgFile());
            setupAvailableMapUI();
        } catch (IOException e) {
            System.err.println("Error parsing beatmap: " + e.getMessage());
            // Fallback to simple layout if background fails
            setupFallbackUI();
        }
    }

    private void setupAvailableMapUI() {
        File imageFile = new File(this.beatmapBgPath);
        Image image = new Image(imageFile.toURI().toString());
        beatmapImageView = new ImageView(image);

        double fixedImageWidth = this.getPrefWidth() * 0.25;
        double fitHeight = this.getPrefHeight() - 2;

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

        beatmapNameLabel = new Label(beatmap.getBeatmapSet().getTitle());
        beatmapNameLabel.getStyleClass().add("beatmap-name-label");
        beatmapNameLabel.setStyle("-fx-font-size: " + getTitleFontSize() + "px;");

        String artist = String.format("%s // %s",
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getCreator());
        beatmapInfoLabel = new Label(artist);
        beatmapInfoLabel.getStyleClass().add("beatmap-info");
        beatmapInfoLabel.setStyle("-fx-font-size: " + getInfoFontSize() + "px;");

        beatmapVersionLabel = new Label(beatmap.getVersion());
        beatmapVersionLabel.getStyleClass().add("beatmap-version");
        beatmapVersionLabel.setStyle("-fx-font-size: " + getInfoFontSize() + "px;");

        beatmapStarsBox = createStarsBox();
        beatmapStarsBox.getStyleClass().add("beatmap-stars-box");
        beatmapStarsBox.setStyle("-fx-font-size: " + (getInfoFontSize() * 0.9) + "px;");

        infoBox.getChildren().addAll(beatmapNameLabel, beatmapInfoLabel, beatmapVersionLabel, beatmapStarsBox);
        contentContainer.getChildren().addAll(gamemodeBox, infoBox);

        this.setOnMouseEntered(e -> {
            SfxManager.playMenuSfx(SfxType.SELECT_BEATMAP);
            transitionToOrange();
        });
        this.setOnMouseExited(e -> transitionToPink());

        this.getChildren().addAll(beatmapImageView, pinkOverlay, orangeOverlay, contentContainer);
    }

    private void setupFallbackUI() {
        // Fallback to original simple layout if background loading fails
        VBox textInfo = createTextInfo();
        this.getChildren().add(textInfo);
    }

    private double getTitleFontSize() {
        return ScreenManager.SCREEN_HEIGHT * 0.0259;
    }

    private double getInfoFontSize() {
        return ScreenManager.SCREEN_HEIGHT * 0.02;
    }

    private VBox createTextInfo() {
        VBox textInfo = new VBox(2);
        textInfo.setPadding(new Insets(10, 0, 0, 10));
        textInfo.setPrefWidth(350);

        Label titleLabel = new Label(beatmap.getBeatmapSet().getTitle());
        titleLabel.getStyleClass().add("title");
        titleLabel.setStyle("-fx-font-size: " + getTitleFontSize() + "px;");

        String artist = String.format("%s // %s",
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getCreator());
        Label artistLabel = new Label(artist);
        artistLabel.setStyle("-fx-font-size: " + getInfoFontSize() + "px;");

        Label versionLabel = new Label(beatmap.getVersion());
        versionLabel.setStyle("-fx-font-size: " + getInfoFontSize() + "px;");

        HBox starsBox = createStarsBox();

        textInfo.getChildren().addAll(titleLabel, artistLabel, versionLabel, starsBox);
        return textInfo;
    }

    private HBox createStarsBox() {
        HBox starsBox = new HBox(8);
        double starFontSize = getInfoFontSize() * 0.9;
        for (int i = 0; i < beatmap.getStarRating(); i++) {
            Label star = new Label("★");
            star.setStyle("-fx-font-size: " + starFontSize + "px;");
            starsBox.getChildren().add(star);
        }
        return starsBox;
    }

    private String getBgImagePath(int beatmapSetId, String gameBg) {
        File tempDir = ResourceManager.getBeatmapDirectory();
        File beatmapDir = new File(tempDir, String.valueOf(beatmapSetId));
        File imageFile = new File(beatmapDir, gameBg);
        return imageFile.getAbsolutePath();
    }

    private void transitionToPink() {
        if (pinkOverlay != null && orangeOverlay != null) {
            FadeTransition pinkFadeIn = new FadeTransition(Duration.millis(200), pinkOverlay);
            pinkFadeIn.setFromValue(0.15);
            if (isSelected)
                pinkFadeIn.setToValue(SELECTED_OPACITY);
            else
                pinkFadeIn.setToValue(DEFAULT_OPACITY);

            FadeTransition orangeFadeOut = new FadeTransition(Duration.millis(200), orangeOverlay);
            orangeFadeOut.setFromValue(1.0);
            orangeFadeOut.setToValue(0.15);

            ParallelTransition parallelTransition = new ParallelTransition(pinkFadeIn, orangeFadeOut);
            parallelTransition.play();
        }
    }

    private void transitionToOrange() {
        if (pinkOverlay != null && orangeOverlay != null) {
            FadeTransition pinkFadeOut = new FadeTransition(Duration.millis(200), pinkOverlay);
            if (isSelected)
                pinkFadeOut.setFromValue(SELECTED_OPACITY);
            else
                pinkFadeOut.setFromValue(DEFAULT_OPACITY);
            pinkFadeOut.setToValue(0.15);

            FadeTransition orangeFadeIn = new FadeTransition(Duration.millis(200), orangeOverlay);
            orangeFadeIn.setFromValue(0.15);
            orangeFadeIn.setToValue(1.0);

            ParallelTransition parallelTransition = new ParallelTransition(pinkFadeOut, orangeFadeIn);
            parallelTransition.play();
        }
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

        if (selected && pinkOverlay != null && beatmapImageView != null) {
            beatmapImageView.toFront();
            pinkOverlay.setOpacity(SELECTED_OPACITY);
        } else if (pinkOverlay != null && beatmapImageView != null) {
            beatmapImageView.toBack();
            pinkOverlay.setOpacity(DEFAULT_OPACITY);
        }
    }

    public void parseBeatmapIfNeeded() throws IOException {
        OsuParser.parseBeatmap(beatmap);
    }
}
