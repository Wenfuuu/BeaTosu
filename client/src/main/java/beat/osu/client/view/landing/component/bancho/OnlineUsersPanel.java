package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineUsersPanel extends VBox {

    private ArrayList<UserCard> userCards;
    private Map<Integer, UserCard> userCardMap;

    private Label onlineUsersLabel;
    private Label titleLabel;
    private ConnectedUsersController connectedUsersController;

    public OnlineUsersPanel() {
        super();
        this.getStyleClass().add("online-users-panel");
        this.setVisible(false);

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("OnlineUsersPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.65);

        this.userCards = new ArrayList<>();
        this.userCardMap = new HashMap<>();

        titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("online-users-title");

        this.connectedUsersController = new ConnectedUsersController();

        onlineUsersLabel = new Label("N/A Users Connected");
        onlineUsersLabel.getStyleClass().add("online-users-label");

        this.getChildren().addAll(titleLabel, onlineUsersLabel);

        setupUserCallbacks();
        setupUserCountSubscription();
    }
    
    public void show() {
        this.setVisible(true);
        this.setOpacity(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> this.setVisible(false));
        fadeOut.play();
    }

    private void setupUserCountSubscription() {
        connectedUsersController.addUserCountCallback(this::updateUserCountLabel);
    }

    private void setupUserCallbacks() {
        connectedUsersController.addUserJoinedCallback(this::onUserJoined);
        connectedUsersController.addUserLeftCallback(this::onUserLeft);
        
        loadInitialUsers();
    }
    
    private void loadInitialUsers() {
        Platform.runLater(() -> {
            List<UserDto> connectedUsers = connectedUsersController.getConnectedUsers();
            for (UserDto user : connectedUsers) {
                addUserCard(user);
            }
        });
    }
    
    private void onUserJoined(UserDto user) {
        Platform.runLater(() -> addUserCard(user));
    }
    
    private void onUserLeft(UserDto user) {
        Platform.runLater(() -> removeUserCard(user));
    }
    
    private void addUserCard(UserDto user) {
        if (userCardMap.containsKey(user.getId())) {
            return;
        }
        
        UserCard userCard = new UserCard(
            user.getId(),
            user.getUsername(),
            user.getCountryCode(),
            user.getProfilePicture(),
            user.getPerformance(),
            user.getAccuracy(),
            user.getPlayCount(),
            user.getLevel()
        );
        
        userCards.add(userCard);
        userCardMap.put(user.getId(), userCard);
        
        userCard.setOpacity(0);
        this.getChildren().add(userCard);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), userCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    private void removeUserCard(UserDto user) {
        UserCard userCard = userCardMap.get(user.getId());
        if (userCard == null) {
            return;
        }
        
        userCards.remove(userCard);
        userCardMap.remove(user.getId());
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), userCard);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> this.getChildren().remove(userCard));
        fadeOut.play();
    }

    private void updateUserCountLabel(Integer userCount) {
        Platform.runLater(() -> {
            if (userCount != null) {
                onlineUsersLabel.setText(userCount + " Users Connected");
            } else {
                onlineUsersLabel.setText("N/A Users Connected");
            }
        });
    }
    
    public boolean isShowing() {
        return this.isVisible();
    }
}
