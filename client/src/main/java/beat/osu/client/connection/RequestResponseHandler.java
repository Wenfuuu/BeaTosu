package beat.osu.client.connection;

import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.ResponseMessage;
import lombok.AllArgsConstructor;

import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class RequestResponseHandler {
    private final ObjectOutputStream oos;
    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<Object> sendRequest(RequestMessage request) {
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        try {
            oos.writeObject(request);
            oos.flush();
        } catch (Exception e) {
            pendingRequests.remove(requestId);
            future.completeExceptionally(e);
        }

        return future;
    }

    public void handleResponse(ResponseMessage response) {
        CompletableFuture<Object> future = pendingRequests.remove(response.getRequestId());
        if (future != null) {
            future.complete(response.getPayload());
        }
    }
}
