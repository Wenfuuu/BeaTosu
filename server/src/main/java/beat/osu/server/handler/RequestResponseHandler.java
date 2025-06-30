package beat.osu.server.handler;

import java.io.ObjectOutputStream;

import beat.osu.server.router.MessageRouter;
import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.ResponseMessage;

public class RequestResponseHandler {
    private final MessageRouter messageRouter;
    private final ObjectOutputStream outputStream;
    private int responsesSent = 0;
    private static final int RESET_INTERVAL = 50; // Reset stream every 50 responses
    
    public RequestResponseHandler(MessageRouter messageRouter, ObjectOutputStream outputStream) {
        this.messageRouter = messageRouter;
        this.outputStream = outputStream;
    }

    public void handleRequest(RequestMessage request, String clientId) {
        try {
            Object result = messageRouter.routeRequestMessage(request, clientId);
            
            ResponseMessage response = new ResponseMessage(
                request.getRequestId(), 
                result, 
                System.currentTimeMillis()
            );
            
            synchronized (outputStream) {
                outputStream.writeObject(response);
                outputStream.flush();
                
                responsesSent++;
                if (responsesSent >= RESET_INTERVAL) {
                    outputStream.reset();
                    responsesSent = 0;
                }
            }
        } catch (Exception e) {
            System.err.println("RequestResponseHandler: Error handling request: " + e.getMessage());
            
            if (!isConnectionError(e)) {
                try {
                    ResponseMessage errorResponse = new ResponseMessage(
                        request.getRequestId(),
                        "Server error: " + e.getMessage(),
                        System.currentTimeMillis()
                    );
                    synchronized (outputStream) {
                        outputStream.writeObject(errorResponse);
                        outputStream.flush();
                    }
                } catch (Exception sendError) {
                    System.err.println("RequestResponseHandler: Failed to send error response: " + sendError.getMessage());
                }
            }
        }
    }
    
    private boolean isConnectionError(Exception e) {
        String message = e.getMessage();
        return message != null && (
            message.contains("connection abort") ||
            message.contains("socket write error") ||
            message.contains("Broken pipe") ||
            message.contains("Connection reset")
        );
    }
}
