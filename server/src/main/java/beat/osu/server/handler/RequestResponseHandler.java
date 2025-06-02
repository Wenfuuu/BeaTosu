package beat.osu.server.handler;

import beat.osu.server.router.MessageRouter;
import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.ResponseMessage;

import java.io.ObjectOutputStream;

public class RequestResponseHandler {
    private final MessageRouter messageRouter;
    private final ObjectOutputStream outputStream;
    
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
            
            outputStream.writeObject(response);
            outputStream.flush();
            
        } catch (Exception e) {
            System.err.println("RequestResponseHandler: Error handling request: " + e.getMessage());
            
            try {
                ResponseMessage errorResponse = new ResponseMessage(
                    request.getRequestId(),
                    "Server error: " + e.getMessage(),
                    System.currentTimeMillis()
                );
                outputStream.writeObject(errorResponse);
                outputStream.flush();
            } catch (Exception sendError) {
                System.err.println("RequestResponseHandler: Failed to send error response: " + sendError.getMessage());
            }
        }
    }
}
