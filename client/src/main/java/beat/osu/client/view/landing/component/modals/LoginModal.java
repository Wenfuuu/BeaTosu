package beat.osu.client.view.landing.component.modals;

import java.net.URL;
import java.util.function.Consumer;

import beat.osu.client.controller.AuthController;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.layout.TopBar;
import beat.osu.client.view.landing.component.ui.LightRays;
import beat.osu.client.view.landing.component.ui.Visualizer;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.dto.auth.responses.LoginResponse;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class LoginModal extends StackPane {

    private final AuthController authController;

    private VBox formContainer;
    private TranslateTransition slideIn;
    private TranslateTransition slideOut;
    private boolean isModalVisible = false;

    private TextField userInput;
    private PasswordField passInput;
    private Button signInButton;
    private Button createAccountButton;
    private Button backButton;
    private Label titleLabel;

    private final TopBar topBar;

    @Setter
    private Consumer<UserDto> onLoginSuccessListener;
    @Setter
    private Runnable onCreateAccountListener;

    public LoginModal(TopBar topBar) {
        this.topBar = topBar;
        this.authController = new AuthController();

        initialize();
        setupAnimations();
        handleComponentEvents();

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Global css file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("LoginModal.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("LoginModal.css file not found!");
        }

        this.setVisible(false);
        this.setManaged(false);
    }

    private void initialize() {
        this.getStyleClass().add("login-modal-background");
        this.setMaxWidth(650);
        formContainer = new VBox(20);
        formContainer.getStyleClass().add("login-form-container");
        formContainer.setMaxWidth(200);
        formContainer.setMaxHeight(Region.USE_PREF_SIZE);
        formContainer.setAlignment(Pos.TOP_LEFT);

        titleLabel = new Label("SIGN IN");
        titleLabel.getStyleClass().add("login-title");

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("login-label");
        userInput = new TextField();
        userInput.getStyleClass().add("login-input");
        VBox userBox = new VBox(5, userLabel, userInput);

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("login-label");
        passInput = new PasswordField();
        passInput.getStyleClass().add("login-input");
        VBox passBox = new VBox(5, passLabel, passInput);

        signInButton = new Button("Sign In");
        signInButton.getStyleClass().addAll("login-button");
        signInButton.setMaxWidth(Double.MAX_VALUE);

        VBox.setMargin(signInButton, new Insets(8, 0, 8, 0));

        createAccountButton = new Button("Create an account");
        createAccountButton.getStyleClass().addAll("login-button");
        createAccountButton.setMaxWidth(Double.MAX_VALUE);

        backButton = ButtonFactory.createBackButton();
        StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backButton, new Insets(0, 0, 12, 0));

        formContainer.getChildren().addAll(
                titleLabel,
                userBox,
                passBox,
                signInButton,
                createAccountButton);

        this.getChildren().addAll(formContainer, backButton);
        StackPane.setAlignment(formContainer, Pos.CENTER_RIGHT);
    }

    private void setupAnimations() {
        slideIn = new TranslateTransition(Duration.millis(150), this);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        slideIn.setOnFinished(e -> {
            isModalVisible = true;
            formContainer.setCacheHint(CacheHint.DEFAULT);

            LightRays rays = findLightRays();
            if (rays != null) {
                rays.startUnifiedAnimation();
            }
        });

        slideOut = new TranslateTransition(Duration.millis(150), this);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        slideOut.setOnFinished(event -> {
            super.setVisible(false);
            super.setManaged(false);
            isModalVisible = false;

            LightRays rays = findLightRays();
            if (rays != null) {
                rays.startUnifiedAnimation();
            }
        });

        formContainer.setCache(true);
        formContainer.setCacheHint(CacheHint.SPEED);
    }

    private LightRays findLightRays() {
        if (getScene() == null || getScene().getRoot() == null) {
            return null;
        }

        Parent root = getScene().getRoot();

        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof StackPane || node instanceof BorderPane) {
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
            String password = passInput.getText();

            authController.login(username, password)
                    .thenAcceptAsync(result -> {
                        Platform.runLater(() -> {
                            if (result.isSuccess()) {
                                LoginResponse response = result.getValue();
                                System.out.println(response.getMessage());

                                AuthManager.setUser(result.getValue().getUser());
                                topBar.updateUserInfo(result.getValue().getUser());

                                if (onLoginSuccessListener != null) {
                                    onLoginSuccessListener.accept(result.getValue().getUser());
                                }

                                Toast.success(response.getMessage()).show();
                                hide();
                            } else {
                                Toast.error(result.getError().getMessage()).show();
                            }
                        });
                    });
        });

        createAccountButton.setOnAction(e -> {
            if (onCreateAccountListener != null) {
                onCreateAccountListener.run();
            }
            hide();
        });

        backButton.setOnAction(e -> hide());
    }

    public void show() {
        if (isModalVisible || (slideIn != null && slideIn.getStatus() == Animation.Status.RUNNING)
                || (slideOut != null && slideOut.getStatus() == Animation.Status.RUNNING)) {
            return;
        }

        LightRays rays = findLightRays();
        if (rays != null) {
            rays.stopAnimations();
        }

        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
        this.setTranslateX(-500);
        super.setManaged(true);
        super.setVisible(true);
        // this.toFront();

        slideIn.setFromX(this.getTranslateX());
        slideIn.play();
    }

    public void hide() {
        if (!isModalVisible || (slideOut != null && slideOut.getStatus() == Animation.Status.RUNNING)) {
            return;
        }

        isModalVisible = false;

        LightRays rays = findLightRays();
        if (rays != null) {
            rays.stopAnimations();
        }

        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);

        slideOut.setFromX(this.getTranslateX());
        slideOut.setToX(-500);
        slideOut.play();
    }

    public boolean isShowing() {
        return isModalVisible || (slideIn != null && slideIn.getStatus() == Animation.Status.RUNNING);
    }

    public void clearFields() {
        userInput.clear();
        passInput.clear();
    }
}