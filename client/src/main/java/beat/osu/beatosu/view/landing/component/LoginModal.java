package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.controller.AuthController;
import beat.osu.beatosu.dto.user.LoginResult;
import beat.osu.beatosu.helper.AuthManager;
import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.model.User;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

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

    @Setter
    private Consumer<User> onLoginSuccessListener;
    @Setter
    private Runnable onCreateAccountListener;

    public LoginModal() {
        this.authController = new AuthController();

        initialize();
        setupAnimations();
        handleComponentEvents();

         URL cssUrl = CssManager.getLandingCssURL("LoginModal.css");
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
        slideIn.setOnFinished(e -> {
            isModalVisible = true;
            // Switch back to quality after animation
            formContainer.setCacheHint(CacheHint.DEFAULT);

            // Resume light rays if needed
            LightRays rays = findLightRays();
            if (rays != null) {
                rays.startUnifiedAnimation();
            }
        });

        // Slide-out animation with similar optimizations
        slideOut = new TranslateTransition(Duration.millis(150), this);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        slideOut.setOnFinished(event -> {
            super.setVisible(false); // Use super to avoid recursion if setVisible is overridden
            super.setManaged(false);
            isModalVisible = false;

            // Resume light rays if they were paused
            LightRays rays = findLightRays();
            if (rays != null) {
                rays.startUnifiedAnimation();
            }
        });

        // Cache the form container during animation for better performance
        formContainer.setCache(true);
        formContainer.setCacheHint(CacheHint.SPEED);
    }

    private LightRays findLightRays() {
        if (getScene() == null || !(getScene().getRoot() instanceof Parent)) {
            return null;
        }

        Parent root = (Parent) getScene().getRoot();

        // Try to find the LandingView or Visualizer component
        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof StackPane || node instanceof BorderPane) {
                // Check if this is the main container
                for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                    if (child instanceof Visualizer) {
                        return ((Visualizer) child).getLightRays();
                    }
                }
            }
        }

        return null;
    }

    private void handleComponentEvents() {
        signInButton.setOnAction(e -> {
            String username = userInput.getText();
            String pass = passInput.getText();
//            String text = authController.login(username, pass);
            LoginResult result = authController.login(username, pass);
            if(result.isSuccess()) {
                // show success toast later
                System.out.println(result.getMessage());

                AuthManager.setUser(result.getUser());
                if (onLoginSuccessListener != null) {
                    onLoginSuccessListener.accept(result.getUser());
                }
                hide();
            }else {
                // show error toast later
                System.out.println(result.getMessage());
            }
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
        // Don't show if already visible or animation is in progress
        if (isModalVisible || (slideIn != null && slideIn.getStatus() == javafx.animation.Animation.Status.RUNNING)
                || (slideOut != null && slideOut.getStatus() == javafx.animation.Animation.Status.RUNNING)) {
            return;
        }

        // Temporarily pause visualizer animations if possible
        LightRays rays = findLightRays();
        if (rays != null) {
            rays.stopAnimations();
        }

        // Set initial state for the modal
        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
        this.setTranslateX(-500); // Initial off-screen position
        super.setManaged(true);
        super.setVisible(true);
        this.toFront();

        // Configure and play the animation
        slideIn.setFromX(this.getTranslateX());
        slideIn.play();
    }

    public void hide() {
        // Don't hide if already hidden or animation is in progress
        if (!isModalVisible || (slideOut != null && slideOut.getStatus() == javafx.animation.Animation.Status.RUNNING)) {
            return;
        }

        // Mark as not visible conceptually at start of animation
        isModalVisible = false;

        // Temporarily pause visualizer animations if possible
        LightRays rays = findLightRays();
        if (rays != null) {
            rays.stopAnimations();
        }

        // Set cache for better performance during animation
        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);

        // Configure and play the animation
        slideOut.setFromX(this.getTranslateX()); // Should be 0
        slideOut.setToX(-500); // Target off-screen position
        slideOut.play();
    }


    public boolean isShowing() {
        return isModalVisible || (slideIn != null && slideIn.getStatus() == javafx.animation.Animation.Status.RUNNING);
    }

    // Optional: provide access to internal fields if MenuPage needs to clear them
    public void clearFields() {
        userInput.clear();
        passInput.clear();
    }
}
