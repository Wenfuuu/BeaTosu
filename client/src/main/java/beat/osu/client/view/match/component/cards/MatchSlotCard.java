package beat.osu.client.view.match.component.cards;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.LocaleManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;

public class MatchSlotCard extends HBox {

    private int matchPlayerId;
    private int matchId;

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

    public MatchSlotCard(int matchPlayerId, int matchId, UserDto user, PlayerRole role, PlayerStatus status, int matchSlotIndex) {
        this.matchPlayerId = matchPlayerId;
        this.matchId = matchId;
        this.user = user;
        this.role = role;
        this.status = status;
        this.matchSlotIndex = matchSlotIndex;

        if (user == null) {
            this.isLocked = false;
        } else {
            this.isLocked = true;
        }

        loadIcons();
        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-slot-card");

        slotIcon = new ImageView();

        if (isLocked) {
            slotIcon.setImage(lockedIcon);
        } else {
            slotIcon.setImage(unlockedIcon);
        }

        slotIcon.getStyleClass().add("slot-icon");

        separator = new VBox();
        separator.getStyleClass().add("separator");

        usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("username-label");

        rankLabel = new Label("#" + user.getRank());
        rankLabel.getStyleClass().add("rank-label");

        setupHoverPopup();
    }

    private void setupLayout() {
        slotIcon.setFitHeight(this.heightProperty().get() - 4);
        slotIcon.setPreserveRatio(true);

        VBox infoSpace = new VBox();
        HBox.setHgrow(infoSpace, Priority.ALWAYS);
        HBox infoBox = new HBox(usernameLabel, infoSpace, rankLabel);
        infoBox.setMinWidth(ScreenManager.SCREEN_WIDTH / 2.8);
        infoBox.setMaxWidth(ScreenManager.SCREEN_WIDTH / 2.8);
        infoBox.setPrefWidth(ScreenManager.SCREEN_WIDTH / 2.8);
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

        this.getChildren().addAll(slotIcon, separator, infoBox);
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
}
