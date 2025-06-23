package beat.osu.client.view.shared.bancho.modals;

import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

import beat.osu.client.controller.SessionController;
import beat.osu.client.controller.SpectateController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.PrivateChatDto;
import beat.osu.shared.dto.game.SpectateDto;
import beat.osu.shared.dto.score.ScoreDto;
import beat.osu.shared.dto.score.responses.GetAllScoresResponse;
import beat.osu.shared.dto.session.responses.GetSessionDataResponse;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class ViewUserModal extends VBox {

    private UserCard userCard;
    private Button startSpectateButton;
    private Button startChatButton;
    private Button closeButton;
    private VBox buttonsContainer;

    private SessionController sessionController;

    @Setter
    private Consumer<PrivateChatDto> onStartChatCallback;
    @Setter
    private Consumer<SpectateDto> onStartSpectateCallback;

    public ViewUserModal(SessionController sessionController) {
        this.sessionController = sessionController;
        initializeComponents();
        setLayout();
        setupStyling();

        this.setVisible(false);
    }

    private Integer fetchPlayingBeatmapId(int userId) {
        try {
            Result<GetSessionDataResponse> result = sessionController.getSessionValue(userId, "playingBeatmap").get();

            if (result.isSuccess()) {
                Object sessionValue = result.getValue().getValue();
                if (sessionValue != null) {
                    if (sessionValue instanceof Integer) {
                        System.out.println("Fetched playing beatmap ID: " + sessionValue);
                        return (Integer) sessionValue;
                    } else {
                        System.err.println("Playing beatmap session value is not a number: " + sessionValue.getClass().getSimpleName());
                        return null;
                    }
                } else {
                    System.out.println("User is not currently playing a beatmap");
                    return null;
                }
            } else {
                System.err.println("Failed to fetch playing beatmap session: " + result.getError().getMessage());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error fetching beatmap session: " + e.getMessage());
            return null;
        }
    }

    private void initializeComponents() {
        userCard = new UserCard();

        startSpectateButton = new Button("1. Start Spectating");
        startChatButton = new Button("2. Start Chat");
        closeButton = new Button("3. Close");

        startSpectateButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        startChatButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        closeButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);

        startSpectateButton.getStyleClass().addAll("modal-button", "spectate-button");
        startChatButton.getStyleClass().addAll("modal-button", "chat-button");
        closeButton.getStyleClass().addAll("modal-button", "close-button");

        closeButton.setOnAction(event -> hide());

        buttonsContainer = new VBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.getChildren().addAll(startSpectateButton, startChatButton, closeButton);

        startSpectateButton.setOnAction(event -> {
            if (onStartSpectateCallback != null && userCard != null) {
                UserDto user = AuthManager.getUser();
                Integer beatmapId = fetchPlayingBeatmapId(userCard.getUserId());

                if (beatmapId != null) {
                    SpectateDto spectateDto = new SpectateDto(
                            user.getId(),
                            userCard.getUserId(),
                            beatmapId
                    );
                    onStartSpectateCallback.accept(spectateDto);
                    hide(); // Close the modal after starting spectate
                } else {
                    System.err.println("Cannot start spectating: No beatmap is currently being played");
                }
            }
        });

        startChatButton.setOnAction(event -> {
            if (onStartChatCallback != null && userCard != null) {
                PrivateChatDto privateChat = new PrivateChatDto(
                    userCard.getUserId(),
                    userCard.getUsername()
                );
                onStartChatCallback.accept(privateChat);
                hide(); // Close the modal after starting chat
            }
        });
    }

    private void setLayout() {
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(new Insets(40, 0, 0, 15));
        this.setSpacing(30);

        this.getChildren().addAll(userCard, buttonsContainer);
        this.getStyleClass().add("view-user-modal");
    }

    private void setupStyling() {
        try {
            URL cssUrl = CssManager.getSharedCssURL("ViewUserModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load ViewUserModal CSS: " + e.getMessage());
        }
    }

    public void show() {
        if (userCard == null) {
            System.err.println("UserCard is not set. Cannot show modal.");
            return;
        }

        this.setVisible(true);
        this.setManaged(true);
        this.setOpacity(0);
        this.toFront();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
            this.setManaged(false);
        });
        fadeOut.play();
    }

    public boolean isShowing() {
        return this.isVisible();
    }

    public void updateUserCard(UserCard newUserCard) {
        this.getChildren().remove(userCard);
        this.userCard = newUserCard;
        this.getChildren().add(0, userCard);
    }
}