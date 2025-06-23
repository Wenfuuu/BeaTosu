package beat.osu.server.service;

import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.session.requests.CreateSessionDataRequest;
import beat.osu.shared.dto.session.requests.GetSessionDataRequest;
import beat.osu.shared.dto.session.requests.RemoveSessionDataRequest;
import beat.osu.shared.dto.session.responses.CreateSessionDataResponse;
import beat.osu.shared.dto.session.responses.GetSessionDataResponse;
import beat.osu.shared.dto.session.responses.RemoveSessionDataResponse;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SessionService {
    private final ConcurrentHashMap<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    public void initializeClientSession(String clientId) {
        sessions.put(clientId, new ConcurrentHashMap<>());
    }

    public void removeClientSession(String clientId) {
        sessions.remove(clientId);
    }

    public void setSessionData(String clientId, String key, Object value) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            session.put(key, value);
        }
    }

    public void removeSessionValue(String clientId, String key) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            session.remove(key);
        }
    }

    public Object getSessionValue(String clientId, String key) {
        Map<String, Object> session = sessions.get(clientId);
        if (session != null) {
            return session.get(key);
        }
        return null;
    }

    public Result<CreateSessionDataResponse> createSessionData(CreateSessionDataRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Create session data is missing"));
        }

        System.out.println("Creating session data - Looking for client with userId: " + request.getUserId());
        System.out.println("Current sessions count: " + sessions.size());

        String clientId = getClientIdByUserId(request.getUserId());
        if (clientId == null) {
            System.err.println(
                    "CRITICAL: Client not found for user ID: " + request.getUserId() + " during session creation");
            String warningMessage = "Warning: Client session not found for user ID: " + request.getUserId() +
                    ", but continuing. This might be a timing issue.";
            System.out.println(warningMessage);
            return Result.success(new CreateSessionDataResponse(warningMessage));
        }

        System.out.println("Found clientId: " + clientId + " for userId: " + request.getUserId());
        setSessionData(clientId, request.getKey(), request.getValue());

        String message = "Session created successfully for client ID: " + clientId + " with key: " + request.getKey()
                + " and value: " + request.getValue();
        System.out.println(message);
        return Result.success(new CreateSessionDataResponse(message));
    }

    public Result<RemoveSessionDataResponse> removeSessionData(RemoveSessionDataRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Remove session data is missing"));
        }

        System.out.println("Removing session data - Looking for client with userId: " + request.getUserId());

        String clientId = getClientIdByUserId(request.getUserId());
        if (clientId == null) {
            System.err.println(
                    "CRITICAL: Client not found for user ID: " + request.getUserId() + " during session removal");
            String warningMessage = "Warning: Client session not found for user ID: " + request.getUserId() +
                    " during removal. Session might already be cleaned up.";
            System.out.println(warningMessage);
            return Result.success(new RemoveSessionDataResponse(warningMessage));
        }

        System.out.println("Found clientId: " + clientId + " for userId: " + request.getUserId());
        removeSessionValue(clientId, request.getKey());

        String message = "Session removed successfully for client ID: " + clientId + " with key: " + request.getKey();
        System.out.println(message);
        return Result.success(new RemoveSessionDataResponse(message));
    }

    public Result<GetSessionDataResponse> getSessionData(GetSessionDataRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Get session data is missing"));
        }

        String clientId = getClientIdByUserId(request.getUserId());
        if (clientId == null) {
            System.err.println(
                    "CRITICAL: Client not found for user ID: " + request.getUserId() + " during session creation");
            String warningMessage = "Warning: Client session not found for user ID: " + request.getUserId() +
                    ", but continuing. This might be a timing issue.";
            System.out.println(warningMessage);
            return Result.success(new GetSessionDataResponse(null, warningMessage));
        }

        Object value = getSessionValue(clientId, request.getKey());
        String message = "Session data retrieved successfully for client ID: " + clientId +
                " with key: " + request.getKey() + " and value: " + value;
        return Result.success(new GetSessionDataResponse(value, message));
    }

    public String getClientIdByUserId(Integer userId) {
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