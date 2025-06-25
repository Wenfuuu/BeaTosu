package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.events.PrivateChatMessageEvent;
import beat.osu.shared.dto.chat.requests.SendPrivateChatMessageRequest;
import beat.osu.shared.dto.chat.responses.SendPrivateChatMessageResponse;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;

public class PrivateChatController {
    private final ClientService clientService;

    private final List<Consumer<PrivateChatMessageEvent>> privateChatMessageCallbacks = new ArrayList<>();

    public PrivateChatController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addPrivateChatMessageCallback(Consumer<PrivateChatMessageEvent> callback) {
        privateChatMessageCallbacks.add(callback);
    }

    public void removePrivateChatMessageCallback(Consumer<PrivateChatMessageEvent> callback) {
        privateChatMessageCallbacks.remove(callback);
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
}