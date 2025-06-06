package beat.osu.client.controller;

import beat.osu.client.helper.LocaleManager;
import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.auth.responses.LoginResponse;
import beat.osu.shared.dto.auth.responses.RegisterResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.RequestMessage;

import java.util.concurrent.CompletableFuture;

public class AuthController {
    private final ClientService clientService;

    public AuthController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<RegisterResponse>> register(String username, String password, String email, byte[] profilePicture) {
        String countryCode = LocaleManager.getCurrentCountry();

        RegisterRequest requestData = new RegisterRequest(username, password, email, countryCode, profilePicture);
        RequestMessage request = new RequestMessage(
                MessageType.AUTH,
                MessageAction.REGISTER,
                requestData
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((RegisterResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<LoginResponse>> login(String username, String password) {
        LoginRequest requestData = new LoginRequest(username, password);
        RequestMessage request = new RequestMessage(
                MessageType.AUTH,
                MessageAction.LOGIN,
                requestData
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((LoginResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
}