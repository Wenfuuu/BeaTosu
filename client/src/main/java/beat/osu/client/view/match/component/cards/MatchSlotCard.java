package beat.osu.client.view.match.component.cards;

import java.net.URL;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.LocaleManager;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

public class MatchSlotCard extends HBox {

    private int matchPlayerId;
    private int matchId;

    @Getter
    private UserDto user;

    private PlayerRole role;
    private PlayerStatus status;

    private int matchSlotIndex;

    private boolean isLocked;

    private Image lockedIcon;
    private Image unlockedIcon;
    private Image hostIcon;

    private ImageView slotIcon;
    private VBox separator;
    private Label usernameLabel;
    private Label rankLabel;

    @FunctionalInterface
    public interface SlotCardClickCallback {
        void onSlotCardClicked(MatchSlotCard card);
    }

    @Setter
    private SlotCardClickCallback slotCardClickCallback;

    public MatchSlotCard(int matchPlayerId, int matchId, UserDto user, PlayerRole role, PlayerStatus status, int matchSlotIndex) {
        this.matchPlayerId = matchPlayerId;
        this.matchId = matchId;
        this.user = user;
        this.role = role;
        this.status = status;
        this.matchSlotIndex = matchSlotIndex;

        this.isLocked = user != null;

        loadIcons();
        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-slot-card");

        slotIcon = new ImageView();

        if (role == PlayerRole.HOST) {
            slotIcon.setImage(hostIcon);
        } else if (isLocked) {
            slotIcon.setImage(lockedIcon);
        } else {
            slotIcon.setImage(unlockedIcon);
        }

        separator = new VBox();
        separator.getStyleClass().add("separator");

        usernameLabel = new Label(user != null ? user.getUsername() : "");
        usernameLabel.getStyleClass().add("username-label");

        rankLabel = new Label(user != null ? "#" + user.getRank() : "");
        rankLabel.getStyleClass().add("rank-label");

        setupHoverPopup();
        setupCallbacks();
    }

    private void setupLayout() {
        slotIcon.setFitHeight(this.heightProperty().get() - 2);
        slotIcon.setPreserveRatio(true);

        HBox iconContainer = new HBox(slotIcon);
        iconContainer.setMinWidth(42);
        iconContainer.setMaxWidth(42);
        iconContainer.setPrefWidth(42);
        iconContainer.setAlignment(Pos.CENTER);

        VBox infoSpace = new VBox();
        HBox.setHgrow(infoSpace, Priority.ALWAYS);
        HBox infoBox = new HBox(usernameLabel, infoSpace, rankLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        infoBox.getStyleClass().add("info-box");

        if (isLocked) {
            infoBox.getStyleClass().add("locked");
        } else {
            infoBox.getStyleClass().add("unlocked");
        }

        separator.minHeightProperty().bind(this.heightProperty());
        separator.prefHeightProperty().bind(this.heightProperty());
        separator.maxHeightProperty().bind(this.heightProperty());
        HBox.setMargin(separator, new Insets(0, 12, 0, 18));

        this.getChildren().addAll(iconContainer, separator, infoBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getMatchCssURL("MatchSlotCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void loadIcons() {
        lockedIcon = new Image(Objects.requireNonNull(Main.class.getResource("/assets/match/locked.png")).toExternalForm());
        unlockedIcon = new Image(Objects.requireNonNull(Main.class.getResource("/assets/match/unlocked.png")).toExternalForm());
        hostIcon = new Image(Objects.requireNonNull(Main.class.getResource("/assets/match/host.png")).toExternalForm());
    }

    private void setupHoverPopup() {
        if (user == null) {
            return;
        }

        String countryName = LocaleManager.getCountryName(user.getCountryCode());
        String tooltipText = "Level:  " + user.getLevel() + "\nAccuracy:  " + user.getAccuracy() + "%\nLocation:  " + countryName + "\n";

        Tooltip playerTooltip = new Tooltip(tooltipText);
        playerTooltip.getStyleClass().add("player-tooltip");

        playerTooltip.setShowDelay(Duration.millis(100));
        playerTooltip.setHideDelay(Duration.millis(100));

        Tooltip.install(this, playerTooltip);
    }

    private void setupCallbacks() {
        this.setOnMouseClicked(event -> {
            if (user != null && slotCardClickCallback != null) {
                slotCardClickCallback.onSlotCardClicked(this);
            }
        });
    }

    public void updateCard(UserDto user, PlayerRole role, PlayerStatus status) {
        this.user = user;
        this.role = role;
        this.status = status;
        this.isLocked = user != null;

        refreshCard();
    }

    private void refreshCard() {
        Tooltip.uninstall(this, null);
        refreshSlotStatus();
        refreshLabels();
        refreshTooltip();
    }

    private void refreshSlotStatus() {
        this.getStyleClass().removeAll("locked", "unlocked", "host");

        if (role == PlayerRole.HOST) {
            slotIcon.setImage(hostIcon);
            this.getStyleClass().add("host");
        } else if (isLocked) {
            slotIcon.setImage(lockedIcon);
            this.getStyleClass().add("locked");
        } else {
            slotIcon.setImage(unlockedIcon);
            this.getStyleClass().add("unlocked");
        }

        HBox infoBox = (HBox) this.getChildren().get(2);
        infoBox.getStyleClass().removeAll("locked", "unlocked");

        if (isLocked) {
            infoBox.getStyleClass().add("locked");
        } else {
            infoBox.getStyleClass().add("unlocked");
        }
    }

    private void refreshLabels() {
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            rankLabel.setText("#" + user.getRank());
        } else {
            usernameLabel.setText("");
            rankLabel.setText("");
        }
    }

    private void refreshTooltip() {
        Tooltip.uninstall(this, null);
        setupHoverPopup();
    }
}