package beat.osu.client.view.match.component.layout;

import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.shared.dto.user.UserDto;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;

public class TopBar extends StackPane {

    private Label titleLabel;
    private Label subTitleLabel;
    private UserCard userCard;

    public TopBar() {
        super();
        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        this.getStyleClass().add("top-bar");

        titleLabel = new Label("Match Setup");
        titleLabel.getStyleClass().add("title-label");

        subTitleLabel = new Label("You are a player.");
        subTitleLabel.getStyleClass().add("subtitle-label");

        UserDto user = AuthManager.getUser();
        userCard = new UserCard(
                user.getId(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfilePicture(),
                user.getPerformance(),
                user.getAccuracy(),
                user.getPlayCount(),
                user.getLevel(),
                user.getExperience(),
                user.getRank(),
                user.isSupporter(),
                UserCardBehavior.STATIC
        );
        userCard.updateUserInfo();
    }

    private void setupLayout() {
        VBox titlesContainer = new VBox(titleLabel, subTitleLabel);
        titlesContainer.getStyleClass().add("titles-container");

        this.getChildren().addAll(titlesContainer, userCard);
        StackPane.setAlignment(userCard, Pos.CENTER);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getMatchCssURL("TopBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    public void updateSubtitle(boolean isHost) {
        if (isHost) {
            subTitleLabel.setText("You are the host!");
        } else {
            subTitleLabel.setText("You are a player.");
        }
    }
}
