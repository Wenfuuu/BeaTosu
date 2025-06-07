package beat.osu.client.interfaces.observers;

import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.dto.user.events.UserCountChangedEvent;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;

import java.util.List;

public interface UserStoreObserver {
    default void onConnectedUsersUpdated(List<UserDto> users) {}
    default void onUserConnected(UserConnectedEvent event) {}
    default void onUserDisconnected(UserDisconnectedEvent event) {}
    default void onUserCountChanged(UserCountChangedEvent event) {}
}
