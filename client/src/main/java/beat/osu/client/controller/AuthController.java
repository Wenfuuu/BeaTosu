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
import beat.osu.shared.models.Message;

import java.util.concurrent.CompletableFuture;

public class AuthController {
    private final ClientService clientService;

    public AuthController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<RegisterResponse>> register(String username, String password, String email) {
        String countryCode = LocaleManager.getCurrentCountry();

        RegisterRequest registerRequest = new RegisterRequest(username, password, email, countryCode);
        Message registerMessage = new Message(
                MessageType.AUTH,
                MessageAction.REGISTER,
                registerRequest,
                System.currentTimeMillis()
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendMessage(registerMessage).get();
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
        LoginRequest loginRequest = new LoginRequest(username, password);
        Message loginMessage = new Message(
                MessageType.AUTH,
                MessageAction.LOGIN,
                loginRequest,
                System.currentTimeMillis()
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendMessage(loginMessage).get();
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