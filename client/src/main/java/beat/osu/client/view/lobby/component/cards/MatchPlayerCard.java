package beat.osu.client.view.lobby.component.cards;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MatchPlayerCard extends VBox {

    private Integer matchPlayerId;
    private Integer matchId;

    private Integer userId;
    private String username;
    private int rank;
    private String country;
    private byte[] profilePicture;

    private boolean isActive;

    private ImageView profileImageView;

    public MatchPlayerCard(Integer matchPlayerId, Integer matchId, Integer userId, String username,
                           int rank, String country, byte[] profilePicture, boolean isActive) {
        this.matchPlayerId = matchPlayerId;
        this.matchId = matchId;
        this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.country = country;
        this.profilePicture = profilePicture;
        this.isActive = isActive;

        initializeComponents();
        setupLayout();
        setupStyling();
        updateUserInfo();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-player-card");

        if (!isActive) {
            this.getStyleClass().add("inactive");
        } else if (username == null) {
            this.getStyleClass().add("locked");
        } else {
            this.getStyleClass().add("unlocked");
        }

        profileImageView = new ImageView();
        profileImageView.getStyleClass().add("profile-picture");
        profileImageView.setFitWidth(54);
        profileImageView.setFitHeight(54);
        profileImageView.setSmooth(true);
        profileImageView.setCache(true);
        setDefaultProfilePicture();
        
        setupHoverPopup();
    }

    private void setupLayout() {
        this.getChildren().add(profileImageView);
    }

    private void setupStyling() {
        URL cssUrl = CssManager.getLobbyCssURL("MatchPlayerCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void updateUserInfo() {
        updateProfilePicture();
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

    public void updateProfilePicture() {
        if (username != null) {
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
    }
    
    private void setupHoverPopup() {
        if (!isActive || username == null) {
            return;
        }
        
        String tooltipText = username + "  (#" + rank + ")\n" + (country != null ? country : "Unknown");

        Tooltip playerTooltip = new Tooltip(tooltipText);
        playerTooltip.getStyleClass().add("player-tooltip");

        playerTooltip.setShowDelay(Duration.millis(100));
        playerTooltip.setHideDelay(Duration.millis(100));

        Tooltip.install(this, playerTooltip);
    }

    public void updateCard(Integer userId, String username, int rank, String country,
                           byte[] profilePicture, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.country = country;
        this.profilePicture = profilePicture;
        this.isActive = isActive;

        refreshCard();
    }

    private void refreshCard() {
        Tooltip.uninstall(this, null);
        refreshActiveStatus();
        refreshUserComponents();
    }

    private void refreshUserComponents() {
        updateProfilePicture();
        refreshTooltip();
    }

    private void refreshActiveStatus() {
        this.getStyleClass().removeAll("inactive", "locked", "unlocked");

        if (!isActive) {
            this.getStyleClass().add("inactive");
        } else if (username == null) {
            this.getStyleClass().add("locked");
        } else {
            this.getStyleClass().add("unlocked");
        }

        refreshTooltip();
    }

    private void refreshTooltip() {
        Tooltip.uninstall(this, null);
        setupHoverPopup();
    }
}