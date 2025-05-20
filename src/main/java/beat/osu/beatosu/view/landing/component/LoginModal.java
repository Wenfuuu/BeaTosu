package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.controller.AuthController;
import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.model.User;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.function.Consumer;

public class LoginModal extends StackPane {

    private AuthController authController;

    private VBox formContainer;
    private TranslateTransition slideIn;
    private TranslateTransition slideOut;
    private boolean isModalVisible = false;

    private TextField userInput;
    private PasswordField passInput;
    private Button signInButton;
    private Button createAccountButton;
    private Button backButton;
    private Label titleLabel; // Added for access if needed

    private Consumer<User> onLoginSuccessListener;
    private Runnable onCreateAccountListener;
    private Runnable onHideListener; // If MenuPage needs to know when it's hidden by back button

    public LoginModal() {
        this.authController = new AuthController();

        initialize();
        setupAnimations();
        handleComponentEvents();

         URL cssUrl = CssManager.getCssURL("LoginModal.css");
         if (cssUrl != null) {
             this.getStylesheets().add(cssUrl.toExternalForm());
         } else {
             System.err.println("LoginModal.css file not found!");
         }

        this.setVisible(false); // Initially hidden
        this.setManaged(false); // Initially not managed for layout
    }

    private void initialize() {
        this.getStyleClass().add("login-modal-background"); // Dimming background style

        formContainer = new VBox(20);
        formContainer.getStyleClass().add("login-form-container");
        formContainer.setMaxWidth(400);
        formContainer.setMaxHeight(Region.USE_PREF_SIZE);
        formContainer.setAlignment(Pos.TOP_CENTER);
        StackPane.setMargin(formContainer, new Insets(0, 0, 0, 30)); // Original margin

        titleLabel = new Label("SIGN IN");
        titleLabel.getStyleClass().add("login-title");
        titleLabel.setPadding(new Insets(0, 0, 10, 0));

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("login-label");
        userInput = new TextField();
        userInput.setPromptText("Enter your username");
        userInput.getStyleClass().add("login-input");
        VBox userBox = new VBox(5, userLabel, userInput);

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("login-label");
        passInput = new PasswordField();
        passInput.setPromptText("Enter your password");
        passInput.getStyleClass().add("login-input");
        VBox passBox = new VBox(5, passLabel, passInput);

        signInButton = new Button("Sign In");
        signInButton.getStyleClass().addAll("login-button");
        signInButton.setMaxWidth(Double.MAX_VALUE);

        createAccountButton = new Button("Create an account");
        createAccountButton.getStyleClass().addAll("login-button");
        createAccountButton.setMaxWidth(Double.MAX_VALUE);

        backButton = new Button("〈 back");
        backButton.getStyleClass().add("back-button");
        StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);

        formContainer.getChildren().addAll(
                titleLabel,
                userBox,
                passBox,
                signInButton,
                createAccountButton
        );

        this.getChildren().addAll(formContainer, backButton);
        StackPane.setAlignment(formContainer, Pos.CENTER);
    }

    private void setupAnimations() {
        // Slide-in animation
        slideIn = new TranslateTransition(Duration.millis(150), this);
        slideIn.setToX(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        slideIn.setOnFinished(e -> isModalVisible = true);

        // Slide-out animation
        slideOut = new TranslateTransition(Duration.millis(150), this);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        slideOut.setOnFinished(event -> {
            super.setVisible(false); // Use super to avoid recursion if setVisible is overridden
            super.setManaged(false);
            isModalVisible = false;
            if (onHideListener != null) {
                onHideListener.run();
            }
        });
    }

    private void handleComponentEvents() {
        signInButton.setOnAction(e -> {
            String username = userInput.getText();
            String pass = passInput.getText();
            // Consider moving UserController.loginUser and AuthContext to be passed in or handled by listener
//            User user = UserController.loginUser(username, pass);
//            if (user != null) {
//                if (onLoginSuccessListener != null) {
//                    onLoginSuccessListener.accept(AuthContext.getUser()); // Pass logged-in user
//                }
//                hide(); // Hide modal on successful login
//            } else {
//                // Handle login failure (e.g., show error message within the modal)
//                System.out.println("Invalid credentials (from LoginModal)");
//                // You might want to add a label to formContainer to show this error
//            }
        });

        createAccountButton.setOnAction(e -> {
            if (onCreateAccountListener != null) {
                onCreateAccountListener.run();
            }
            // Optionally hide this modal when register modal is shown
            // hide();
        });

        backButton.setOnAction(e -> hide());
    }

    public void show() {
        if (isModalVisible || (slideIn != null && slideIn.getStatus() == javafx.animation.Animation.Status.RUNNING) || (slideOut != null && slideOut.getStatus() == javafx.animation.Animation.Status.RUNNING)) {
            return;
        }
        this.setTranslateX(-500); // Initial off-screen position
        super.setManaged(true);
        super.setVisible(true);
        this.toFront();

        slideIn.setFromX(this.getTranslateX());
        slideIn.play();
    }

    public void hide() {
        if (!isModalVisible || (slideOut != null && slideOut.getStatus() == javafx.animation.Animation.Status.RUNNING)) {
            return;
        }
        isModalVisible = false; // Mark as not visible conceptually at start of animation

        slideOut.setFromX(this.getTranslateX()); // Should be 0
        slideOut.setToX(-500); // Target off-screen position
        slideOut.play();
    }

    public boolean isShowing() {
        return isModalVisible || (slideIn != null && slideIn.getStatus() == javafx.animation.Animation.Status.RUNNING);
    }

    // --- Event Listeners ---
    public void setOnLoginSuccessListener(Consumer<User> listener) {
        this.onLoginSuccessListener = listener;
    }

    public void setOnCreateAccountListener(Runnable listener) {
        this.onCreateAccountListener = listener;
    }

    public void setOnHideListener(Runnable listener) {
        this.onHideListener = listener;
    }

    // Optional: provide access to internal fields if MenuPage needs to clear them
    public void clearFields() {
        userInput.clear();
        passInput.clear();
    }
}
