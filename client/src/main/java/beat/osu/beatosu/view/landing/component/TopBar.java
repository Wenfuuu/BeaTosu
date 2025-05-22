package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.Main;
import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class TopBar extends HBox {

    @Getter
    private HBox userInfoBox;
    private ImageView profilePic;
    private VBox userStats;
    private Label usernameLbl;
    private Label signinLbl;

    private HBox controlsBar;
    private Label songTitle;

    // Expose these for menu page to access
    private HBox nowPlayingSection;
    private VBox songBox;

    public TopBar() {
        super(20); // Spacing between children
        this.getStyleClass().add("top-bar");

        // Initialize components
        initializeComponents();

        // Set layout
        setupLayout();

        // Load CSS
        loadStyles();
    }

    private void initializeComponents() {
        createUserInfoSection();
        createControlsBar();
        createNowPlayingSection();
    }

    private void createUserInfoSection() {
        userInfoBox = new HBox(15);
        userInfoBox.setAlignment(Pos.CENTER_LEFT);
        userInfoBox.getStyleClass().add("user-info-box");

        profilePic = new ImageView();
        profilePic.setFitHeight(80);
        profilePic.setFitWidth(80);
        profilePic.getStyleClass().add("profile-pic");

        // Try to load a placeholder profile image
        try {
            profilePic.setImage(new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/avatar-guest.png")).toExternalForm()));
        } catch (Exception e) {
            // If image loading fails, create a colored rectangle as placeholder
            Region placeholder = new Region();
            placeholder.setPrefSize(60, 60);
            placeholder.setStyle("-fx-background-color: #888888; -fx-background-radius: 5;");
            userInfoBox.getChildren().add(placeholder);
        }

        userStats = new VBox(2);
        userStats.getStyleClass().add("user-stats");

        usernameLbl = new Label("Guest");
        usernameLbl.getStyleClass().add("username");

        signinLbl = new Label("Click to sign in!");
        signinLbl.getStyleClass().add("stats-text");

        userStats.getChildren().addAll(usernameLbl, signinLbl);
        userInfoBox.getChildren().addAll(profilePic, userStats);
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

        // Create the song box that will contain the playing info and controls
        songBox = new VBox(songPlayingBox, controlsBar);
    }

    private void setupLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.setMaxHeight(90);

        this.getChildren().addAll(userInfoBox, spacer, songBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("TopBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    // Add media controls
    public void addControlsToBar(HBox controls) {
        controlsBar.getChildren().setAll(controls.getChildren());
    }

    // Update user info when logged in
    public void updateUserInfo(User user) {
        if (user != null) {
            usernameLbl.setText(user.getUsername());
            signinLbl.setVisible(false);
        } else {
            usernameLbl.setText("Guest");
            signinLbl.setVisible(true);
        }
    }

    // Set song title
    public void setSongTitle(String title) {
        songTitle.setText(title);
    }

    // Get song title
    public String getSongTitle() {
        return songTitle.getText();
    }

}
