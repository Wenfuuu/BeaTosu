package beat.osu.server.service;

import beat.osu.server.entities.Channel;
import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.ChannelMessageDto;
import beat.osu.shared.dto.chat.events.ChannelMessageEvent;
import beat.osu.shared.dto.chat.events.UserJoinedChannelEvent;
import beat.osu.shared.dto.chat.events.UserLeftChannelEvent;
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
        channels.put(3, new Channel(3, "#arabic", "Arabic community channel."));
        channels.put(4, new Channel(4, "#bulgarian", "Bulgarian community channel."));
        channels.put(5, new Channel(5, "#cantonese", "Cantonese community channel."));
        channels.put(6, new Channel(6, "#chinese", "Chinese community channel."));
        channels.put(7, new Channel(7, "#ctb", "Discuss osu!catch (previously Catch The Beat)."));
        channels.put(8, new Channel(8, "#czechlovak", "Czechlovak community channel."));
        channels.put(9, new Channel(9, "#dutch", "Dutch community channel."));
        channels.put(10, new Channel(10, "#english", "English community channel."));

        channels.put(11, new Channel(11, "#french", "French community channel."));
        channels.put(12, new Channel(12, "#german", "German community channel."));
        channels.put(13, new Channel(13, "#japanese", "Japanese community channel."));
        channels.put(14, new Channel(14, "#korean", "Korean community channel."));
        channels.put(15, new Channel(15, "#portuguese", "Portuguese community channel."));
        channels.put(16, new Channel(16, "#russian", "Russian community channel."));
        channels.put(17, new Channel(17, "#spanish", "Spanish community channel."));
        channels.put(18, new Channel(18, "#thai", "Thai community channel."));
        channels.put(19, new Channel(19, "#turkish", "Turkish community channel."));
        channels.put(20, new Channel(20, "#vietnamese", "Vietnamese community channel."));
        channels.put(21, new Channel(21, "#mania", "Discuss osu!mania, the 4-key and more beatmap mode."));
        channels.put(22, new Channel(22, "#taiko", "Discuss osu!taiko, the drumming beatmap mode."));
        channels.put(23, new Channel(23, "#modding", "For mappers and modders to discuss beatmap creation and feedback."));
        channels.put(24, new Channel(24, "#tournaments", "Announcements and discussion about official and community tournaments."));
        channels.put(25, new Channel(25, "#help", "Ask for help with the game client, website, or common issues."));
        channels.put(26, new Channel(26, "#art", "Share and discuss osu! related fan art and creative works."));
        channels.put(27, new Channel(27, "#skins", "Discuss and share custom osu! skins."));
        channels.put(28, new Channel(28, "#development", "Discussions about the ongoing development of osu! and its features."));
        channels.put(29, new Channel(29, "#memes", "A place for fun and osu! related memes."));
        channels.put(30, new Channel(30, "#offtopic", "General chat for anything not directly related to osu!."));

        for (int i = 1; i <= 30; i++) {
            channelMembers.put(i, ConcurrentHashMap.newKeySet());
        }
    }

    public Result<GetAllChannelsResponse> getAllChannels(String clientId) {
        Integer userId = (Integer) sessionService.getSessionData(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        List<ChannelDto> channelDtos = new ArrayList<>();

        List<Channel> channelList = new ArrayList<>(channels.values());
        for (Channel channel : channelList) {
            Set<Integer> membersOfThisChannel = channelMembers.get(channel.getId());
            if (membersOfThisChannel == null) {
                membersOfThisChannel = Collections.emptySet();
            }

            int memberCount = channelMembers.get(channel.getId()).size();
            boolean isJoined = membersOfThisChannel.contains(userId);

            ChannelDto channelDto = new ChannelDto(channel.getId(), channel.getName(), channel.getDescription(), memberCount, isJoined);
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
            UserJoinedChannelEvent event = new UserJoinedChannelEvent(channelId, userId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_JOINED_CHANNEL, clientId, event);
            RealtimeMessageHandler.broadcastToAllExcept(realtimeMessage, clientId);
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
            UserLeftChannelEvent event = new UserLeftChannelEvent(channelId, userId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_LEFT_CHANNEL, clientId, event);
            RealtimeMessageHandler.broadcastToAllExcept(realtimeMessage, clientId);
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
            ChannelMessageEvent event = new ChannelMessageEvent(channelMessageDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.CHANNEL_MESSAGE, clientId, event);
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