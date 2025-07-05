package beat.osu.client.view.shared.bancho.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.LocaleManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.client.view.shared.bancho.cards.UserCardBehavior;
import beat.osu.client.view.shared.bancho.tabs.SortUserTabs;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.dto.user.events.UserCountChangedEvent;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class OnlineUsersPanel extends VBox {

    @FunctionalInterface
    public interface UserCardClickCallback {
        void onUserCardClicked(UserCard userCard);
    }

    private ArrayList<UserCard> userCards;
    private Map<Integer, UserCard> userCardMap;
    private FlowPane userCardsContainer;
    private ScrollPane scrollPane;

    private Label onlineUsersLabel;
    private Label titleLabel;
    private SortUserTabs sortUserTabs;
    private Label searchLabel;
    private TextField searchField;

    private ConnectedUsersController connectedUsersController;
    @Setter
    private UserCardClickCallback userCardClickCallback;

    public OnlineUsersPanel(ConnectedUsersController connectedUsersController) {
        super();
        this.getStyleClass().add("online-users-panel");
        this.setVisible(false);

        URL cssUrl = CssManager.getSharedCssURL("OnlineUsersPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.userCards = new ArrayList<>();
        this.userCardMap = new HashMap<>();

        titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("online-users-title");

        onlineUsersLabel = new Label("N/A Users Connected");
        onlineUsersLabel.getStyleClass().add("online-users-label");

        HBox searchSection = new HBox(10);
        searchSection.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(searchSection, new Insets(0, 0, 0, 200));

        searchLabel = new Label("Search:");
        searchLabel.getStyleClass().add("search-label");

        searchField = new TextField();
        searchField.getStyleClass().add("search-field");

        searchSection.getChildren().addAll(searchLabel, searchField);

        VBox topLeftSection = new VBox(0);
        topLeftSection.getChildren().addAll(titleLabel, onlineUsersLabel);

        HBox topSection = new HBox(0);
        topSection.getChildren().addAll(topLeftSection, searchSection);
        HBox.setMargin(searchSection, new Insets(20, 0, 0, 80));

        sortUserTabs = new SortUserTabs();
        sortUserTabs.setOnSelectionChanged(this::onSortTypeChanged);
        VBox.setMargin(sortUserTabs, new Insets(8, 0, 0, 0));

        userCardsContainer = new FlowPane();
        userCardsContainer.getStyleClass().add("user-cards-container");
        userCardsContainer.setHgap(5);
        userCardsContainer.setVgap(3);
        
        scrollPane = new ScrollPane(userCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("user-cards-scroll-pane");

        this.getChildren().addAll(topSection, sortUserTabs, scrollPane);

        this.connectedUsersController = connectedUsersController;
        setupUserCallbacks();
        setupUserCountSubscription();
        setupSearchListener();
        setupInputFieldSounds();
    }

    private void setupInputFieldSounds() {
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
        });
    }
    
    public void show() {
        if (this.getParent() instanceof VBox) {
            VBox parentContainer = (VBox) this.getParent();
            parentContainer.setVisible(true);
            parentContainer.setManaged(true);
            parentContainer.setMouseTransparent(false);
        }
        
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
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
            
            if (this.getParent() instanceof VBox) {
                VBox parentContainer = (VBox) this.getParent();
                boolean chatPanelVisible = false;
                
                for (Node child : parentContainer.getChildren()) {
                    if (child instanceof ChatPanel && child.isVisible()) {
                        chatPanelVisible = true;
                        break;
                    }
                }
                
                if (!chatPanelVisible) {
                    parentContainer.setVisible(false);
                    parentContainer.setManaged(false);
                    parentContainer.setMouseTransparent(true);
                }
            }
        });
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
            user.getLevel(),
            user.getExperience(),
            user.getRank(),
            user.isSupporter(),
            UserCardBehavior.HOVER_TIME_COUNTRY
        );
        
        userCard.setOnMouseClicked(event -> {
            if (userCardClickCallback != null) {
                SfxManager.playSfx("menuhit.wav");
                userCardClickCallback.onUserCardClicked(userCard);
            }
        });
        
        userCards.add(userCard);
        userCardMap.put(user.getId(), userCard);
        
        String searchText = searchField.getText();
        if (searchText == null) {
            searchText = "";
        }
        searchText = searchText.toLowerCase().trim();
        
        boolean matchesFilter = searchText.isEmpty() || 
                               (userCard.getUsername() != null && 
                                userCard.getUsername().toLowerCase().contains(searchText));
        
        if (matchesFilter) {
            String currentSortType = sortUserTabs.getSelectedSortType();
            if (currentSortType != null) {
                sortUserCards(currentSortType);
            } else {
                userCard.setOpacity(0);
                userCardsContainer.getChildren().add(userCard);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), userCard);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }
        } else {
            String currentSortType = sortUserTabs.getSelectedSortType();
            if (currentSortType != null) {
                sortUserCards(currentSortType);
            }
        }
    }
    
    private void removeUserCard(UserDto user) {
        UserCard userCard = userCardMap.get(user.getId());
        if (userCard == null) {
            return;
        }
        
        userCards.remove(userCard);
        userCardMap.remove(user.getId());
        
        if (userCardsContainer.getChildren().contains(userCard)) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), userCard);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> userCardsContainer.getChildren().remove(userCard));
            fadeOut.play();
        }
    }

    private void updateUserCountLabel(UserCountChangedEvent event) {
        int userCount = event.getUserCount();
        Platform.runLater(() -> {
            onlineUsersLabel.setText(userCount + " Users Connected");
        });
    }
    
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::filterAndDisplayUsers);
        });
    }
    
    private void filterAndDisplayUsers() {
        if (userCards == null || userCards.isEmpty()) {
            return;
        }
        
        String searchText = searchField.getText();
        if (searchText == null) {
            searchText = "";
        }
        searchText = searchText.toLowerCase().trim();
        userCardsContainer.getChildren().clear();
        
        List<UserCard> filteredCards = new ArrayList<>();
        for (UserCard card : userCards) {
            String username = card.getUsername();
            if (username != null && username.toLowerCase().contains(searchText)) {
                filteredCards.add(card);
            }
        }
        
        addCardsWithAnimation(filteredCards);
    }
    
    private void addCardsWithAnimation(List<UserCard> cardsToAdd) {
        for (int i = 0; i < cardsToAdd.size(); i++) {
            UserCard card = cardsToAdd.get(i);
            card.setOpacity(0);
            userCardsContainer.getChildren().add(card);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(i * 50));
            fadeIn.play();
        }
    }
    
    public boolean isShowing() {
        return this.isVisible();
    }
    
    private void onSortTypeChanged(String sortType) {
        Platform.runLater(() -> {
            sortUserCards(sortType);
        });
    }
    
    private void sortUserCards(String sortType) {
        if (userCards == null || userCards.isEmpty()) {
            return;
        }
        
        Comparator<UserCard> comparator = null;
        
        switch (sortType) {
            case "Name":
                comparator = Comparator.comparing(card -> card.getUsername().toLowerCase());
                break;
            case "Rank":
                comparator = Comparator.comparingInt(UserCard::getRank);
                break;
            case "Location":
                comparator = Comparator.comparing(card -> {
                    String countryName = LocaleManager.getCountryName(card.getCountryCode());
                    return countryName.toLowerCase();
                });
                break;
            case "Time Zone":
                comparator = Comparator.comparing(card -> {
                    String timezone = LocaleManager.getTimezone(card.getCountryCode());
                    return timezone != null ? timezone : "zzz";
                });
                break;
            default:
                comparator = Comparator.comparing(card -> card.getUsername().toLowerCase());
                break;
        }

        userCards.sort(comparator);
        filterAndDisplayUsers();
    }
}
