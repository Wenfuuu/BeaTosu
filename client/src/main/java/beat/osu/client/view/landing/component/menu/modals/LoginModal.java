package beat.osu.client.view.landing.component.menu.modals;

import java.net.URL;
import java.util.function.Consumer;

import beat.osu.client.controller.AuthController;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.menu.layout.TopBar;
import beat.osu.client.view.landing.component.menu.ui.LightRays;
import beat.osu.client.view.landing.component.menu.ui.Visualizer;
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

    private TopBar topBar;

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
            System.err.println("LoginModal.css file not found!");
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
        this.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.4);

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

        backButton = createBackButton();
        StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backButton, new Insets(0, 0, 12, 0));

        formContainer.getChildren().addAll(
                titleLabel,
                userBox,
                passBox,
                signInButton,
                createAccountButton
        );

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
                                // Update the TopBar with the logged-in user information
                                topBar.updateUserInfo(result.getValue().getUser());

                                if (onLoginSuccessListener != null) {
                                    onLoginSuccessListener.accept(result.getValue().getUser());
                                }
                                hide();
                            } else {
                                System.err.println(result.getError().getMessage());
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
        this.toFront();

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

    private Button createBackButton() {
        Button button = new Button();
        try {
            String normalImagePath = "/assets/buttons/shared/global_back.png";
            String hoveredImagePath = "/assets/buttons/shared/global_back_hovered.png";
            
            URL normalImageUrl = getClass().getResource(normalImagePath);
            URL hoveredImageUrl = getClass().getResource(hoveredImagePath);
            
            if (normalImageUrl == null) {
                System.err.println("Image not found: " + normalImagePath);
                button.setText("〈 back");
            } else {
                Image normalImage = new Image(normalImageUrl.toExternalForm());
                
                ImageView normalImageView = new ImageView(normalImage);
                normalImageView.setFitHeight(50);
                normalImageView.setPreserveRatio(true);
                
                button.setGraphic(normalImageView);
                button.getStyleClass().clear();
                button.getStyleClass().add("back-button-image");
                button.setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-radius: 0; -fx-effect: null; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

                button.setMinSize(normalImageView.getFitWidth(), normalImageView.getFitHeight());
                button.setMaxSize(normalImageView.getFitWidth(), normalImageView.getFitHeight());
                button.setPrefSize(normalImageView.getFitWidth(), normalImageView.getFitHeight());
                
                if (hoveredImageUrl != null) {
                    Image hoveredImage = new Image(hoveredImageUrl.toExternalForm());
                    ImageView hoveredImageView = new ImageView(hoveredImage);
                    hoveredImageView.setFitHeight(50);
                    hoveredImageView.setPreserveRatio(true);
                    
                    hoveredImageView.setVisible(false);
                    
                    StackPane imageStack = new StackPane();
                    imageStack.setAlignment(Pos.CENTER_LEFT);
                    imageStack.getChildren().addAll(normalImageView, hoveredImageView);
                    
                    button.setGraphic(imageStack);
                    
                    double normalWidth = normalImageView.getBoundsInLocal().getWidth();
                    double normalHeight = normalImageView.getBoundsInLocal().getHeight();
                    double hoveredWidth = hoveredImageView.getBoundsInLocal().getWidth();
                    double hoveredHeight = hoveredImageView.getBoundsInLocal().getHeight();
                    
                    double maxWidth = Math.max(normalWidth, hoveredWidth);
                    double maxHeight = Math.max(normalHeight, hoveredHeight);
                    
                    button.setMinSize(maxWidth, maxHeight);
                    button.setMaxSize(maxWidth, maxHeight);
                    button.setPrefSize(maxWidth, maxHeight);
                    
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(80), imageStack);
                    scaleUp.setToX(1.05);
                    scaleUp.setToY(1.05);
                    scaleUp.setInterpolator(Interpolator.EASE_OUT);
                    
                    ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), imageStack);
                    scaleDown.setToX(1.0);
                    scaleDown.setToY(1.0);
                    scaleDown.setInterpolator(Interpolator.EASE_OUT);
                    
                    button.setOnMouseEntered(e -> {
                        scaleDown.stop();
                        normalImageView.setVisible(false);
                        hoveredImageView.setVisible(true);
                        scaleUp.play();
                    });
                    
                    button.setOnMouseExited(e -> {
                        scaleUp.stop();
                        normalImageView.setVisible(true);
                        hoveredImageView.setVisible(false);
                        scaleDown.play();
                    });
                } else {
                    System.err.println("Hovered image not found: " + hoveredImagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading back button images: " + e.getMessage());
            button.setText("〈 back");
            button.getStyleClass().add("back-button");
        }
        return button;
    }
}