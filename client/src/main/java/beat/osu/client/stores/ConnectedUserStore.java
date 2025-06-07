package beat.osu.client.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import beat.osu.client.interfaces.observers.UserStoreObserver;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.dto.user.events.UserCountChangedEvent;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;

public class ConnectedUserStore {
    private static ConnectedUserStore instance;

    private final Map<Integer, UserDto> connectedUsers = new ConcurrentHashMap<>();
    private final List<UserStoreObserver> observers = new CopyOnWriteArrayList<>();

    private ConnectedUserStore() {
        // Private constructor for singleton
    }

    public static synchronized ConnectedUserStore getInstance() {
        if (instance == null) {
            instance = new ConnectedUserStore();
        }
        return instance;
    }

    public void addObserver(UserStoreObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(UserStoreObserver observer) {
        observers.remove(observer);
    }

    public List<UserDto> getConnectedUsers() {
        return new ArrayList<>(connectedUsers.values());
    }

    public void setConnectedUsers(List<UserDto> users) {
        connectedUsers.clear();
        users.forEach(user -> connectedUsers.put(user.getId(), user));
        notifyUsersUpdated();
        notifyUserCountChanged();
    }

    public int getConnectedUserCount() {
        return connectedUsers.size();
    }

    public Optional<UserDto> getConnectedUser(int userId) {
        return Optional.ofNullable(connectedUsers.get(userId));
    }

    public void handleUserConnected(UserConnectedEvent event) {
        UserDto user = event.getUserDto();
        connectedUsers.put(user.getId(), user);
        notifyUserConnected(event);
        notifyUserCountChanged();
    }

    public void handleUserDisconnected(UserDisconnectedEvent event) {
        UserDto user = event.getUserDto();
        connectedUsers.remove(user.getId());
        notifyUserDisconnected(event);
        notifyUserCountChanged();
    }

    private void notifyUsersUpdated() {
        observers.forEach(observer -> observer.onConnectedUsersUpdated(getConnectedUsers()));
    }

    private void notifyUserConnected(UserConnectedEvent event) {
        observers.forEach(observer -> observer.onUserConnected(event));
    }

    private void notifyUserDisconnected(UserDisconnectedEvent event) {
        observers.forEach(observer -> observer.onUserDisconnected(event));
    }

    private void notifyUserCountChanged() {
        UserCountChangedEvent event = new UserCountChangedEvent(getConnectedUserCount());
        observers.forEach(observer -> observer.onUserCountChanged(event));
    }
}
