package beat.osu.client.view.landing.component.bancho.panels;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.bancho.SelectChannelModal;
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
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChatPanel extends VBox {

    private ChannelController channelController;
    private SelectChannelModal selectChannelModal;
    private OnlineUsersPanel onlineUsersPanel;
    private BanchoButtons banchoButtons;

    private ChatTabs chatTabs;

    public ChatPanel(ChannelController channelController, SelectChannelModal selectChannelModal, OnlineUsersPanel onlineUsersPanel, BanchoButtons banchoButtons) {
        super();
        this.channelController = channelController;
        this.selectChannelModal = selectChannelModal;
        this.onlineUsersPanel = onlineUsersPanel;
        this.banchoButtons = banchoButtons;

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
                    chatTabs.setJoinedChannels(response.getChannels());
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
                chatTabs.addChannel(event.getChannel());
            }
        });
    }

    private void handleUserLeftChannel(UserLeftChannelEvent event) {
        Platform.runLater(() -> {
            if (event.getUserId() == AuthManager.getUser().getId()) {
                chatTabs.removeChannel(event.getChannelId());
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
        // Initialize the chat tabs component
        chatTabs = new ChatTabs();
        
        // Set up callbacks for chat tabs
        chatTabs.setOnChannelSelected(this::onChannelSelected);
        chatTabs.setOnChannelClosed(this::handleChannelClose);
        chatTabs.setOnAddChannelRequested(this::openChannelSelectionModal);
        
        this.getChildren().add(chatTabs);
    }
    
    private void onChannelSelected(ChannelDto channel) {
        System.out.println("Selected channel: " + channel.getName());
    }
    
    private void handleChannelClose(ChatTabButton tabButton) {
        List<ChannelDto> joinedChannels = chatTabs.getJoinedChannels();
        ChannelDto channelToLeave = joinedChannels.stream()
                .filter(channel -> channel.getName().equals(tabButton.getTabText()))
                .findFirst()
                .orElse(null);

        if (channelToLeave != null) {
            channelController.leaveChannel(channelToLeave.getId())
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            if (result.isSuccess()) {
                                chatTabs.removeChannel(channelToLeave.getId());
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