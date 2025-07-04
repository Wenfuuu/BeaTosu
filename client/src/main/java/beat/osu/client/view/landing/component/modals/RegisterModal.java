package beat.osu.client.view.landing.component.modals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import beat.osu.client.controller.AuthController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.dto.auth.responses.RegisterResponse;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class RegisterModal extends StackPane {

    private AuthController authController;

    private VBox root;
    private Label title;
    private VBox inputBox;

    private VBox usernameBox;
    private Label usernameLabel;
    private TextField usernameField;
    private HBox usernameInputBox;
    private Label usernameHint;

    private VBox emailBox;
    private Label emailLabel;
    private TextField emailField;
    private HBox emailInputBox;
    private Label emailHint;

    private VBox passwordBox;
    private Label passwordLabel;
    private PasswordField passwordField;
    private HBox passwordInputBox;
    private Label passwordHint;

    private VBox profilePictureBox;
    private Label profilePictureLabel;
    private ImageView profileImageView;
    private StackPane imageContainer;
    private Label placeholderLabel;
    private Label profilePictureHint;
    private File selectedImageFile;

    private VBox supporterBox;
    private CheckBox supporterCheckBox;
    private Label supporterLabel;

    private VBox buttonBox;
    private Button createButton;
    private Button cancelButton;

    public RegisterModal() {
        this.authController = new AuthController();

        initialize();
        setLayout();
        handleEvents();

        this.getChildren().add(root);
        this.setVisible(false);

        URL cssUrl = CssManager.getLandingCssURL("RegisterModal.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void initialize() {
        root = new VBox(15);
        root.getStyleClass().add("root-register");

        inputBox = new VBox(20);
        inputBox.getStyleClass().add("input-box");

        title = new Label("Account Creation");
        title.getStyleClass().add("title");

        usernameBox = new VBox(5);
        usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("label");
        usernameField = new TextField();
        usernameInputBox = new HBox(10, usernameLabel, usernameField);
        usernameHint = new Label("Others will recognise you by this name. Make sure you are happy with it!");
        usernameHint.getStyleClass().add("hint");

        emailBox = new VBox(5);
        emailLabel = new Label("Email Address:");
        emailLabel.getStyleClass().add("label");
        emailField = new TextField();
        emailInputBox = new HBox(10, emailLabel, emailField);
        emailHint = new Label(
                "Will be used for notifications, account verification and in the case you forget your password. No spam, ever. Make sure to get it right!");
        emailHint.setWrapText(true);
        emailHint.getStyleClass().add("hint");

        passwordBox = new VBox(5);
        passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("label");
        passwordField = new PasswordField();
        passwordInputBox = new HBox(10, passwordLabel, passwordField);
        passwordHint = new Label(
                "At least 8 characters long. Choose something long but also something you will remember, like a line from your favourite song.");
        passwordHint.setWrapText(true);
        passwordHint.getStyleClass().add("hint");

        profilePictureBox = new VBox(8);
        profilePictureBox.setAlignment(Pos.CENTER_LEFT);
        profilePictureBox.getStyleClass().add("profile-picture-box");
        profilePictureLabel = new Label("Profile Picture:");
        profilePictureLabel.getStyleClass().add("label");

        profileImageView = new ImageView();
        profileImageView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.30);
        profileImageView.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.35);
        profileImageView.setPreserveRatio(true);

        placeholderLabel = new Label("Click here to upload image");
        placeholderLabel.getStyleClass().add("upload-placeholder");

        imageContainer = new StackPane();
        imageContainer.getChildren().addAll(profileImageView, placeholderLabel);
        imageContainer.getStyleClass().add("image-upload-container");
        imageContainer.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.30);
        imageContainer.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.30);
        imageContainer.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.33);
        imageContainer.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.33);

        profilePictureHint = new Label(
                "Optional: Upload a profile picture. It will be displayed on your profile and in game lobbies.");
        profilePictureHint.setWrapText(true);
        profilePictureHint.getStyleClass().add("hint");

        supporterBox = new VBox(5);
        supporterCheckBox = new CheckBox("Register as osu! supporter");
        supporterCheckBox.getStyleClass().add("supporter-checkbox");
        supporterLabel = new Label(
                "Optional: Become an osu! supporter to help keep the game free and get some cool perks!");
        supporterLabel.setWrapText(true);
        supporterLabel.getStyleClass().add("hint");

        buttonBox = new VBox(20);
        buttonBox.getStyleClass().add("button-box");

        createButton = new Button("1. Create my account!");
        createButton.setPrefWidth(250);
        createButton.getStyleClass().addAll("button", "create-button");

        cancelButton = new Button("2. Cancel");
        cancelButton.setPrefWidth(250);
        cancelButton.getStyleClass().addAll("button", "cancel-button");

        usernameField.getStyleClass().add("text-field");
        emailField.getStyleClass().add("text-field");
        passwordField.getStyleClass().add("password-field");

        usernameInputBox.getStyleClass().add("hbox");
        emailInputBox.getStyleClass().add("hbox");
        passwordInputBox.getStyleClass().add("hbox");
        imageContainer.getStyleClass().add("image-upload-container");
        profileImageView.getStyleClass().add("profile-image-view");
    }

    private void setLayout() {
        usernameBox.getChildren().addAll(usernameInputBox, usernameHint);
        emailBox.getChildren().addAll(emailInputBox, emailHint);
        passwordBox.getChildren().addAll(passwordInputBox, passwordHint);
        profilePictureBox.getChildren().addAll(profilePictureLabel, imageContainer, profilePictureHint);
        supporterBox.getChildren().addAll(supporterCheckBox, supporterLabel);
        buttonBox.getChildren().addAll(createButton, cancelButton);

        inputBox.getChildren().addAll(usernameBox, emailBox, passwordBox, profilePictureBox, supporterBox);
        root.getChildren().addAll(title, inputBox, buttonBox);
    }

    private void setupInputFieldSounds() {
        // Username field sound effects
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
        });

        // Email field sound effects
        emailField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
        });

        // Password field sound effects
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
        });
    }

    private void handleEvents() {
        // Add keyboard sound effects for input fields
        setupInputFieldSounds();

        createButton.setOnMouseEntered(e -> {
            SfxManager.playSfx("menuhover.wav");
        });

        supporterCheckBox.setOnAction(e -> {
            SfxManager.playSfx("menuhit.wav");
        });

        createButton.setOnMouseClicked(e -> {
            SfxManager.playSfx("menuhit.wav");
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            byte[] profilePicture = null;
            boolean isSupporter = supporterCheckBox.isSelected();

            if (selectedImageFile != null) {
                try {
                    profilePicture = Files.readAllBytes(selectedImageFile.toPath());
                } catch (IOException ex) {
                    System.err.println("Failed to read profile picture: " + ex.getMessage());
                    return;
                }
            }

            authController.register(username, password, email, profilePicture, isSupporter)
                    .thenAcceptAsync(result -> {
                        Platform.runLater(() -> {
                            if (result.isSuccess()) {
                                RegisterResponse response = result.getValue();
                                this.setVisible(false);
                                Toast.success(response.getMessage()).show();
                            } else {
                                Toast.error(result.getError().getMessage()).show();
                            }
                        });
                    });
        });

        cancelButton.setOnMouseEntered(e -> {
            SfxManager.playSfx("menuhover.wav");
        });

        cancelButton.setOnMouseClicked(e -> {
            SfxManager.playSfx("menuback.wav");
            // clear all input fields
            usernameField.clear();
            emailField.clear();
            passwordField.clear();
            profileImageView.setImage(null);
            placeholderLabel.setText("Click here to upload image");
            supporterCheckBox.setSelected(false);
            this.setVisible(false);
        });

        imageContainer.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));

            selectedImageFile = fileChooser.showOpenDialog(this.getScene().getWindow());

            if (selectedImageFile != null) {
                Image image = new Image(selectedImageFile.toURI().toString());
                profileImageView.setImage(image);
                placeholderLabel.setVisible(false);
            }
        });
    }
}