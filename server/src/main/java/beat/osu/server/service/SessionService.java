package beat.osu.server.service;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SessionService {
    private final ConcurrentHashMap<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    public void createSession(String clientId) {
        sessions.put(clientId, new ConcurrentHashMap<>());
    }

    public void setSessionData(String clientId, String key, Object value) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            session.put(key, value);
        }
    }

    public Object getSessionData(String clientId, String key) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            return session.get(key);
        }
        return null;
    }

    public void removeSession(String clientId) {
        sessions.remove(clientId);
    }

    public String getClientIdByUserId(Integer userId) {
        for (Map.Entry<String, Map<String, Object>> entry : sessions.entrySet()) {
            String clientId = entry.getKey();
            Map<String, Object> sessionData = entry.getValue();

            Integer sessionUserId = (Integer) sessionData.get("userId");
            if (sessionUserId != null && sessionUserId.equals(userId)) {
                return clientId;
            }
        }
        return null;
    }
}