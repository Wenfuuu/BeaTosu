package beat.osu.client.view.landing.component.layout;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.utils.BeatmapUtils;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class TopBar extends HBox {

    @Getter
    private UserCard userCard;

    private HBox controlsBar;
    private Label songTitle;

    private HBox nowPlayingSection;
    private VBox songBox;

    private EventHandler<MouseEvent> userCardClickHandler;

    private Label beatmapCountLabel;
    @Getter
    private Label runtimeLabel;
    @Getter
    private Label currentTimeLabel;

    private Timeline runtimeTimer;
    private Timeline clockTimer;
    @Getter
    private long startTime;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TopBar() {
        super(20);
        this.getStyleClass().add("top-bar");
        this.startTime = System.currentTimeMillis();

        initializeComponents();
        setupLayout();
        loadStyles();
        startTimers();
    }

    private void initializeComponents() {
        createUserInfoSection();
        createControlsBar();
        createNowPlayingSection();
        createBeatmapCountLabel();
        createRuntimeComponent();
        createCurrentTimeComponent();
    }

    private void createUserInfoSection() {
        UserDto user = AuthManager.getUser();
        createUserCard(user);
    }

    private void createUserCard(UserDto user) {
        if (user != null) {
            userCard = new UserCard(
                    user.getId(),
                    user.getUsername(),
                    user.getCountryCode(),
                    user.getProfilePicture(),
                    user.getPerformance(),
                    user.getAccuracy(),
                    user.getPlayCount(),
                    user.getLevel(),
                    user.getRank(),
                    user.isSupporter(),
                    UserCardBehavior.STATIC
            );
            userCard.updateUserInfo();
        } else {
            userCard = new UserCard(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    UserCardBehavior.EMPTY
            );
        }
    }

    private void createControlsBar() {
        controlsBar = new HBox(15);
        controlsBar.getStyleClass().add("controls-bar");
        controlsBar.setAlignment(Pos.CENTER_RIGHT);
    }

    private void createNowPlayingSection() {
        nowPlayingSection = new HBox();
        nowPlayingSection.setAlignment(Pos.CENTER_RIGHT);

        Label nowPlaying = new Label("Now");
        nowPlaying.getStyleClass().add("playing-label");

        Label playing = new Label("Playing");
        playing.getStyleClass().add("playing-label");

        songTitle = new Label("Minato Aqua - #Aquairo Palette");
        songTitle.getStyleClass().add("song-name");

        VBox songPlayingBox = new VBox(10, new HBox(new VBox(nowPlaying, playing), songTitle));
        songPlayingBox.setAlignment(Pos.CENTER);

        songBox = new VBox(songPlayingBox, controlsBar);
    }

    private void createBeatmapCountLabel() {
        int count = BeatmapUtils.getBeatmapCount();
        beatmapCountLabel = new Label("You have " + count + " beatmaps available!");
        beatmapCountLabel.getStyleClass().add("beatmap-count");
    }

    private void createRuntimeComponent() {
        runtimeLabel = new Label("osu! has been running for 0 seconds.");
        runtimeLabel.getStyleClass().add("runtime-label");
    }

    private void createCurrentTimeComponent() {
        currentTimeLabel = new Label("It is currently " + LocalTime.now().format(TIME_FORMATTER) + ".");
        currentTimeLabel.getStyleClass().add("current-time");
    }

    private void setupLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.setMaxHeight(90);

        VBox timeInfoBox = new VBox(2);
        timeInfoBox.setAlignment(Pos.TOP_LEFT);
        timeInfoBox.getChildren().addAll(beatmapCountLabel, runtimeLabel, currentTimeLabel);
        timeInfoBox.setPadding(new Insets(0, 0, 0, 4));

        this.getChildren().addAll(userCard, timeInfoBox, spacer, songBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("TopBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void startTimers() {
        runtimeTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRuntime()));
        runtimeTimer.setCycleCount(Timeline.INDEFINITE);
        runtimeTimer.play();

        clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCurrentTime()));
        clockTimer.setCycleCount(Timeline.INDEFINITE);
        clockTimer.play();
    }

    private void updateRuntime() {
        long currentTime = System.currentTimeMillis();
        long runtimeSeconds = (currentTime - startTime) / 1000;

        if (runtimeSeconds >= 60) {
            long hours = runtimeSeconds / 3600;
            long minutes = (runtimeSeconds % 3600) / 60;
            long seconds = runtimeSeconds % 60;

            if (hours > 0) {
                runtimeLabel.setText(String.format("osu! has been running for %02d:%02d:%02d.", hours, minutes, seconds));
            } else {
                runtimeLabel.setText(String.format("osu! has been running for %02d:%02d.", minutes, seconds));
            }

        } else {
            runtimeLabel.setText("osu! has been running for " + runtimeSeconds + " seconds.");
        }
    }

    private void updateCurrentTime() {
        currentTimeLabel.setText("It is currently " + LocalTime.now().format(TIME_FORMATTER) + ".");
    }

    public void addControlsToBar(HBox controls) {
        controlsBar.getChildren().setAll(controls.getChildren());
    }

    public void updateUserInfo(UserDto user) {
        if (userCard != null) {
            getChildren().remove(userCard);
        }
        
        if (user != null) {
            userCard = new UserCard(
                    user.getId(),
                    user.getUsername(),
                    user.getCountryCode(),
                    user.getProfilePicture(),
                    user.getPerformance(),
                    user.getAccuracy(),
                    user.getPlayCount(),
                    user.getLevel(),
                    user.getRank(),
                    user.isSupporter(),
                    UserCardBehavior.STATIC
            );
            userCard.updateUserInfo();
        } else {
            userCard = new UserCard(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    UserCardBehavior.EMPTY
            );
        }
        
        getChildren().add(0, userCard);
        
        if (userCardClickHandler != null) {
            userCard.setOnMouseClicked(userCardClickHandler);
        }
    }

    public void setFullOpacity() {
        FadeTransition fadeToFull = new FadeTransition(Duration.millis(300), this);
        fadeToFull.setFromValue(this.getOpacity());
        fadeToFull.setToValue(1.0);
        fadeToFull.play();
    }

    public void setLowOpacity() {
        FadeTransition fadeToLow = new FadeTransition(Duration.millis(300), this);
        fadeToLow.setFromValue(this.getOpacity());
        fadeToLow.setToValue(0.2);
        fadeToLow.play();
    }

    public void setSongTitle(String title) {
        songTitle.setText(title);
    }

    public String getSongTitle() {
        return songTitle.getText();
    }

    public void setUserCardClickHandler(EventHandler<MouseEvent> handler) {
        this.userCardClickHandler = handler;
        if (userCard != null) {
            userCard.setOnMouseClicked(handler);
        }
    }
}
