package beat.osu.client.view.landing.component.bancho.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.bancho.UserCard;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.dto.user.events.UserCountChangedEvent;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class OnlineUsersPanel extends VBox {

    private ArrayList<UserCard> userCards;
    private Map<Integer, UserCard> userCardMap;
    private FlowPane userCardsContainer;
    private ScrollPane scrollPane;

    private Label onlineUsersLabel;
    private Label titleLabel;
    private SortUserTabs sortUserTabs;
    private ConnectedUsersController connectedUsersController;

    public OnlineUsersPanel(ConnectedUsersController connectedUsersController) {
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
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.65);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.65);

        this.userCards = new ArrayList<>();
        this.userCardMap = new HashMap<>();

        titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("online-users-title");

        onlineUsersLabel = new Label("N/A Users Connected");
        onlineUsersLabel.getStyleClass().add("online-users-label");

        sortUserTabs = new SortUserTabs();

        userCardsContainer = new FlowPane();
        userCardsContainer.getStyleClass().add("user-cards-container");
        userCardsContainer.setHgap(5);
        userCardsContainer.setVgap(3);
        
        scrollPane = new ScrollPane(userCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("user-cards-scroll-pane");

        this.getChildren().addAll(titleLabel, onlineUsersLabel, sortUserTabs, scrollPane);

        this.connectedUsersController = connectedUsersController;
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
        connectedUsersController.addUserCountChangedCallback(this::updateUserCountLabel);
    }

    private void setupUserCallbacks() {
        connectedUsersController.addUserConnectedCallback(this::onUserJoined);
        connectedUsersController.addUserDisconnectedCallback(this::onUserLeft);
        
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
    
    private void onUserJoined(UserConnectedEvent event) {
        UserDto userDto = event.getUserDto();
        Platform.runLater(() -> addUserCard(userDto));
    }
    
    private void onUserLeft(UserDisconnectedEvent event) {
        UserDto userDto = event.getUserDto();
        Platform.runLater(() -> removeUserCard(userDto));
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
        userCardsContainer.getChildren().add(userCard);
        
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
        fadeOut.setOnFinished(e -> userCardsContainer.getChildren().remove(userCard));
        fadeOut.play();
    }

    private void updateUserCountLabel(UserCountChangedEvent event) {
        int userCount = event.getUserCount();
        Platform.runLater(() -> {
            onlineUsersLabel.setText(userCount + " Users Connected");
        });
    }
    
    public boolean isShowing() {
        return this.isVisible();
    }
}
