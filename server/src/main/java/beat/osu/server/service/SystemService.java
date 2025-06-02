package beat.osu.server.service;

import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.system.responses.GetCurrentUserCountResponse;

public class SystemService {

    public Result<GetCurrentUserCountResponse> getCurrentUserCount() {
        int userCount = RealtimeMessageHandler.getActiveClientCount();

        try {
            return Result.success(new GetCurrentUserCountResponse(userCount));
        } catch (Exception e) {
            return Result.failure(Error.internal("Failed to register user: " + e.getMessage()));
        }
    }
}
