package beat.osu.client.view.landing.component.bancho.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.bancho.SelectChannelModal;
import beat.osu.client.view.landing.component.bancho.buttons.AddChatButton;
import beat.osu.client.view.landing.component.bancho.buttons.BanchoButtons;
import beat.osu.client.view.landing.component.bancho.buttons.ChatTabButton;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.events.UserJoinedChannelEvent;
import beat.osu.shared.dto.chat.events.UserLeftChannelEvent;
import beat.osu.shared.dto.chat.responses.GetJoinedChannelsResponse;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChatPanel extends VBox {

    private ChannelController channelController;
    private SelectChannelModal selectChannelModal;
    private OnlineUsersPanel onlineUsersPanel;
    private BanchoButtons banchoButtons;

    private List<ChannelDto> joinedChannels;
    private HBox channelTabsContainer;
    private ChannelDto currentSelectedChannel;
    private AddChatButton addChatButton;

    public ChatPanel(ChannelController channelController, SelectChannelModal selectChannelModal, OnlineUsersPanel onlineUsersPanel, BanchoButtons banchoButtons) {
        super();
        this.channelController = channelController;
        this.selectChannelModal = selectChannelModal;
        this.onlineUsersPanel = onlineUsersPanel;
        this.banchoButtons = banchoButtons;

        this.joinedChannels = new ArrayList<>();

        this.getStyleClass().add("chat-panel");
        this.setVisible(false);

        URL cssUrl = CssManager.getLandingCssURL("ChatPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.35);

        setupEventHandlers();
        setupUI();
    }

    public void show() {
        loadJoinedChannels();

        this.setVisible(true);
        this.setTranslateY(this.getHeight());
        this.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), this);
        slideIn.setFromY(this.getHeight() / 4);
        slideIn.setToY(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);
        showTransition.play();
    }

    public void hide() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), this);
        slideOut.setFromY(0);
        slideOut.setToY(this.getHeight() / 4);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hideTransition = new ParallelTransition(slideOut, fadeOut);
        hideTransition.setOnFinished(e -> this.setVisible(false));
        hideTransition.play();
    }

    private void loadJoinedChannels() {
        try {
            Result<GetJoinedChannelsResponse> result = channelController.getJoinedChannels().get();
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    GetJoinedChannelsResponse response = result.getValue();
                    this.joinedChannels = response.getChannels();
                    refreshChannelDisplay();
                } else {
                    System.out.println(result.getError().getMessage());
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUserJoinedChannel(UserJoinedChannelEvent event) {
        Platform.runLater(() -> {
            if (event.getUserId() == AuthManager.getUser().getId()) {
                boolean channelExists = joinedChannels.stream()
                        .anyMatch(channel -> channel.getId() == event.getChannelId());
                
                if (!channelExists) {
                    loadJoinedChannels();
                }
            } else {
                ChannelDto targetChannel = joinedChannels.stream()
                        .filter(channel -> channel.getId() == event.getChannelId())
                        .findFirst()
                        .orElse(null);

                if (targetChannel != null) {
                    refreshChannelDisplay();
                }
            }
        });
    }

    private void handleUserLeftChannel(UserLeftChannelEvent event) {
        Platform.runLater(() -> {
            if (event.getUserId() == AuthManager.getUser().getId()) {
                joinedChannels.removeIf(channel -> channel.getId() == event.getChannelId());
                
                if (currentSelectedChannel != null && currentSelectedChannel.getId() == event.getChannelId()) {
                    currentSelectedChannel = null;
                    if (!joinedChannels.isEmpty()) {
                        selectChannel(joinedChannels.get(0));
                    }
                }
                
                refreshChannelDisplay();
            } else {
                ChannelDto targetChannel = joinedChannels.stream()
                        .filter(channel -> channel.getId() == event.getChannelId())
                        .findFirst()
                        .orElse(null);

                if (targetChannel != null) {
                    refreshChannelDisplay();
                }
            }
        });
    }

    public boolean isShowing() {
        return this.isVisible();
    }

    private void setupEventHandlers() {
//        channelController.addChannelMessageCallback(this::handleChannelMessage);
        channelController.addUserJoinedChannelCallback(this::handleUserJoinedChannel);
        channelController.addUserLeftChannelCallback(this::handleUserLeftChannel);
    }

    private void setupUI() {
        addChatButton = new AddChatButton();
        addChatButton.setOnAction(e -> openChannelSelectionModal());

        channelTabsContainer = new HBox();
        channelTabsContainer.getStyleClass().add("channel-tabs");
        channelTabsContainer.getChildren().add(addChatButton);

        this.getChildren().addAll(channelTabsContainer);

        refreshChannelDisplay();
    }

    private void refreshChannelDisplay() {
        if (joinedChannels == null) return;

        channelTabsContainer.getChildren().clear();
        channelTabsContainer.getChildren().add(addChatButton);

        for (ChannelDto channel : joinedChannels) {
            ChatTabButton tabButton = new ChatTabButton(channel.getName());
            tabButton.setOnAction(e -> selectChannel(channel));
            tabButton.setOnCloseAction(this::closeChannelTab);

            if (currentSelectedChannel != null && currentSelectedChannel.getId() == channel.getId()) {
                tabButton.setSelected(true);
            }

            channelTabsContainer.getChildren().add(channelTabsContainer.getChildren().size() - 1, tabButton);
        }

        if (currentSelectedChannel == null && !joinedChannels.isEmpty()) {
            selectChannel(joinedChannels.get(0));
        }
    }

    private void selectChannel(ChannelDto channel) {
        currentSelectedChannel = channel;

        for (Node node : channelTabsContainer.getChildren()) {
            if (node instanceof ChatTabButton) {
                ChatTabButton tab = (ChatTabButton) node;
                tab.setSelected(tab.getTabText().equals(channel.getName()));
            }
        }
    }

    private void closeChannelTab(ChatTabButton tabButton) {
        ChannelDto channelToLeave = joinedChannels.stream()
                .filter(channel -> channel.getName().equals(tabButton.getTabText()))
                .findFirst()
                .orElse(null);

        if (channelToLeave != null) {
            channelController.leaveChannel(channelToLeave.getId())
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            if (result.isSuccess()) {
                                joinedChannels.remove(channelToLeave);
                                
                                if (currentSelectedChannel != null && currentSelectedChannel.getId() == channelToLeave.getId()) {
                                    currentSelectedChannel = null;
                                    if (!joinedChannels.isEmpty()) {
                                        selectChannel(joinedChannels.get(0));
                                    }
                                }
                                
                                refreshChannelDisplay();
                            } else {
                                System.err.println("Failed to leave channel: " + result.getError().getMessage());
                            }
                        });
                    });
        }
    }

    private void openChannelSelectionModal() {
        if (selectChannelModal != null) {
            selectChannelModal.show();
        }
    }
}