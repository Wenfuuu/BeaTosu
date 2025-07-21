package beat.osu.client.view.shared.bancho.modals;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.shared.bancho.buttons.BanchoButtons;
import beat.osu.client.view.shared.bancho.cards.ChannelCard;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.landing.component.layout.BottomBar;
import beat.osu.client.view.landing.component.layout.TopBar;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.events.UserJoinedChannelEvent;
import beat.osu.shared.dto.chat.events.UserLeftChannelEvent;
import beat.osu.shared.dto.chat.responses.GetAllChannelsResponse;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class SelectChannelModal extends VBox {

    private Label selectChannelTitle;
    private Label searchLabel;
    private TextField searchField;
    private Button closeButton;

    private ScrollPane scrollPane;
    private VBox channelContainer;
    private List<ChannelCard> channelCards;
    
    private ChannelController channelController;
    private List<ChannelDto> allChannels;

    @Setter
    private OnlineUsersPanel onlineUsersPanel;
    @Setter
    private ChatPanel chatPanel;
    private BanchoButtons banchoButtons;

    public SelectChannelModal(ChannelController channelController, BanchoButtons banchoButtons) {
        this.channelController = channelController;
        this.banchoButtons = banchoButtons;

        this.channelCards = new ArrayList<>();
        this.allChannels = new ArrayList<>();
        
        initializeUI();
        loadStyles();

        setupChannelCallbacks();
        setupInputFieldSounds();
    }

    private void setupInputFieldSounds() {
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playMenuSfx(SfxType.KEY_DELETE);
            } else {
                SfxManager.playMenuSfx(SfxType.KEY_PRESS);
            }
        });
    }

    private void initializeUI() {
        this.setAlignment(Pos.TOP_LEFT);
        this.setSpacing(15);
        this.getStyleClass().add("select-channel-modal");

        selectChannelTitle = new Label("Select any channel you wish to join!");
        selectChannelTitle.getStyleClass().add("modal-title");
        VBox.setMargin(selectChannelTitle, new Insets(0, 0, 0, 0));

        HBox searchSection = new HBox(10);
        searchSection.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(searchSection, new Insets(0, 0, 0, 200));

        searchLabel = new Label("Search:");
        searchLabel.getStyleClass().add("search-label");

        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterChannels(newValue));
        
        searchSection.getChildren().addAll(searchLabel, searchField);

        channelContainer = new VBox(8);
        channelContainer.setPadding(new Insets(10));
        channelContainer.setAlignment(Pos.TOP_CENTER);
        channelContainer.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.56);
        channelContainer.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.56);
        
        scrollPane = new ScrollPane(channelContainer);
        scrollPane.getStyleClass().add("channel-scroll-pane");
        scrollPane.setFitToWidth(false); 
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.56 + 50);
        scrollPane.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.56 + 50);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        HBox scrollContainer = new HBox();
        scrollContainer.setAlignment(Pos.CENTER);
        scrollContainer.getChildren().add(scrollPane);

        closeButton = new Button("Close");
        closeButton.getStyleClass().add("close-button");
        closeButton.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.56 + 30);
        closeButton.setOnMouseClicked(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_BACK);
            this.hide();
            banchoButtons.show();

            if (banchoButtons.getOnlineUsersButton().isOnlineUserShown()) {
                onlineUsersPanel.show();
            }

            chatPanel.show();
        });

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().add(closeButton);
        HBox.setMargin(closeButton, new Insets(20, 0, 80, 0));

        this.getChildren().addAll(selectChannelTitle, searchSection, scrollContainer, buttonContainer);
        this.setVisible(false);
    }

    private void loadChannels() {
        try {
            Result<GetAllChannelsResponse> result = channelController.getAllChannels().get();
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    GetAllChannelsResponse response = result.getValue();
                    this.allChannels = response.getChannels();
                    displayChannels(this.allChannels);
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayChannels(List<ChannelDto> channels) {
        channelCards.clear();
        channelContainer.getChildren().clear();

        for (ChannelDto channel : channels) {
            ChannelCard card = new ChannelCard(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getMemberCount(),
                channel.isJoined()
            );
            
            card.setOnMouseClicked(e -> {
                SfxManager.playMenuSfx(SfxType.MENU_HIT);
                handleChannelClick(card, channel);
            });
            card.setOnMouseEntered(e -> {
                SfxManager.playMenuSfx(SfxType.MENU_HOVER);
            });
            
            channelCards.add(card);
            channelContainer.getChildren().add(card);
        }
    }

    private void handleChannelClick(ChannelCard card, ChannelDto channel) {
        if (!card.isJoined()) {
            channelController.joinChannel(channel.getId()).thenAccept(result -> {
                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        // System.out.println("Joined channel: " + channel.getName());
                    }
                });
            });
        }
    }

    private void refreshChannelDisplay() {
        String currentSearchText = searchField.getText();
        if (currentSearchText.isEmpty()) {
            displayChannels(allChannels);
        } else {
            filterChannels(currentSearchText);
        }
    }

    private void filterChannels(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            displayChannels(allChannels);
            return;
        }

        String lowerCaseSearch = searchText.toLowerCase().trim();
        List<ChannelDto> filteredChannels = allChannels.stream()
            .filter(channel -> 
                channel.getName().toLowerCase().contains(lowerCaseSearch) ||
                channel.getDescription().toLowerCase().contains(lowerCaseSearch))
            .collect(Collectors.toList());

        displayChannels(filteredChannels);
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
        });

        fadeOut.play();
    }

    public void show() {
        this.loadChannels();
        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getSharedCssURL("SelectChannelModal.css");

        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void setupChannelCallbacks() {
        channelController.addUserJoinedChannelCallback(this::handleUserJoinedChannel);
        channelController.addUserLeftChannelCallback(this::handleUserLeftChannel);
    }

    private void handleUserJoinedChannel(UserJoinedChannelEvent event) {
        Platform.runLater(() -> {
            ChannelDto targetChannel = allChannels.stream()
                .filter(channel -> channel.getId() == event.getChannel().getId())
                .findFirst()
                .orElse(null);

            if (targetChannel != null) {
                targetChannel.setMemberCount(targetChannel.getMemberCount() + 1);

                if (event.getUserId() == AuthManager.getUser().getId()) {
                    targetChannel.setJoined(true);
                }
                refreshChannelDisplay();
            }
        });
    }

    private void handleUserLeftChannel(UserLeftChannelEvent event) {
        Platform.runLater(() -> {
            ChannelDto targetChannel = allChannels.stream()
                .filter(channel -> channel.getId() == event.getChannelId())
                .findFirst()
                .orElse(null);
            
            if (targetChannel != null) {
                targetChannel.setMemberCount(Math.max(0, targetChannel.getMemberCount() - 1));

                if (event.getUserId() == AuthManager.getUser().getId()) {
                    targetChannel.setJoined(false);
                }
                refreshChannelDisplay();
            }
        });
    }

    public void cleanup() {
        if (channelController != null) {
            channelController.removeUserJoinedChannelCallback(this::handleUserJoinedChannel);
            channelController.removeUserLeftChannelCallback(this::handleUserLeftChannel);
        }
    }
}