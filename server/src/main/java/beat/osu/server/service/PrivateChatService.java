package beat.osu.server.service;

import beat.osu.server.entities.PrivateChat;
import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.PrivateChatDto;
import beat.osu.shared.dto.chat.PrivateChatMessageDto;
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
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateChatService {

    private final SessionService sessionService;
    private final UserService userService;

    private final Map<Integer, PrivateChat> privateChats = new ConcurrentHashMap<>();
    private final Map<String, Integer> userPairToChatId = new ConcurrentHashMap<>();

    private int nextChatId = 1;

    public PrivateChatService(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    public Result<GetPrivateChatsResponse> getPrivateChats(String clientId) {
        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        List<PrivateChatDto> chats = new ArrayList<>();

        for (PrivateChat chat : privateChats.values()) {
            if (chat.isParticipant(userId)) {
                int otherUserId = chat.getOtherUserId(userId);
                User otherUser = userService.findUserById(otherUserId);

                if (otherUser != null) {
                    PrivateChatDto privateChatDto = new PrivateChatDto(
                            otherUserId,
                            otherUser.getUsername()
                    );

                    chats.add(privateChatDto);
                }
            }
        }

        return Result.success(new GetPrivateChatsResponse(chats));
    }

    public Result<StartPrivateChatResponse> startPrivateChat(StartPrivateChatRequest request, String clientId) {
        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        int otherUserId = request.getOtherUserId();

        if (userId == otherUserId) {
            return Result.failure(Error.validation("Cannot start conversation with yourself"));
        }

        User otherUser = userService.findUserById(otherUserId);
        if (otherUser == null) {
            return Result.failure(Error.notFound("User not found"));
        }

        createOrGetPrivateChat(userId, otherUserId);

        Result<StartPrivateChatResponse> response = Result.success(new StartPrivateChatResponse("Started private chat with " + otherUser.getUsername()));
        if (response.isSuccess()) {
            PrivateChatDto privateChatDto = new PrivateChatDto(
                    userId,
                    userService.findUserById(userId).getUsername()
            );
            PrivateChatStartedEvent event = new PrivateChatStartedEvent(privateChatDto, otherUserId);
            RealtimeMessage message = new RealtimeMessage(RealtimeMessageType.PRIVATE_CHAT_STARTED, clientId, event);
            String otherUserClientId =sessionService.getClientIdByUserId(otherUserId);
            RealtimeMessageHandler.sendToClient(message, otherUserClientId);
        }

        return response;
    }

    public Result<LeavePrivateChatResponse> leavePrivateChat(LeavePrivateChatRequest request, String clientId) {
        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        int otherUserId = request.getOtherUserId();

        if (userId == otherUserId) {
            return Result.failure(Error.validation("Invalid operation"));
        }

        User otherUser = userService.findUserById(otherUserId);
        if (otherUser == null) {
            return Result.failure(Error.notFound("User not found"));
        }

        String userPair = createUserPairKey(userId, otherUserId);
        Integer chatId = userPairToChatId.get(userPair);

        if (chatId == null) {
            return Result.failure(Error.notFound("Private chat not found"));
        }

        privateChats.remove(chatId);
        userPairToChatId.remove(userPair);

        Result<LeavePrivateChatResponse> response = Result.success(new LeavePrivateChatResponse("Left private chat with " + otherUser.getUsername()));

        if (response.isSuccess()) {
            UserLeftPrivateChatEvent event = new UserLeftPrivateChatEvent(chatId, userId);
            RealtimeMessage message = new RealtimeMessage(RealtimeMessageType.LEFT_PRIVATE_CHAT, clientId, event);
            String otherUserClientId = sessionService.getClientIdByUserId(otherUserId);
            RealtimeMessageHandler.sendToClient(message, otherUserClientId);
        }

        return response;
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

        createOrGetPrivateChat(senderId, recipientId);

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

    private String createUserPairKey(int userIdA, int userIdB) {
        int smaller = Math.min(userIdA, userIdB);
        int larger = Math.max(userIdA, userIdB);
        return smaller + "-" + larger;
    }

    private void createOrGetPrivateChat(int userIdA, int userIdB) {
        String userPair = createUserPairKey(userIdA, userIdB);

        Integer chatId = userPairToChatId.get(userPair);
        if (chatId != null) {
            privateChats.get(chatId);
            return;
        }

        int newChatId = nextChatId++;
        PrivateChat newChat = new PrivateChat(
                newChatId,
                Math.min(userIdA, userIdB),
                Math.max(userIdA, userIdB)
        );

        privateChats.put(newChatId, newChat);
        userPairToChatId.put(userPair, newChatId);

    }
}
