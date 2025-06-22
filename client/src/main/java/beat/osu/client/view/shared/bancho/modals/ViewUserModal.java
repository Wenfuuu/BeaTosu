package beat.osu.client.view.shared.bancho.modals;

import java.net.URL;
import java.util.function.Consumer;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.shared.bancho.cards.UserCard;
import beat.osu.shared.dto.chat.PrivateChatDto;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ViewUserModal extends VBox {

    private UserCard userCard;
    private Button startSpectateButton;
    private Button startChatButton;
    private Button closeButton;
    private VBox buttonsContainer;
    
    private Consumer<PrivateChatDto> onStartChatCallback;

    public ViewUserModal() {
        initializeComponents();
        setLayout();
        setupStyling();

        this.setVisible(false);
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

//        startSpectateButton.setOnAction(event -> startSpectating());
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
    
    public void setOnStartChatCallback(Consumer<PrivateChatDto> callback) {
        this.onStartChatCallback = callback;
    }
}