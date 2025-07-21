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
import lombok.Getter;

public class MatchPlayerCard extends VBox {

    private Integer matchPlayerId;
    private Integer matchId;

    @Getter
    private Integer userId;
    @Getter
    private String username;
    private int rank;
    private String country;
    private byte[] profilePicture;

    private boolean isHost;

    private ImageView profileImageView;

    public MatchPlayerCard(Integer matchPlayerId, Integer matchId, Integer userId, String username,
                           int rank, String country, byte[] profilePicture, boolean isHost) {
        this.matchPlayerId = matchPlayerId;
        this.matchId = matchId;
        this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.country = country;
        this.profilePicture = profilePicture;
        this.isHost = isHost;

        if (isHost) {
            this.getStyleClass().add("host");
        }

        initializeComponents();
        setupLayout();
        setupStyling();
        updateUserInfo();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-player-card");

        profileImageView = new ImageView();
        profileImageView.getStyleClass().add("profile-picture");
        profileImageView.setSmooth(true);
        profileImageView.setCache(true);

        if (isHost) {
            profileImageView.setFitWidth(96);
            profileImageView.setFitHeight(96);
        } else {
            profileImageView.setFitWidth(54);
            profileImageView.setFitHeight(54);
        }

         if (username == null) {
            this.getStyleClass().add("unlocked");
        } else {
            this.getStyleClass().add("locked");
             setDefaultProfilePicture();
        }

        setupHoverPopup();
    }

    private void setupLayout() {
        this.getChildren().add(profileImageView);
    }

    private void setupStyling() {
        URL cssUrl = CssManager.getLobbyCssURL("MatchPlayerCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    private void updateUserInfo() {
        updateProfilePicture();
    }

    private void setDefaultProfilePicture() {
        try {
            Image defaultImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/images/misc/avatar-guest.png")).toExternalForm());
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
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
                    setDefaultProfilePicture();
                }
            } else {
                setDefaultProfilePicture();
            }
        }
    }
    
    private void setupHoverPopup() {
        if (username == null) {
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
                           byte[] profilePicture) {
        this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.country = country;
        this.profilePicture = profilePicture;

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

         if (username == null) {
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