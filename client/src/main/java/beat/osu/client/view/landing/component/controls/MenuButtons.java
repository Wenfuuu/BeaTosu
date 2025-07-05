package beat.osu.client.view.landing.component.controls;

import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;

public class MenuButtons extends VBox {

    private Button menuPlayButton;
    private Button menuOptionButton;
    private Button menuExitButton;

    public MenuButtons() {
        super(25);
        this.getStyleClass().add("menu-box");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setMaxWidth(Region.USE_PREF_SIZE);

        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        menuPlayButton = createMenuButton("play.png");
        menuOptionButton = createMenuButton("options.png");
        menuExitButton = createMenuButton("exit.png");

        menuPlayButton.setAlignment(Pos.CENTER);
        menuOptionButton.setAlignment(Pos.CENTER);
        menuExitButton.setAlignment(Pos.CENTER);
    }

    private void setupLayout() {
        this.getChildren().addAll(menuPlayButton, menuOptionButton, menuExitButton);
    }

    private Button createMenuButton(String imageName) {
        Button button = new Button();
        try {
            String imagePath = "/assets/buttons/main-menu/" + imageName;
            String hoveredImageName = imageName.substring(0, imageName.lastIndexOf('.')) + "_hovered.png";
            String hoveredImagePath = "/assets/buttons/main-menu/" + hoveredImageName;
            
            URL imageUrl = getClass().getResource(imagePath);
            URL hoveredImageUrl = getClass().getResource(hoveredImagePath);
            
            if (imageUrl == null) {
                System.err.println("Image not found: " + imagePath);
                button.setText(imageName.substring(0, imageName.lastIndexOf('.')));
            } else {
                Image normalImage = new Image(imageUrl.toExternalForm());
                Image hoveredImage = null;
                
                ImageView normalImageView = new ImageView(normalImage);
                normalImageView.setFitHeight(ScreenManager.SCREEN_HEIGHT / 11);
                normalImageView.setPreserveRatio(true);
                
                ImageView hoveredImageView = new ImageView();
                hoveredImageView.setFitHeight(ScreenManager.SCREEN_HEIGHT / 11);
                hoveredImageView.setPreserveRatio(true);
                hoveredImageView.setOpacity(0);
                
                StackPane imageStack = new StackPane();
                imageStack.getChildren().addAll(normalImageView, hoveredImageView);
                
                button.setGraphic(imageStack);
                button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
                
                if (hoveredImageUrl != null) {
                    hoveredImage = new Image(hoveredImageUrl.toExternalForm());
                    hoveredImageView.setImage(hoveredImage);
                    
                    FadeTransition fadeOutNormal = new FadeTransition(Duration.millis(50), normalImageView);
                    fadeOutNormal.setFromValue(1.0);
                    fadeOutNormal.setToValue(0.0);
                    
                    FadeTransition fadeInHovered = new FadeTransition(Duration.millis(200), hoveredImageView);
                    fadeInHovered.setFromValue(0.0);
                    fadeInHovered.setToValue(1.0);
                    
                    FadeTransition fadeOutHovered = new FadeTransition(Duration.millis(200), hoveredImageView);
                    fadeOutHovered.setFromValue(1.0);
                    fadeOutHovered.setToValue(0.0);
                    
                    FadeTransition fadeInNormal = new FadeTransition(Duration.millis(50), normalImageView);
                    fadeInNormal.setFromValue(0.0);
                    fadeInNormal.setToValue(1.0);
                    
                    TranslateTransition bounceRight = new TranslateTransition(Duration.millis(200), button);
                    bounceRight.setFromX(0);
                    bounceRight.setToX(50);

                    TranslateTransition bounceBack = new TranslateTransition(Duration.millis(200), button);
                    bounceBack.setToX(0);

                    ParallelTransition hoverInTransition = new ParallelTransition(
                        new ParallelTransition(fadeOutNormal, fadeInHovered),
                        bounceRight
                    );
                    
                    ParallelTransition hoverOutTransition = new ParallelTransition(
                        new ParallelTransition(fadeOutHovered, fadeInNormal),
                        bounceBack
                    );
                    
                    button.setOnMouseEntered(e -> {
                        SfxManager.playMenuSfx(SfxType.MENU_HOVER);
                        hoverInTransition.stop();
                        hoverOutTransition.stop();
                        button.setTranslateX(0);
                        hoverInTransition.play();
                    });
                    
                    button.setOnMouseExited(e -> {
                        hoverInTransition.stop();
                        hoverOutTransition.stop();
                        bounceBack.setFromX(button.getTranslateX());
                        hoverOutTransition.play();
                    });
                } else {
                    System.out.println("Hovered image not found: " + hoveredImagePath + " (using normal image only)");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + imageName + ": " + e.getMessage());
            button.setText(imageName.substring(0, imageName.lastIndexOf('.')));
        }
//        button.getStyleClass().add("menu-button");
        return button;
    }

    public Button getPlayButton() {
        return menuPlayButton;
    }

    public Button getOptionButton() {
        return menuOptionButton;
    }

    public Button getExitButton() {
        return menuExitButton;
    }
}
