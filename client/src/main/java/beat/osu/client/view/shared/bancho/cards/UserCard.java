package beat.osu.client.view.shared.bancho.cards;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCard extends HBox {

    private Integer userId;
    private String username;
    private String countryCode;
    private byte[] profilePicture;
    private int performance;
    private double accuracy;
    private int playCount;
    private int level;
    private int rank;
    private boolean isSupporter;

    private ImageView profileImageView;
    private ImageView gamemodeImageView;
    private Label usernameLabel;
    private Label performanceLabel;
    private Label accuracyLabel;
    private Label playCountLabel;
    private Label backgroundRankLabel;

    @Getter
    private Label timeLabel;
    @Getter
    private VBox userStats;
    @Getter
    private VBox timeStats;
    private StackPane contentContainer;
    private boolean isHovering = false;

    @Setter
    @Getter
    private ParallelTransition currentTransition;
    private UserCardBehavior behavior;

    public UserCard(Integer userId, String username, String countryCode, byte[] profilePicture,
                    int performance, double accuracy, int playCount, int level, int rank, boolean isSupporter,
                    UserCardBehavior behavior) {
        super(10);
        this.userId = userId;
        this.username = username;
        this.countryCode = countryCode;
        this.profilePicture = profilePicture;
        this.performance = performance;
        this.accuracy = accuracy;
        this.playCount = playCount;
        this.level = level;
        this.rank = rank;
        this.isSupporter = isSupporter;

        initializeComponents();
        setupLayout();
        setupStyling();
        updateUserInfo();
        
        setBehavior(behavior);
    }

    private void initializeComponents() {
        this.setMaxWidth(475);

        profileImageView = new ImageView();
        profileImageView.getStyleClass().add("profile-picture");

        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        setDefaultProfilePicture();

        gamemodeImageView = new ImageView();
        gamemodeImageView.getStyleClass().add("gamemode-icon");
        setGamemodeIcon();

        usernameLabel = new Label("Guest");
        usernameLabel.getStyleClass().add("username-label");

        performanceLabel = new Label("Performance: 0pp");
        performanceLabel.getStyleClass().add("performance-label");

        accuracyLabel = new Label("Accuracy: 0.00%");
        accuracyLabel.getStyleClass().add("accuracy-label");

        playCountLabel = new Label("Play Count: 0 (Lv0)");
        playCountLabel.getStyleClass().add("stats-label");

        backgroundRankLabel = new Label("#" + rank);
        backgroundRankLabel.getStyleClass().add("background-rank-label");

        timeLabel = new Label("");
        timeLabel.getStyleClass().add("time-label");
    }

    private void setupLayout() {
        userStats = new VBox(2);
        userStats.setAlignment(Pos.TOP_LEFT);
        userStats.getStyleClass().add("user-stats");
        userStats.getChildren().addAll(performanceLabel, accuracyLabel, playCountLabel);

        timeStats = new VBox(2);
        timeStats.setAlignment(Pos.TOP_LEFT);
        timeStats.getStyleClass().add("user-stats");
        timeStats.getChildren().add(timeLabel);
        
        timeStats.setOpacity(0.0);

        contentContainer = new StackPane();
        contentContainer.setAlignment(Pos.TOP_LEFT);
        contentContainer.getChildren().addAll(userStats, timeStats);

        VBox mainStats = new VBox(2);
        mainStats.setAlignment(Pos.TOP_LEFT);
        mainStats.getChildren().addAll(usernameLabel, contentContainer);

        HBox mainContent = new HBox(10);
        mainContent.setAlignment(Pos.CENTER_LEFT);
        mainContent.getChildren().addAll(profileImageView, mainStats);

        StackPane cardContainer = new StackPane();

        cardContainer.setPrefWidth(ScreenManager.SCREEN_WIDTH / 4 - 28);
        cardContainer.setMaxWidth(ScreenManager.SCREEN_WIDTH / 4 - 28);
        cardContainer.setMinWidth(ScreenManager.SCREEN_WIDTH / 4 - 28);

        cardContainer.getChildren().add(mainContent);
        StackPane.setAlignment(mainContent, Pos.TOP_LEFT);

        cardContainer.getChildren().add(gamemodeImageView);
        StackPane.setAlignment(gamemodeImageView, Pos.TOP_RIGHT);

        StackPane.setMargin(gamemodeImageView, new Insets(4, 8, 0, 0));

        cardContainer.getChildren().add(backgroundRankLabel);
        StackPane.setAlignment(backgroundRankLabel, Pos.BOTTOM_RIGHT);

        StackPane.setMargin(backgroundRankLabel, new Insets(0, 8, 0, 0));

        this.setAlignment(Pos.CENTER_LEFT);
        this.setMinWidth(ScreenManager.SCREEN_WIDTH / 4 - 20);
        this.setPrefWidth(ScreenManager.SCREEN_WIDTH / 4 - 20);
        this.setMaxWidth(ScreenManager.SCREEN_WIDTH / 4 - 20);
        this.getChildren().add(cardContainer);
    }

    private void setupStyling() {
        this.getStyleClass().add("user-card");

        if (isSupporter) {
            this.getStyleClass().add("user-card-supporter");
        }

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("UserCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void setDefaultProfilePicture() {
        try {
            Image defaultImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/images/avatar-guest.png")).toExternalForm());
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default avatar: " + e.getMessage());
            profileImageView.setImage(null);
        }
    }

    private void setGamemodeIcon() {
        try {
            Image gamemodeImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/gamemode/osu-gamemode.png")).toExternalForm());
            gamemodeImageView.setImage(gamemodeImage);
            gamemodeImageView.setFitHeight(40);
            gamemodeImageView.setFitWidth(40);
        } catch (Exception e) {
            System.err.println("Could not load gamemode icon: " + e.getMessage());
            gamemodeImageView.setImage(null);
        }
    }

    public void updateUserInfo() {
        if (username != null) {
            usernameLabel.setText(username);
        }

        performanceLabel.setText("Performance: " + String.format("%,d", performance) + "pp");
        accuracyLabel.setText("Accuracy: " + String.format("%.2f", accuracy) + "%");
        playCountLabel.setText("Play Count: " + String.format("%,d", playCount) + " (Lv" + level + ")");

        updateProfilePicture();
    }

    public void updateProfilePicture() {
        if (profilePicture != null && profilePicture.length > 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(profilePicture);
                Image userImage = new Image(bis);
                profileImageView.setImage(userImage);
            } catch (Exception e) {
                System.err.println("Could not load user profile picture: " + e.getMessage());
                setDefaultProfilePicture();
            }
        } else {
            setDefaultProfilePicture();
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) {
            usernameLabel.setText(username != null ? username : "Guest");
        }
    }

    public void setPerformance(int performance) {
        this.performance = performance;
        if (performanceLabel != null) {
            performanceLabel.setText("Performance: " + String.format("%,d", performance) + "pp");
        }
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
        if (accuracyLabel != null) {
            accuracyLabel.setText("Accuracy: " + String.format("%.2f", accuracy) + "%");
        }
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
        if (playCountLabel != null) {
            playCountLabel.setText("Play Count: " + String.format("%,d", playCount) + " (Lv" + level + ")");
        }
    }

    public void setLevel(int level) {
        this.level = level;
        if (playCountLabel != null) {
            playCountLabel.setText("Play Count: " + String.format("%,d", playCount) + " (Lv" + level + ")");
        }
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
        updateProfilePicture();
    }

    public void setIsSupporter(boolean isSupporter) {
        this.isSupporter = isSupporter;
        updateSupporterStyling();
    }

    private void updateSupporterStyling() {
        this.getStyleClass().remove("user-card-supporter");

        if (isSupporter) {
            this.getStyleClass().add("user-card-supporter");
        }
    }

    public void setRank(int rank) {
        this.rank = rank;
        if (backgroundRankLabel != null) {
            backgroundRankLabel.setText("#" + rank);
        }
    }

    public void setBehavior(UserCardBehavior behavior) {
        if (this.behavior != null) {
            this.behavior.removeBehavior(this);
        }
        
        this.behavior = behavior;
        
        if (this.behavior != null) {
            this.behavior.setupBehavior(this);
        }
        
        updateHoverStyling();
    }

    private void updateHoverStyling() {
        this.getStyleClass().removeAll("user-card-hoverable", "user-card-static");
        
        if (behavior == UserCardBehavior.HOVER_TIME_COUNTRY) {
            this.getStyleClass().add("user-card-hoverable");
        } else if (behavior == UserCardBehavior.STATIC) {
            this.getStyleClass().add("user-card-static");
        }
    }

    public boolean isHovering() {
        return isHovering;
    }

    public void setHovering(boolean hovering) {
        this.isHovering = hovering;
    }
}
