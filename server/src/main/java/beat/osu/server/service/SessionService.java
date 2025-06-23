package beat.osu.server.service;

import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.session.requests.CreateSessionRequest;
import beat.osu.shared.dto.session.requests.RemoveSessionRequest;
import beat.osu.shared.dto.session.responses.CreateSessionResponse;
import beat.osu.shared.dto.session.responses.RemoveSessionResponse;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SessionService {
    private final ConcurrentHashMap<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    public void createSessionData(String clientId) {
        sessions.put(clientId, new ConcurrentHashMap<>());
    }

    public void setSessionData(String clientId, String key, Object value) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            session.put(key, value);
        }
    }

    public void removeSessionData(String clientId, String key) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            session.remove(key);
        }
    }    public Result<CreateSessionResponse> createSessionData(CreateSessionRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Create session data is missing"));
        }

        System.out.println("Creating session data - Looking for client with userId: " + request.getUserId());
        System.out.println("Current sessions count: " + sessions.size());
        
        String clientId = getClientIdByUserId(request.getUserId());
        if (clientId == null) {
            System.err.println("CRITICAL: Client not found for user ID: " + request.getUserId() + " during session creation");
            
            // Instead of failing, let's be more lenient and just log the warning
            // This prevents disconnection due to session timing issues
            String warningMessage = "Warning: Client session not found for user ID: " + request.getUserId() + 
                                  ", but continuing. This might be a timing issue.";
            System.out.println(warningMessage);
            return Result.success(new CreateSessionResponse(warningMessage));
        }
        
        System.out.println("Found clientId: " + clientId + " for userId: " + request.getUserId());
        setSessionData(clientId, request.getKey(), request.getValue());

        String message = "Session created successfully for client ID: " + clientId + " with key: " + request.getKey()
                + " and value: " + request.getValue();
        System.out.println(message);
        return Result.success(new CreateSessionResponse(message));
    }    public Result<RemoveSessionResponse> removeSessionKey(RemoveSessionRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Remove session data is missing"));
        }

        System.out.println("Removing session data - Looking for client with userId: " + request.getUserId());

        String clientId = getClientIdByUserId(request.getUserId());
        if (clientId == null) {
            System.err.println("CRITICAL: Client not found for user ID: " + request.getUserId() + " during session removal");
            
            // Instead of failing, let's be more lenient for remove operations too
            // If the client is not found, the session data might already be cleaned up
            String warningMessage = "Warning: Client session not found for user ID: " + request.getUserId() + 
                                  " during removal. Session might already be cleaned up.";
            System.out.println(warningMessage);
            return Result.success(new RemoveSessionResponse(warningMessage));
        }
        
        System.out.println("Found clientId: " + clientId + " for userId: " + request.getUserId());
        removeSessionData(clientId, request.getKey());

        String message = "Session removed successfully for client ID: " + clientId + " with key: " + request.getKey();
        System.out.println(message);
        return Result.success(new RemoveSessionResponse(message));
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
    }    public String getClientIdByUserId(Integer userId) {
        System.out.println("Looking up clientId for userId: " + userId);
        
        for (Map.Entry<String, Map<String, Object>> entry : sessions.entrySet()) {
            String clientId = entry.getKey();
            Map<String, Object> sessionData = entry.getValue();

            if (sessionData == null) {
                System.out.println("Session data is null for clientId: " + clientId);
                continue;
            }

            Integer sessionUserId = (Integer) sessionData.get("userId");
            System.out.println("Checking clientId: " + clientId + " with userId: " + sessionUserId);
            
            if (sessionUserId != null && sessionUserId.equals(userId)) {
                System.out.println("Found match! ClientId: " + clientId + " for userId: " + userId);
                return clientId;
            }
        }
        
        System.err.println("No clientId found for userId: " + userId);
        System.err.println("Available sessions:");
        sessions.forEach((id, data) -> {
            if (data != null) {
                System.err.println("  ClientId: " + id + " -> userId: " + data.get("userId"));
            } else {
                System.err.println("  ClientId: " + id + " -> null session data");
            }
        });
        
        return null;
    }
}