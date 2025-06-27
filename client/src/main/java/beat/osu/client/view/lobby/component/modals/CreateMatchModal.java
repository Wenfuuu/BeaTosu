package beat.osu.client.view.lobby.component.modals;

import beat.osu.client.controller.MatchController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.match.responses.CreateMatchResponse;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;
import java.util.concurrent.ExecutionException;

public class CreateMatchModal extends VBox {

    private Label titleLabel;

    private VBox createMatchForm;
    private Label gameLabel;
    private TextField gameTextField;
    private CheckBox passwordCheckBox;
    private HBox passwordBox;
    private Label passwordLabel;
    private PasswordField passwordField;
    private Label maxPlayersLabel;
    private ComboBox<String> maxPlayersComboBox;

    @Getter
    private Button startGameButton;
    @Getter
    private Button cancelButton;

    private VBox buttonsContainer;

    private final MatchController matchController;

    public CreateMatchModal(MatchController matchController) {
        this.matchController = matchController;

        initializeComponents();
        setLayout();
        loadStyles();
        setupEventHandlers();

        this.setVisible(false);
    }

    private void initializeComponents() {
        this.getStyleClass().add("root");

        titleLabel = new Label("Create New Game...");
        titleLabel.getStyleClass().add("title-label");

        gameLabel = new Label("Game Name:");
        gameLabel.getStyleClass().add("game-label");

        gameTextField = new TextField();
        gameTextField.getStyleClass().add("game-input");

        passwordCheckBox = new CheckBox("Require password to join");
        passwordCheckBox.getStyleClass().add("password-checkbox");

        passwordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            passwordBox.setVisible(newVal);
        });

        passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("password-label");

        passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-input");

        createMatchForm = new VBox();
        createMatchForm.getStyleClass().add("create-match-form");

        maxPlayersLabel = new Label("Max Players:");
        maxPlayersLabel.getStyleClass().add("max-players-label");

        maxPlayersComboBox = new ComboBox<>();
        maxPlayersComboBox.getItems().addAll("2 players", "3 players", "4 players",
                "5 players", "6 players", "7 players",
                "8 players", "16 players");

        maxPlayersComboBox.getStyleClass().add("dark-combo-box");

        startGameButton = new Button("1. Start Game");
        cancelButton = new Button("2. Cancel");

        startGameButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);
        cancelButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.52);

        startGameButton.getStyleClass().addAll("main-button", "start-game-button");
        cancelButton.getStyleClass().addAll("main-button", "cancel-button");

        buttonsContainer = new VBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.getChildren().addAll(startGameButton, cancelButton);
    }

    private void setLayout() {
        this.getChildren().add(titleLabel);

        HBox gameNameBox = new HBox();
        gameNameBox.setAlignment(Pos.CENTER_LEFT);
        gameNameBox.getStyleClass().add("game-name-box");
        gameNameBox.getChildren().addAll(gameLabel, gameTextField);

        passwordBox = new HBox();
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        passwordBox.getStyleClass().add("password-box");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        passwordBox.setVisible(false);

        HBox maxPlayersBox = new HBox();
        maxPlayersBox.setAlignment(Pos.CENTER_LEFT);
        maxPlayersBox.getStyleClass().add("max-players-box");
        maxPlayersBox.getChildren().addAll(maxPlayersLabel, maxPlayersComboBox);

        createMatchForm.getChildren().addAll(gameNameBox, passwordCheckBox, passwordBox, maxPlayersBox);

        this.getChildren().addAll(createMatchForm, buttonsContainer);
        VBox.setMargin(createMatchForm, new Insets(156, 0, 0, 0));
        VBox.setMargin(buttonsContainer, new Insets(84, 0, 0, 0));
    }

    private void loadStyles() {
        try {
            URL cssUrl = CssManager.getLobbyCssURL("CreateMatchModal.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load CreateMatchModal CSS: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        startGameButton.setOnAction(event -> {
            String gameName = gameTextField.getText();
            String password = passwordField.getText();
            String maxPlayersSelection = maxPlayersComboBox.getSelectionModel().getSelectedItem();
            int maxPlayers = Integer.parseInt(maxPlayersSelection.split(" ")[0]);

            if (gameName.isEmpty()) {
                Toast.error("Please enter the game name!");
                return;
            }

            if (passwordCheckBox.isSelected() && password.isEmpty()) {
                Toast.error("Please enter the password!");
                return;
            }

            try {
                Result<CreateMatchResponse> response = matchController.createMatch(gameName, password, maxPlayers).get();
                if (response.isSuccess()) {
                    CreateMatchResponse joinResponse = response.getValue();
                    Toast.success("Successfully joined lobby: " + joinResponse.getMessage()).show();
                    ViewManager.getInstance().showMatchView(joinResponse.getMatch());
                } else {
                    Toast.error("Failed to join match: " + response.getError().getMessage()).show();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            hide();
        });
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
        });

        fadeOut.play();
    }

    public void show() {
        gameTextField.clear();
        passwordField.clear();
        maxPlayersComboBox.getSelectionModel().selectFirst();
        passwordCheckBox.setSelected(false);

        String username = AuthManager.getUser().getUsername();
        gameTextField.setText(username + "'s game");

        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}
