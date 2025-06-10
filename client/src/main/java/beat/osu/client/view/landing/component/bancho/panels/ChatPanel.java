package beat.osu.client.view.landing.component.bancho.panels;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.controller.PrivateChatController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.bancho.buttons.BanchoButtons;
import beat.osu.client.view.landing.component.bancho.buttons.ChatTabButton;
import beat.osu.client.view.landing.component.bancho.modals.SelectChannelModal;
import beat.osu.client.view.landing.component.bancho.tabs.ChatTabs;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.ChannelMessageDto;
import beat.osu.shared.dto.chat.PrivateChatDto;
import beat.osu.shared.dto.chat.PrivateChatMessageDto;
import beat.osu.shared.dto.chat.events.ChannelMessageEvent;
import beat.osu.shared.dto.chat.events.PrivateChatMessageEvent;
import beat.osu.shared.dto.chat.events.UserJoinedChannelEvent;
import beat.osu.shared.dto.chat.events.UserLeftChannelEvent;
import beat.osu.shared.dto.chat.responses.GetJoinedChannelsResponse;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChatPanel extends VBox {

    private ChannelController channelController;
    private PrivateChatController privateChatController;

    private SelectChannelModal selectChannelModal;
    private OnlineUsersPanel onlineUsersPanel;
    private BanchoButtons banchoButtons;

    private ChatTabs chatTabs;
    private Map<Integer, List<ChannelMessageDto>> channelMessages = new HashMap<>();
    private Map<Integer, List<PrivateChatMessageDto>> privateChatMessages = new HashMap<>();
    private TextField chatField;
    private ScrollPane messagesScrollPane;
    private VBox messagesContainer;

    public ChatPanel(ChannelController channelController, PrivateChatController privateChatController, SelectChannelModal selectChannelModal, OnlineUsersPanel onlineUsersPanel, BanchoButtons banchoButtons) {
        super();
        this.channelController = channelController;
        this.privateChatController = privateChatController;
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

    private void handleChannelMessage(ChannelMessageEvent event) {
        Platform.runLater(() -> {
            ChannelMessageDto message = event.getChannelMessage();
            int channelId = message.getChannelId();
            
            channelMessages.computeIfAbsent(channelId, k -> new ArrayList<>()).add(message);
            
            if (chatTabs.getCurrentSelectedTab() != null &&
                    ((ChannelDto) chatTabs.getCurrentSelectedTab()).getId() == channelId) {
                displayMessages();
            }
        });
    }

    private void handlePrivateMessage(PrivateChatMessageEvent event) {
        Platform.runLater(() -> {
            PrivateChatMessageDto message = event.getPrivateChatMessage();
            int otherUserId = message.getSenderId() == AuthManager.getUser().getId() 
                ? message.getRecipientId()
                : message.getSenderId();
            
            privateChatMessages.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(message);
            
            String otherUserName = message.getSenderId() == AuthManager.getUser().getId()
                ? message.getRecipientName()
                : message.getSenderName();
            
            PrivateChatDto privateChat = new PrivateChatDto(otherUserId, otherUserName);
            chatTabs.addPrivateChat(privateChat);
            
            if (chatTabs.getCurrentSelectedTab() instanceof PrivateChatDto &&
                ((PrivateChatDto) chatTabs.getCurrentSelectedTab()).getOtherUserId() == otherUserId) {
                displayMessages();
            }
        });
    }

    public void addPrivateChat(PrivateChatDto privateChat) {
        chatTabs.addPrivateChat(privateChat);
        chatTabs.selectTab(privateChat);
    }

    public boolean isShowing() {
        return this.isVisible();
    }

    private void setupEventHandlers() {
        channelController.addChannelMessageCallback(this::handleChannelMessage);
        channelController.addUserJoinedChannelCallback(this::handleUserJoinedChannel);
        channelController.addUserLeftChannelCallback(this::handleUserLeftChannel);

        privateChatController.addPrivateChatMessageCallback(this::handlePrivateMessage);
    }

    private void setupUI() {
        chatTabs = new ChatTabs();
        
        chatTabs.setOnTabSelected(this::onTabSelected);
        chatTabs.setOnTabClosed(this::handleTabClose);
        chatTabs.setOnAddChannelRequested(this::openChannelSelectionModal);
        chatTabs.getStyleClass().add("chat-tabs");

        messagesContainer = new VBox();
        messagesContainer.getStyleClass().add("messages-container");
        messagesContainer.setPadding(new Insets(10));
        
        messagesScrollPane = new ScrollPane(messagesContainer);
        messagesScrollPane.getStyleClass().add("messages-scroll-pane");
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messagesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(messagesScrollPane, Priority.ALWAYS);
        
        chatField = new TextField();
        chatField.getStyleClass().add("chat-input");
        chatField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
        
        Label chatPrompt = new Label(">");
        chatPrompt.getStyleClass().add("chat-prompt");
        
        HBox chatInputContainer = new HBox();
        chatInputContainer.getStyleClass().add("chat-input-container");
        chatInputContainer.getChildren().addAll(chatPrompt, chatField);
        HBox.setHgrow(chatField, Priority.ALWAYS);
        
        this.getChildren().addAll(chatTabs, messagesScrollPane, chatInputContainer);
    }
    
    private void onTabSelected(Object tab) {
        displayMessages();
    }
    
    private void handleTabClose(ChatTabButton tabButton) {
        List<ChannelDto> joinedChannels = chatTabs.getJoinedChannels();
        List<PrivateChatDto> privateChats = chatTabs.getPrivateChats();
        
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
        } else {
            PrivateChatDto privateChatToClose = privateChats.stream()
                    .filter(chat -> ("@" + chat.getOtherUserName()).equals(tabButton.getTabText()))
                    .findFirst()
                    .orElse(null);
            
            if (privateChatToClose != null) {
                chatTabs.removePrivateChat(privateChatToClose.getOtherUserId());
            }
        }
    }

    private void openChannelSelectionModal() {
        if (selectChannelModal != null) {
            selectChannelModal.show();
            banchoButtons.hide();
            this.hide();

            if (onlineUsersPanel != null && onlineUsersPanel.isVisible()) {
                onlineUsersPanel.hide();
            }
        }
    }
    
    private void displayMessages() {
        messagesContainer.getChildren().clear();
        
        Object currentTab = chatTabs.getCurrentSelectedTab();
        if (currentTab instanceof ChannelDto) {
            ChannelDto currentChannel = (ChannelDto) currentTab;
            List<ChannelMessageDto> messages = channelMessages.get(currentChannel.getId());
            if (messages != null) {
                for (ChannelMessageDto message : messages) {
                    HBox messageBox = createChannelMessageItem(message);
                    messagesContainer.getChildren().add(messageBox);
                }
            }
        } else if (currentTab instanceof PrivateChatDto) {
            PrivateChatDto currentPrivateChat = (PrivateChatDto) currentTab;
            List<PrivateChatMessageDto> messages = privateChatMessages.get(currentPrivateChat.getOtherUserId());
            if (messages != null) {
                for (PrivateChatMessageDto message : messages) {
                    HBox messageBox = createPrivateChatMessageItem(message);
                    messagesContainer.getChildren().add(messageBox);
                }
            }
        }
        
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }
    
    private HBox createChannelMessageItem(ChannelMessageDto message) {
        HBox messageBox = new HBox();
        messageBox.getStyleClass().add("message-item");

        String timestampAndSender = String.format("%s %s: ",
            formatTimestamp(message.getTimestamp()),
            message.getSenderName());
        
        Label timestampSenderLabel = new Label(timestampAndSender);
        if (message.isFromSupporter()) {
            timestampSenderLabel.getStyleClass().add("message-timestamp-sender-supporter");
        } else {
            timestampSenderLabel.getStyleClass().add("message-timestamp-sender");
        }
        
        Label messageContentLabel = new Label(message.getMessage());
        messageContentLabel.getStyleClass().add("message-content");
        messageContentLabel.setWrapText(true);
        
        messageBox.getChildren().addAll(timestampSenderLabel, messageContentLabel);
        
        return messageBox;
    }
    
    private HBox createPrivateChatMessageItem(PrivateChatMessageDto message) {
        HBox messageBox = new HBox();
        messageBox.getStyleClass().add("message-item");

        String timestampAndSender = String.format("%s %s: ",
            formatTimestamp(message.getTimestamp()),
            message.getSenderName());
        
        Label timestampSenderLabel = new Label(timestampAndSender);
        if (message.isFromSupporter()) {
            timestampSenderLabel.getStyleClass().add("message-timestamp-sender-supporter");
        } else {
            timestampSenderLabel.getStyleClass().add("message-timestamp-sender");
        }
        
        Label messageContentLabel = new Label(message.getMessage());
        messageContentLabel.getStyleClass().add("message-content");
        messageContentLabel.setWrapText(true);
        
        messageBox.getChildren().addAll(timestampSenderLabel, messageContentLabel);
        
        return messageBox;
    }
    
    private String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timestamp.format(formatter);
    }
    
    private void sendMessage() {
        String messageText = chatField.getText().trim();
        if (!messageText.isEmpty()) {
            Object selectedTab = chatTabs.getCurrentSelectedTab();
            if (selectedTab instanceof ChannelDto) {
                ChannelDto channel = (ChannelDto) selectedTab;
                channelController.sendChannelMessage(channel.getId(), messageText)
                        .thenAccept(result -> {
                            Platform.runLater(() -> {
                                if (result.isSuccess()) {
                                    ChannelMessageDto sentMessage = result.getValue().getChannelMessage();
                                    channelMessages.computeIfAbsent(channel.getId(), k -> new ArrayList<>()).add(sentMessage);
                                    displayMessages();
                                } else {
                                    System.err.println("Failed to send message: " + result.getError().getMessage());
                                }
                            });
                        });
            } else if (selectedTab instanceof PrivateChatDto) {
                PrivateChatDto privateChat = (PrivateChatDto) selectedTab;
                privateChatController.sendPrivateMessage(privateChat.getOtherUserId(), messageText)
                        .thenAccept(result -> {
                            Platform.runLater(() -> {
                                if (result.isSuccess()) {
                                    PrivateChatMessageDto sentMessage = result.getValue().getPrivateChatMessage();
                                    privateChatMessages.computeIfAbsent(privateChat.getOtherUserId(), k -> new ArrayList<>()).add(sentMessage);
                                    displayMessages();
                                } else {
                                    System.err.println("Failed to send private message: " + result.getError().getMessage());
                                }
                            });
                        });
            }
            chatField.clear();
        }
    }
    
    public void startPrivateChat(int otherUserId, String otherUserName) {
        PrivateChatDto privateChat = new PrivateChatDto(otherUserId, otherUserName);
        chatTabs.addPrivateChat(privateChat);
        chatTabs.selectTab(privateChat);
    }
}