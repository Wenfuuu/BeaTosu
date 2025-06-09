package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.events.PrivateChatMessageEvent;
import beat.osu.shared.dto.chat.events.PrivateChatStartedEvent;
import beat.osu.shared.dto.chat.events.UserLeftPrivateChatEvent;
import beat.osu.shared.dto.chat.requests.LeavePrivateChatRequest;
import beat.osu.shared.dto.chat.requests.SendPrivateChatMessageRequest;
import beat.osu.shared.dto.chat.requests.StartPrivateChatRequest;
import beat.osu.shared.dto.chat.responses.GetPrivateChatsResponse;
import beat.osu.shared.dto.chat.responses.LeavePrivateChatResponse;
import beat.osu.shared.dto.chat.responses.SendPrivateChatMessageResponse;
import beat.osu.shared.dto.chat.responses.StartPrivateChatResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;

public class PrivateChatController {
    private final ClientService clientService;

    private final List<Consumer<PrivateChatMessageEvent>> privateChatMessageCallbacks = new ArrayList<>();
    private final List<Consumer<PrivateChatStartedEvent>> privateChatStartedCallbacks = new ArrayList<>();
    private final List<Consumer<UserLeftPrivateChatEvent>> userLeftPrivateChatCallbacks = new ArrayList<>();

    public PrivateChatController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addPrivateChatMessageCallback(Consumer<PrivateChatMessageEvent> callback) {
        privateChatMessageCallbacks.add(callback);
    }

    public void addPrivateChatStartedCallback(Consumer<PrivateChatStartedEvent> callback) {
        privateChatStartedCallbacks.add(callback);
    }

    public void addUserLeftPrivateChatCallback(Consumer<UserLeftPrivateChatEvent> callback) {
        userLeftPrivateChatCallbacks.add(callback);
    }

    public void removePrivateChatMessageCallback(Consumer<PrivateChatMessageEvent> callback) {
        privateChatMessageCallbacks.remove(callback);
    }

    public void removePrivateChatStartedCallback(Consumer<PrivateChatStartedEvent> callback) {
        privateChatStartedCallbacks.remove(callback);
    }

    public void removeUserLeftPrivateChatCallback(Consumer<UserLeftPrivateChatEvent> callback) {
        userLeftPrivateChatCallbacks.remove(callback);
    }

    public CompletableFuture<Result<GetPrivateChatsResponse>> getPrivateChats() {
        RequestMessage request = new RequestMessage(MessageType.PRIVATE_CHAT, MessageAction.GET_PRIVATE_CHATS, null);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((GetPrivateChatsResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<StartPrivateChatResponse>> startPrivateChat(int otherUserId) {
        StartPrivateChatRequest requestData = new StartPrivateChatRequest(otherUserId);
        RequestMessage request = new RequestMessage(MessageType.PRIVATE_CHAT, MessageAction.START_PRIVATE_CHAT, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((StartPrivateChatResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<LeavePrivateChatResponse>> leavePrivateChat(int otherUserId) {
        LeavePrivateChatRequest requestData = new LeavePrivateChatRequest(otherUserId);
        RequestMessage request = new RequestMessage(MessageType.PRIVATE_CHAT, MessageAction.LEAVE_PRIVATE_CHAT, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((LeavePrivateChatResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<SendPrivateChatMessageResponse>> sendPrivateMessage(int otherUserId, String message) {
        SendPrivateChatMessageRequest requestData = new SendPrivateChatMessageRequest(otherUserId, message);
        RequestMessage request = new RequestMessage(MessageType.PRIVATE_CHAT, MessageAction.SEND_PRIVATE_CHAT_MESSAGE, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((SendPrivateChatMessageResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().addRealtimeMessageCallback(this::handleRealtimeMessage);
        }
    }

    private void handleRealtimeMessage(RealtimeMessage message) {
        if (message.getType() == RealtimeMessageType.PRIVATE_CHAT_MESSAGE) {
            if (message.getPayload() instanceof PrivateChatMessageEvent) {
                PrivateChatMessageEvent event = (PrivateChatMessageEvent) message.getPayload();
                notifyPrivateChatMessage(event);
            }
        } else if (message.getType() == RealtimeMessageType.PRIVATE_CHAT_STARTED) {
            if (message.getPayload() instanceof PrivateChatStartedEvent) {
                PrivateChatStartedEvent event = (PrivateChatStartedEvent) message.getPayload();
                notifyPrivateChatStarted(event);
            }
        } else if (message.getType() == RealtimeMessageType.LEFT_PRIVATE_CHAT) {
            if (message.getPayload() instanceof UserLeftPrivateChatEvent) {
                UserLeftPrivateChatEvent event = (UserLeftPrivateChatEvent) message.getPayload();
                notifyUserLeftPrivateChat(event);
            }
        }
    }

    private void notifyPrivateChatMessage(PrivateChatMessageEvent event) {
        for (Consumer<PrivateChatMessageEvent> callback : privateChatMessageCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in private chat message callback: " + e.getMessage());
            }
        }
    }

    private void notifyPrivateChatStarted(PrivateChatStartedEvent event) {
        for (Consumer<PrivateChatStartedEvent> callback : privateChatStartedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in private chat started callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserLeftPrivateChat(UserLeftPrivateChatEvent event) {
        for (Consumer<UserLeftPrivateChatEvent> callback : userLeftPrivateChatCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in user left private chat callback: " + e.getMessage());
            }
        }
    }
}