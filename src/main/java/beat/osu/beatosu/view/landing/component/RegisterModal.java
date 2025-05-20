package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.controller.AuthController;
import beat.osu.beatosu.dto.user.RegisterResult;
import beat.osu.beatosu.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;

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

    private VBox buttonBox;
    private Button createButton;
    private Button cancelButton;

    public RegisterModal() {
        this.authController = new AuthController();

        initialize();
        setLayout();
        handleEvents();

        // Add the root VBox to this StackPane
        this.getChildren().add(root);
        this.setVisible(false);

        // Apply stylesheet
        URL cssUrl = CssManager.getCssURL("RegisterModal.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void initialize() {
        // Root
        root = new VBox(15);
        root.getStyleClass().add("root-register");

        inputBox = new VBox(20);
        inputBox.getStyleClass().add("input-box");

        // Title
        title = new Label("Account Creation");
        title.getStyleClass().add("title");

        // Username
        usernameBox = new VBox(5);
        usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("label");
        usernameField = new TextField();
        usernameInputBox = new HBox(10, usernameLabel, usernameField);
        usernameHint = new Label("Others will recognise you by this name. Make sure you are happy with it!");
        usernameHint.getStyleClass().add("hint");

        // Email
        emailBox = new VBox(5);
        emailLabel = new Label("Email Address:");
        emailLabel.getStyleClass().add("label");
        emailField = new TextField();
        emailInputBox = new HBox(10, emailLabel, emailField);
        emailHint = new Label("Will be used for notifications, account verification and in the case you forget your password. No spam, ever. Make sure to get it right!");
        emailHint.setWrapText(true);
        emailHint.getStyleClass().add("hint");

        // Password
        passwordBox = new VBox(5);
        passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("label");
        passwordField = new PasswordField();
        passwordInputBox = new HBox(10, passwordLabel, passwordField);
        passwordHint = new Label("At least 8 characters long. Choose something long but also something you will remember, like a line from your favourite song.");
        passwordHint.setWrapText(true);
        passwordHint.getStyleClass().add("hint");

        // Buttons
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
    }

    private void setLayout() {
        usernameBox.getChildren().addAll(usernameInputBox, usernameHint);
        emailBox.getChildren().addAll(emailInputBox, emailHint);
        passwordBox.getChildren().addAll(passwordInputBox, passwordHint);
        buttonBox.getChildren().addAll(createButton, cancelButton);

        inputBox.getChildren().addAll(usernameBox, emailBox, passwordBox);
        root.getChildren().addAll(title, inputBox, buttonBox);
    }

    private void handleEvents() {
        createButton.setOnMouseClicked(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            RegisterResult result = authController.register(username, email, password);
            if(result.isSuccess()) {
                // success toast
                System.out.println(result.getMessage());
            }else {
                // error toast
                System.out.println(result.getMessage());
            }
        });

        cancelButton.setOnMouseClicked(e -> {
            this.setVisible(false);
        });
    }
}