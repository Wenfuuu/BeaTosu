package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import beat.osu.client.helper.AuthManager;
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
import beat.osu.shared.dto.chat.responses.SendChannelMessageResponse;
import beat.osu.shared.dto.chat.responses.SendPrivateChatMessageResponse;
import javafx.application.Platform;

public class ChatController {
    private final ChannelController channelController;
    private final PrivateChatController privateChatController;
    
    // Data storage
    private Map<Integer, List<ChannelMessageDto>> channelMessages = new HashMap<>();
    private Map<Integer, List<PrivateChatMessageDto>> privateChatMessages = new HashMap<>();
    private List<ChannelDto> joinedChannels = new ArrayList<>();
    private List<PrivateChatDto> privateChats = new ArrayList<>();
    
    // Callbacks for UI updates
    private List<Consumer<ChannelDto>> channelAddedCallbacks = new ArrayList<>();
    private List<Consumer<Integer>> channelRemovedCallbacks = new ArrayList<>();
    private List<Consumer<PrivateChatDto>> privateChatAddedCallbacks = new ArrayList<>();
    private List<Consumer<Integer>> privateChatRemovedCallbacks = new ArrayList<>();
    private List<Consumer<Integer>> channelMessagesUpdatedCallbacks = new ArrayList<>();
    private List<Consumer<Integer>> privateChatMessagesUpdatedCallbacks = new ArrayList<>();
    private List<Runnable> joinedChannelsUpdatedCallbacks = new ArrayList<>();
    
    public ChatController(ChannelController channelController, PrivateChatController privateChatController) {
        this.channelController = channelController;
        this.privateChatController = privateChatController;
        setupEventHandlers();
    }
    
    // Data access methods
    public List<ChannelMessageDto> getChannelMessages(int channelId) {
        return channelMessages.getOrDefault(channelId, new ArrayList<>());
    }
    
    public List<PrivateChatMessageDto> getPrivateChatMessages(int otherUserId) {
        return privateChatMessages.getOrDefault(otherUserId, new ArrayList<>());
    }
    
    public List<ChannelDto> getJoinedChannels() {
        return new ArrayList<>(joinedChannels);
    }
    
    public List<PrivateChatDto> getPrivateChats() {
        return new ArrayList<>(privateChats);
    }
    
    // Channel operations
    public CompletableFuture<Result<SendChannelMessageResponse>> sendChannelMessage(int channelId, String message) {
        return channelController.sendChannelMessage(channelId, message)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        Platform.runLater(() -> {
                            ChannelMessageDto sentMessage = result.getValue().getChannelMessage();
                            channelMessages.computeIfAbsent(channelId, k -> new ArrayList<>()).add(sentMessage);
                            notifyChannelMessagesUpdated(channelId);
                        });
                    }
                    return result;
                });
    }
    
    // Private chat operations
    public CompletableFuture<Result<SendPrivateChatMessageResponse>> sendPrivateMessage(int otherUserId, String message) {
        return privateChatController.sendPrivateMessage(otherUserId, message)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        Platform.runLater(() -> {
                            PrivateChatMessageDto sentMessage = result.getValue().getPrivateChatMessage();
                            privateChatMessages.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(sentMessage);
                            notifyPrivateChatMessagesUpdated(otherUserId);
                        });
                    }
                    return result;
                });
    }
    
    public void loadJoinedChannels() {
        try {
            Result<GetJoinedChannelsResponse> result = channelController.getJoinedChannels().get();
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    GetJoinedChannelsResponse response = result.getValue();
                    joinedChannels.clear();
                    joinedChannels.addAll(response.getChannels());
                    notifyJoinedChannelsUpdated();
                } else {
                    System.out.println(result.getError().getMessage());
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void addPrivateChat(PrivateChatDto privateChat) {
        if (privateChats.stream().noneMatch(chat -> chat.getOtherUserId() == privateChat.getOtherUserId())) {
            privateChats.add(privateChat);
            notifyPrivateChatAdded(privateChat);
        }
    }
    
    public void removePrivateChat(int otherUserId) {
        privateChats.removeIf(chat -> chat.getOtherUserId() == otherUserId);
        privateChatMessages.remove(otherUserId);
        notifyPrivateChatRemoved(otherUserId);
    }
    
    public void leaveChannel(int channelId) {
        channelController.leaveChannel(channelId)
                .thenAccept(result -> {
                    Platform.runLater(() -> {
                        if (result.isSuccess()) {
                            joinedChannels.removeIf(channel -> channel.getId() == channelId);
                            channelMessages.remove(channelId);
                            notifyChannelRemoved(channelId);
                        }
                    });
                });
    }
    
    // Callback registration methods
    public void addChannelAddedCallback(Consumer<ChannelDto> callback) {
        channelAddedCallbacks.add(callback);
    }
    
    public void addChannelRemovedCallback(Consumer<Integer> callback) {
        channelRemovedCallbacks.add(callback);
    }
    
    public void addPrivateChatAddedCallback(Consumer<PrivateChatDto> callback) {
        privateChatAddedCallbacks.add(callback);
    }
    
    public void addPrivateChatRemovedCallback(Consumer<Integer> callback) {
        privateChatRemovedCallbacks.add(callback);
    }
    
    public void addChannelMessagesUpdatedCallback(Consumer<Integer> callback) {
        channelMessagesUpdatedCallbacks.add(callback);
    }
    
    public void addPrivateChatMessagesUpdatedCallback(Consumer<Integer> callback) {
        privateChatMessagesUpdatedCallbacks.add(callback);
    }
    
    public void addJoinedChannelsUpdatedCallback(Runnable callback) {
        joinedChannelsUpdatedCallbacks.add(callback);
    }
    
    // Remove callback methods
    public void removeChannelAddedCallback(Consumer<ChannelDto> callback) {
        channelAddedCallbacks.remove(callback);
    }
    
    public void removeChannelRemovedCallback(Consumer<Integer> callback) {
        channelRemovedCallbacks.remove(callback);
    }
    
    public void removePrivateChatAddedCallback(Consumer<PrivateChatDto> callback) {
        privateChatAddedCallbacks.remove(callback);
    }
    
    public void removePrivateChatRemovedCallback(Consumer<Integer> callback) {
        privateChatRemovedCallbacks.remove(callback);
    }
    
    public void removeChannelMessagesUpdatedCallback(Consumer<Integer> callback) {
        channelMessagesUpdatedCallbacks.remove(callback);
    }
    
    public void removePrivateChatMessagesUpdatedCallback(Consumer<Integer> callback) {
        privateChatMessagesUpdatedCallbacks.remove(callback);
    }
    
    public void removeJoinedChannelsUpdatedCallback(Runnable callback) {
        joinedChannelsUpdatedCallbacks.remove(callback);
    }
    
    // Event handling setup
    private void setupEventHandlers() {
        channelController.addChannelMessageCallback(this::handleChannelMessage);
        channelController.addUserJoinedChannelCallback(this::handleUserJoinedChannel);
        channelController.addUserLeftChannelCallback(this::handleUserLeftChannel);
        privateChatController.addPrivateChatMessageCallback(this::handlePrivateMessage);
    }
    
    private void handleChannelMessage(ChannelMessageEvent event) {
        Platform.runLater(() -> {
            ChannelMessageDto message = event.getChannelMessage();
            int channelId = message.getChannelId();
            
            channelMessages.computeIfAbsent(channelId, k -> new ArrayList<>()).add(message);
            notifyChannelMessagesUpdated(channelId);
        });
    }
    
    private void handleUserJoinedChannel(UserJoinedChannelEvent event) {
        Platform.runLater(() -> {
            if (event.getUserId() == AuthManager.getUser().getId()) {
                ChannelDto channel = event.getChannel();
                if (joinedChannels.stream().noneMatch(ch -> ch.getId() == channel.getId())) {
                    joinedChannels.add(channel);
                    notifyChannelAdded(channel);
                }
            }
        });
    }
    
    private void handleUserLeftChannel(UserLeftChannelEvent event) {
        Platform.runLater(() -> {
            if (event.getUserId() == AuthManager.getUser().getId()) {
                joinedChannels.removeIf(channel -> channel.getId() == event.getChannelId());
                channelMessages.remove(event.getChannelId());
                notifyChannelRemoved(event.getChannelId());
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
            addPrivateChat(privateChat);
            
            notifyPrivateChatMessagesUpdated(otherUserId);
        });
    }
    
    // Notification methods
    private void notifyChannelAdded(ChannelDto channel) {
        for (Consumer<ChannelDto> callback : channelAddedCallbacks) {
            try {
                callback.accept(channel);
            } catch (Exception e) {
                System.err.println("Error in channel added callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyChannelRemoved(int channelId) {
        for (Consumer<Integer> callback : channelRemovedCallbacks) {
            try {
                callback.accept(channelId);
            } catch (Exception e) {
                System.err.println("Error in channel removed callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyPrivateChatAdded(PrivateChatDto privateChat) {
        for (Consumer<PrivateChatDto> callback : privateChatAddedCallbacks) {
            try {
                callback.accept(privateChat);
            } catch (Exception e) {
                System.err.println("Error in private chat added callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyPrivateChatRemoved(int otherUserId) {
        for (Consumer<Integer> callback : privateChatRemovedCallbacks) {
            try {
                callback.accept(otherUserId);
            } catch (Exception e) {
                System.err.println("Error in private chat removed callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyChannelMessagesUpdated(int channelId) {
        for (Consumer<Integer> callback : channelMessagesUpdatedCallbacks) {
            try {
                callback.accept(channelId);
            } catch (Exception e) {
                System.err.println("Error in channel messages updated callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyPrivateChatMessagesUpdated(int otherUserId) {
        for (Consumer<Integer> callback : privateChatMessagesUpdatedCallbacks) {
            try {
                callback.accept(otherUserId);
            } catch (Exception e) {
                System.err.println("Error in private chat messages updated callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyJoinedChannelsUpdated() {
        for (Runnable callback : joinedChannelsUpdatedCallbacks) {
            try {
                callback.run();
            } catch (Exception e) {
                System.err.println("Error in joined channels updated callback: " + e.getMessage());
            }
        }
    }

    public void loadExistingPrivateChats() {
        Platform.runLater(() -> {
            for (PrivateChatDto privateChat : privateChats) {
                notifyPrivateChatAdded(privateChat);
            }
        });
    }
}
