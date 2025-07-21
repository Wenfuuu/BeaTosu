package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.events.ChannelMessageEvent;
import beat.osu.shared.dto.chat.events.UserJoinedChannelEvent;
import beat.osu.shared.dto.chat.events.UserLeftChannelEvent;
import beat.osu.shared.dto.chat.requests.JoinChannelRequest;
import beat.osu.shared.dto.chat.requests.LeaveChannelRequest;
import beat.osu.shared.dto.chat.requests.SendChannelMessageRequest;
import beat.osu.shared.dto.chat.responses.GetAllChannelsResponse;
import beat.osu.shared.dto.chat.responses.GetJoinedChannelsResponse;
import beat.osu.shared.dto.chat.responses.JoinChannelResponse;
import beat.osu.shared.dto.chat.responses.LeaveChannelResponse;
import beat.osu.shared.dto.chat.responses.SendChannelMessageResponse;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;

public class ChannelController {
    private final ClientService clientService;

    private final List<Consumer<ChannelMessageEvent>> channelMessageCallbacks = new ArrayList<>();
    private final List<Consumer<UserJoinedChannelEvent>> userJoinedChannelCallbacks = new ArrayList<>();
    private final List<Consumer<UserLeftChannelEvent>> userLeftChannelCallbacks = new ArrayList<>();

    public ChannelController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addChannelMessageCallback(Consumer<ChannelMessageEvent> callback) {
        channelMessageCallbacks.add(callback);
    }

    public void addUserJoinedChannelCallback(Consumer<UserJoinedChannelEvent> callback) {
        userJoinedChannelCallbacks.add(callback);
    }

    public void addUserLeftChannelCallback(Consumer<UserLeftChannelEvent> callback) {
        userLeftChannelCallbacks.add(callback);
    }

    public void removeChannelMessageCallback(Consumer<ChannelMessageEvent> callback) {
        channelMessageCallbacks.remove(callback);
    }

    public void removeUserJoinedChannelCallback(Consumer<UserJoinedChannelEvent> callback) {
        userJoinedChannelCallbacks.remove(callback);
    }

    public void removeUserLeftChannelCallback(Consumer<UserLeftChannelEvent> callback) {
        userLeftChannelCallbacks.remove(callback);
    }

    public CompletableFuture<Result<GetAllChannelsResponse>> getAllChannels() {
        RequestMessage request = new RequestMessage(MessageType.CHANNEL, MessageAction.GET_ALL_CHANNELS, null);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((GetAllChannelsResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<JoinChannelResponse>> joinChannel(int channelId) {
        JoinChannelRequest requestData = new JoinChannelRequest(channelId);
        RequestMessage request = new RequestMessage(MessageType.CHANNEL, MessageAction.JOIN_CHANNEL, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((JoinChannelResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<LeaveChannelResponse>> leaveChannel(int channelId) {
        LeaveChannelRequest requestData = new LeaveChannelRequest(channelId);
        RequestMessage request = new RequestMessage(MessageType.CHANNEL, MessageAction.LEAVE_CHANNEL, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((LeaveChannelResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<SendChannelMessageResponse>> sendChannelMessage(int channelId, String message) {
        SendChannelMessageRequest requestData = new SendChannelMessageRequest(channelId, message);
        RequestMessage request = new RequestMessage(MessageType.CHANNEL, MessageAction.SEND_CHANNEL_MESSAGE, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((SendChannelMessageResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<GetJoinedChannelsResponse>> getJoinedChannels() {
        RequestMessage request = new RequestMessage(MessageType.CHANNEL, MessageAction.GET_JOINED_CHANNELS, null);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((GetJoinedChannelsResponse) result.getValue());
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
        if (message.getType() == RealtimeMessageType.CHANNEL_MESSAGE) {
            if (message.getPayload() instanceof ChannelMessageEvent) {
                ChannelMessageEvent event = (ChannelMessageEvent) message.getPayload();
                notifyChannelMessage(event);
            }
        } else if (message.getType() == RealtimeMessageType.USER_JOINED_CHANNEL) {
            if (message.getPayload() instanceof UserJoinedChannelEvent) {
                UserJoinedChannelEvent event = (UserJoinedChannelEvent) message.getPayload();
                notifyUserJoinedChannel(event);
            }
        } else if (message.getType() == RealtimeMessageType.USER_LEFT_CHANNEL) {
            if (message.getPayload() instanceof UserLeftChannelEvent) {
                UserLeftChannelEvent event = (UserLeftChannelEvent) message.getPayload();
                notifyUserLeftChannel(event);
            }
        }
    }

    private void notifyChannelMessage(ChannelMessageEvent event) {
        for (Consumer<ChannelMessageEvent> callback : channelMessageCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                // System.err.println("Error in channel message callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserJoinedChannel(UserJoinedChannelEvent event) {
        for (Consumer<UserJoinedChannelEvent> callback : userJoinedChannelCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                // System.err.println("Error in user joined channel callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserLeftChannel(UserLeftChannelEvent event) {
        for (Consumer<UserLeftChannelEvent> callback : userLeftChannelCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                // System.err.println("Error in user left channel callback: " + e.getMessage());
            }
        }
    }
}
