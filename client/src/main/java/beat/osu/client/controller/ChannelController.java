package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.requests.JoinChannelRequest;
import beat.osu.shared.dto.chat.requests.LeaveChannelRequest;
import beat.osu.shared.dto.chat.requests.SendChannelMessageRequest;
import beat.osu.shared.dto.chat.responses.GetAllChannelsResponse;
import beat.osu.shared.dto.chat.responses.JoinChannelResponse;
import beat.osu.shared.dto.chat.responses.LeaveChannelResponse;
import beat.osu.shared.dto.chat.responses.SendChannelMessageResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.RequestMessage;

import java.util.concurrent.CompletableFuture;

public class ChannelController {
    private final ClientService clientService;

    public ChannelController() {
        this.clientService = ClientService.getInstance();
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

                System.out.println("Response type: " + response.getClass().getName());
                System.out.println("Response content: " + response.toString());

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

                System.out.println("Response type: " + response.getClass().getName());
                System.out.println("Response content: " + response.toString());

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
}
