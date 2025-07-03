package beat.osu.client.view.shared.bancho.panels;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import beat.osu.client.controller.ChatController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.shared.bancho.buttons.BanchoButtons;
import beat.osu.client.view.shared.bancho.buttons.ChatTabButton;
import beat.osu.client.view.shared.bancho.modals.SelectChannelModal;
import beat.osu.client.view.shared.bancho.tabs.ChatTabs;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.ChannelMessageDto;
import beat.osu.shared.dto.chat.PrivateChatDto;
import beat.osu.shared.dto.chat.PrivateChatMessageDto;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class ChatPanel extends VBox {

    private ChatController chatController;

    private SelectChannelModal selectChannelModal;
    private OnlineUsersPanel onlineUsersPanel;
    private BanchoButtons banchoButtons;

    @Getter
    private ChatTabs chatTabs;
    private TextField chatField;
    private ScrollPane messagesScrollPane;
    private VBox messagesContainer;

    public ChatPanel(ChatController chatController, SelectChannelModal selectChannelModal, OnlineUsersPanel onlineUsersPanel, BanchoButtons banchoButtons) {
        super();
        this.chatController = chatController;
        this.selectChannelModal = selectChannelModal;
        this.onlineUsersPanel = onlineUsersPanel;
        this.banchoButtons = banchoButtons;

        this.getStyleClass().add("chat-panel");
        this.setVisible(false);

        URL cssUrl = CssManager.getSharedCssURL("ChatPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        setupUI();
        setupEventHandlers();
    }

    public void show() {
        if (this.getParent() instanceof VBox) {
            VBox parentContainer = (VBox) this.getParent();
            parentContainer.setVisible(true);
            parentContainer.setManaged(true);
            parentContainer.setMouseTransparent(false);
        }
        
        chatController.loadJoinedChannels();
        chatController.loadExistingPrivateChats();

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
        hideTransition.setOnFinished(e -> {
            this.setVisible(false);
            
            if (this.getParent() instanceof VBox) {
                VBox parentContainer = (VBox) this.getParent();
                boolean onlineUsersPanelVisible = false;
                
                for (Node child : parentContainer.getChildren()) {
                    if (child instanceof OnlineUsersPanel && child.isVisible()) {
                        onlineUsersPanelVisible = true;
                        break;
                    }
                }
                
                if (!onlineUsersPanelVisible) {
                    parentContainer.setVisible(false);
                    parentContainer.setManaged(false);
                    parentContainer.setMouseTransparent(true);
                }
            }
        });
        hideTransition.play();
    }

    private void setupInputFieldSounds() {
        chatField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
        });
    }

    private void setupEventHandlers() {
        setupInputFieldSounds();

        chatController.addJoinedChannelsUpdatedCallback(() -> {
            Platform.runLater(() -> {
                chatTabs.setJoinedChannels(chatController.getJoinedChannels());
            });
        });
        
        chatController.addChannelAddedCallback(channel -> {
            Platform.runLater(() -> {
                chatTabs.addChannel(channel);
            });
        });
        
        chatController.addChannelRemovedCallback(channelId -> {
            Platform.runLater(() -> {
                chatTabs.removeChannel(channelId);
            });
        });
        
        chatController.addPrivateChatAddedCallback(privateChat -> {
            Platform.runLater(() -> {
                chatTabs.addPrivateChat(privateChat);
            });
        });
        
        chatController.addPrivateChatRemovedCallback(otherUserId -> {
            Platform.runLater(() -> {
                chatTabs.removePrivateChat(otherUserId);
                displayMessages();
            });
        });
        
        chatController.addChannelMessagesUpdatedCallback(channelId -> {
            Platform.runLater(() -> {
                if (chatTabs.getCurrentSelectedTab() instanceof ChannelDto &&
                        Objects.equals(((ChannelDto) chatTabs.getCurrentSelectedTab()).getId(), channelId)) {
                    displayMessages();
                }
            });
        });
        
        chatController.addPrivateChatMessagesUpdatedCallback(otherUserId -> {
            Platform.runLater(() -> {
                if (chatTabs.getCurrentSelectedTab() instanceof PrivateChatDto &&
                    ((PrivateChatDto) chatTabs.getCurrentSelectedTab()).getOtherUserId() == otherUserId) {
                    displayMessages();
                }
            });
        });
    }

    public void addPrivateChat(PrivateChatDto privateChat) {
        chatController.addPrivateChat(privateChat);
        chatTabs.selectTab(privateChat);
    }

    public boolean isShowing() {
        return this.isVisible();
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
        List<ChannelDto> joinedChannels = chatController.getJoinedChannels();
        List<PrivateChatDto> privateChats = chatController.getPrivateChats();
        
        ChannelDto channelToLeave = joinedChannels.stream()
                .filter(channel -> channel.getName().equals(tabButton.getTabText()))
                .findFirst()
                .orElse(null);

        if (channelToLeave != null) {
            chatController.leaveChannel(channelToLeave.getId());
        } else {
            PrivateChatDto privateChatToClose = privateChats.stream()
                    .filter(chat -> chat.getOtherUserName().equals(tabButton.getTabText()))
                    .findFirst()
                    .orElse(null);
            
            if (privateChatToClose != null) {
                chatController.removePrivateChat(privateChatToClose.getOtherUserId());
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
            List<ChannelMessageDto> messages = chatController.getChannelMessages(currentChannel.getId());
            for (ChannelMessageDto message : messages) {
                HBox messageBox = createChannelMessageItem(message);
                messagesContainer.getChildren().add(messageBox);
            }
        } else if (currentTab instanceof PrivateChatDto) {
            PrivateChatDto currentPrivateChat = (PrivateChatDto) currentTab;
            List<PrivateChatMessageDto> messages = chatController.getPrivateChatMessages(currentPrivateChat.getOtherUserId());
            for (PrivateChatMessageDto message : messages) {
                HBox messageBox = createPrivateChatMessageItem(message);
                messagesContainer.getChildren().add(messageBox);
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
                chatController.sendChannelMessage(channel.getId(), messageText)
                        .thenAccept(result -> {
                            Platform.runLater(() -> {
                                if (!result.isSuccess()) {
                                    System.err.println("Failed to send message: " + result.getError().getMessage());
                                }
                            });
                        });
            } else if (selectedTab instanceof PrivateChatDto) {
                PrivateChatDto privateChat = (PrivateChatDto) selectedTab;
                chatController.sendPrivateMessage(privateChat.getOtherUserId(), messageText)
                        .thenAccept(result -> {
                            Platform.runLater(() -> {
                                if (!result.isSuccess()) {
                                    System.err.println("Failed to send private message: " + result.getError().getMessage());
                                }
                            });
                        });
            }
            chatField.clear();
        }
    }
    
    public void startPrivateChat(PrivateChatDto privateChat) {
        chatController.addPrivateChat(privateChat);
        chatTabs.selectTab(privateChat);
    }
}