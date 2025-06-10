package beat.osu.server.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import beat.osu.server.entities.PrivateChat;
import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.PrivateChatMessageDto;
import beat.osu.shared.dto.chat.events.PrivateChatMessageEvent;
import beat.osu.shared.dto.chat.requests.SendPrivateChatMessageRequest;
import beat.osu.shared.dto.chat.responses.SendPrivateChatMessageResponse;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

public class PrivateChatService {

    private final SessionService sessionService;
    private final UserService userService;

    public PrivateChatService(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    public Result<SendPrivateChatMessageResponse> sendPrivateMessage(SendPrivateChatMessageRequest request, String clientId) {
        Integer senderId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (senderId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        int recipientId = request.getOtherUserId();
        String message = request.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return Result.failure(Error.validation("Message cannot be empty"));
        }

        if (senderId == recipientId) {
            return Result.failure(Error.validation("Cannot send message to yourself"));
        }

        User sender = userService.findUserById(senderId);
        User recipient = userService.findUserById(recipientId);

        if (sender == null || recipient == null) {
            return Result.failure(Error.notFound("User not found"));
        }

        PrivateChatMessageDto messageDto = new PrivateChatMessageDto(
                senderId,
                sender.getUsername(),
                sender.isSupporter(),
                recipientId,
                recipient.getUsername(),
                message,
                LocalDateTime.now()
        );

        Result<SendPrivateChatMessageResponse> response = Result.success(new SendPrivateChatMessageResponse(messageDto));

        if (response.isSuccess()) {
            PrivateChatMessageDto privateChatMessageDto = new PrivateChatMessageDto(
                senderId,
                sender.getUsername(),
                sender.isSupporter(),
                recipientId,
                recipient.getUsername(),
                message,
                LocalDateTime.now()
            );
            PrivateChatMessageEvent event = new PrivateChatMessageEvent(privateChatMessageDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.PRIVATE_CHAT_MESSAGE, clientId, event);
            String otherUserClientId = sessionService.getClientIdByUserId(recipientId);
            RealtimeMessageHandler.sendToClient(realtimeMessage, otherUserClientId);
        }

        return response;
    }
}
