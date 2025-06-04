package beat.osu.server.service;

import beat.osu.server.entities.Channel;
import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.ChannelMessageDto;
import beat.osu.shared.dto.chat.requests.JoinChannelRequest;
import beat.osu.shared.dto.chat.requests.LeaveChannelRequest;
import beat.osu.shared.dto.chat.requests.SendChannelMessageRequest;
import beat.osu.shared.dto.chat.responses.GetAllChannelsResponse;
import beat.osu.shared.dto.chat.responses.JoinChannelResponse;
import beat.osu.shared.dto.chat.responses.LeaveChannelResponse;
import beat.osu.shared.dto.chat.responses.SendChannelMessageResponse;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelService {

    private final Map<Integer, Channel> channels = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> channelMembers = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> userChannels = new ConcurrentHashMap<>();

    private final SessionService sessionService;
    private final UserService userService;

    public ChannelService(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;

        // TODO: Load channels from DB
        channels.put(1, new Channel(1, "#osu", "The official osu! channel (english only)."));
        channels.put(2, new Channel(2, "#announce", "Automated announcements of stuff going on in osu!"));
        channels.put(3, new Channel(3, "english", "English community channel."));

        channelMembers.put(1, ConcurrentHashMap.newKeySet());
        channelMembers.put(2, ConcurrentHashMap.newKeySet());
        channelMembers.put(3, ConcurrentHashMap.newKeySet());
    }

    public Result<GetAllChannelsResponse> getAllChannels() {
        List<ChannelDto> channelDtos = new ArrayList<>();

        List<Channel> channelList = new ArrayList<>(channels.values());
        for (Channel channel : channelList) {
            int memberCount = channelMembers.get(channel.getId()).size();
            ChannelDto channelDto = new ChannelDto(channel.getId(), channel.getName(), channel.getDescription(), memberCount);
            channelDtos.add(channelDto);
        }

        Result<GetAllChannelsResponse> response = Result.success(new GetAllChannelsResponse(channelDtos));
        return response;
    }

    public Result<JoinChannelResponse> joinChannel(JoinChannelRequest request, String clientId) {
        int channelId = request.getChannelId();

        Channel channel = channels.get(channelId);
        if (channel == null) {
            return Result.failure(Error.notFound("Channel not found"));
        }

        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (channelMembers.get(channelId).contains(userId)) {
            return Result.failure(Error.validation("You are already a member of this channel"));
        }

        channelMembers.get(channelId).add(userId);
        userChannels.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(channelId);

        Result<JoinChannelResponse> response = Result.success(new JoinChannelResponse("Successfully joined channel " + channel.getName()));
        if (response.isSuccess()) {
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_JOINED_CHANNEL, clientId, userId);
            broadcastMessageToChannelMembers(clientId, channelId, realtimeMessage);
        }

        return response;
    }

    public Result<LeaveChannelResponse> leaveChannel(LeaveChannelRequest request, String clientId) {
        int channelId = request.getChannelId();

        Channel channel = channels.get(channelId);
        if (channel == null) {
            return Result.failure(Error.notFound("Channel not found"));
        }

        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (!channelMembers.get(channelId).contains(userId)) {
            return Result.failure(Error.validation("You are not a member of this channel"));
        }

        channelMembers.get(channelId).remove(userId);
        Set<Integer> userChannelSet = userChannels.get(userId);
        if (userChannelSet != null) {
            userChannelSet.remove(channelId);
        }

        Result<LeaveChannelResponse> response = Result.success(new LeaveChannelResponse("Successfully left channel " + channel.getName()));
        if (response.isSuccess()) {
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_LEFT_CHANNEL, clientId, userId);
            broadcastMessageToChannelMembers(clientId, channelId, realtimeMessage);
        }

        return response;
    }

    public Result<SendChannelMessageResponse> sendChannelMessage(SendChannelMessageRequest request, String clientId) {
        int channelId = request.getChannelId();
        String message = request.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return Result.failure(Error.validation("Message cannot be empty"));
        }

        Channel channel = channels.get(channelId);
        if (channel == null) {
            return Result.failure(Error.notFound("Channel not found"));
        }

        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (!channelMembers.get(channelId).contains(userId)) {
            return Result.failure(Error.validation("You are not a member of this channel"));
        }

        User user = userService.findUserById(userId);
        String username = user.getUsername();

        ChannelMessageDto channelMessageDto = new ChannelMessageDto(
                channel.getId(),
                userId,
                username,
                message,
                LocalDateTime.now()
        );

        Result<SendChannelMessageResponse> response = Result.success(new SendChannelMessageResponse("Message sent successfully"));
        if (response.isSuccess()) {
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.CHANNEL_MESSAGE, clientId, channelMessageDto);
            broadcastMessageToChannelMembers(clientId, channelId, realtimeMessage);
        }

        return response;
    }

    private void broadcastMessageToChannelMembers(String clientId, int channelId, RealtimeMessage realtimeMessage) {
        Set<Integer> memberIds = channelMembers.get(channelId);
        for (Integer memberId : memberIds) {
            String memberClientId = sessionService.getClientIdByUserId(memberId);
            if (memberClientId != null && !memberClientId.equals(clientId)) {
                RealtimeMessageHandler.sendToClient(realtimeMessage, memberClientId);
            }
        }
    }

    public Set<Integer> getChannelMembers(int channelId) {
        return new HashSet<>(channelMembers.getOrDefault(channelId, Collections.emptySet()));
    }

    public Set<Integer> getUserChannels(int userId) {
        return new HashSet<>(userChannels.getOrDefault(userId, Collections.emptySet()));
    }

    public void removeUserFromAllChannels(int userId) {
        Set<Integer> userChannelSet = userChannels.remove(userId);
        if (userChannelSet != null) {
            for (int channelId : userChannelSet) {
                Set<Integer> members = channelMembers.get(channelId);
                if (members != null) {
                    members.remove(userId);
                }
            }
        }
    }

    public Channel getChannelById(int channelId) {
        return channels.get(channelId);
    }
}