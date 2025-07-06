package beat.osu.client.view.game.component.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HitCountsPanel extends VBox {

    private final HBox[] hitCountRows;
    private final ImageView[] hitCountLabels;
    private final List<List<ImageView>> hitCountDigits;
    private final ImageView[] hitCountXSymbols;

    // Combo display with images
    private final List<ImageView> comboDigits;
    private final ImageView comboXSymbol;
    private final HBox comboContainer;

    // Accuracy display with images
    private final ImageView[] accuracyDigits;
    private final ImageView percentSymbol;
    private final HBox accuracyContainer;
    private final ImageView scoreComma;

    private final Image[] digitImages;
    private final Image xImage;
    private final Image percentImage;
    private final Image commaImage;
    private final Image[] hitImages;

    public HitCountsPanel(Image[] digitImages) {
        this.digitImages = digitImages;
        
        xImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-x.png")).toExternalForm());

        percentImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-percent.png")).toExternalForm());
        commaImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-comma.png")).toExternalForm());

        hitImages = new Image[6];
        hitImages[0] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit300.png")).toExternalForm());
        hitImages[1] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit300g.png")).toExternalForm());
        hitImages[2] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit100.png")).toExternalForm());
        hitImages[3] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit100k.png")).toExternalForm());
        hitImages[4] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit50.png")).toExternalForm());
        hitImages[5] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit0.png")).toExternalForm());

        hitCountRows = new HBox[3];
        hitCountLabels = new ImageView[6];
        hitCountDigits = new ArrayList<>();
        hitCountXSymbols = new ImageView[6];

        // Initialize combo display
        comboDigits = new ArrayList<>();
        comboXSymbol = new ImageView(xImage);
        comboContainer = new HBox(2);

        // Initialize accuracy display (4 digits + percent)
        accuracyDigits = new ImageView[4];
        percentSymbol = new ImageView(percentImage);
        accuracyContainer = new HBox(1);
        scoreComma = new ImageView(commaImage);

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        for (int i = 0; i < 6; i++) {
            hitCountLabels[i] = new ImageView(hitImages[i]);
            hitCountLabels[i].setFitWidth(60);
            hitCountLabels[i].setFitHeight(60);

            if (i == 1 || i == 3) {
                hitCountLabels[i].setScaleX(0.8);
                hitCountLabels[i].setScaleY(0.8);
                hitCountLabels[i].setScaleZ(0.8);
            }

            hitCountXSymbols[i] = new ImageView(xImage);
            hitCountXSymbols[i].setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.06);
            hitCountXSymbols[i].setPreserveRatio(true);

            hitCountDigits.add(new ArrayList<>());
        }

        for (int i = 0; i < 3; i++) {
            hitCountRows[i] = new HBox(20);
            hitCountRows[i].setPadding(new Insets(0, 0, 0, ScreenManager.SCREEN_WIDTH * 0.03));
            hitCountRows[i].setAlignment(Pos.CENTER_LEFT);
        }

        ImageView initialComboDigit = new ImageView(digitImages[0]);
        initialComboDigit.setFitWidth(25);
        initialComboDigit.setFitHeight(35);
        initialComboDigit.setPreserveRatio(true);
        comboDigits.add(initialComboDigit);

        comboXSymbol.setFitWidth(25);
        comboXSymbol.setFitHeight(35);
        comboXSymbol.setPreserveRatio(true);

        comboContainer.getChildren().addAll(initialComboDigit, comboXSymbol);

        // Initialize accuracy display (4 digits + percent)
        scoreComma.setFitWidth(10);
        scoreComma.setFitHeight(30);

        for (int i = 0; i < 4; i++) {
            accuracyDigits[i] = new ImageView(digitImages[0]);
            accuracyDigits[i].setFitWidth(20);
            accuracyDigits[i].setFitHeight(28);
            accuracyDigits[i].setPreserveRatio(true);
            accuracyContainer.getChildren().add(accuracyDigits[i]);

            if (i == 1) {
                accuracyContainer.getChildren().add(scoreComma);
            }
        }

        percentSymbol.setFitWidth(20);
        percentSymbol.setFitHeight(28);
        percentSymbol.setPreserveRatio(true);
        accuracyContainer.getChildren().add(percentSymbol);
    }

    private void setupLayout() {
        this.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.45);
        this.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.45);
        this.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.45);

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.52);
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.52);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.52);

        this.setPadding(new Insets(ScreenManager.SCREEN_HEIGHT * 0.037, 0, 0, 0));

        this.setSpacing(ScreenManager.SCREEN_HEIGHT * 0.06);
        this.getStyleClass().add("hit-counts-panel");
        
        // Create combo and accuracy container
        HBox comboAccuracyBox = new HBox(ScreenManager.SCREEN_WIDTH * 0.175);
        comboAccuracyBox.setAlignment(Pos.CENTER_LEFT);
        comboAccuracyBox.getChildren().addAll(
                new VBox(10, comboContainer),
                new VBox(10, accuracyContainer)
        );
        
        // Add hit count rows and combo/accuracy display
        this.getChildren().addAll(hitCountRows);
        this.getChildren().add(comboAccuracyBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getGameCssURL("HitCountsPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("HitCountsPanel CSS file not found!");
        }
    }

    public void updateCombo(int combo) {
        String comboStr = String.valueOf(combo);
        int requiredDigits = comboStr.length();

        // Clear the container
        comboContainer.getChildren().clear();

        // Adjust the number of digit ImageViews
        while (comboDigits.size() < requiredDigits) {
            ImageView newDigit = new ImageView(digitImages[0]);
            newDigit.setFitWidth(25);
            newDigit.setFitHeight(35);
            newDigit.setPreserveRatio(true);
            comboDigits.add(0, newDigit);
        }

        while (comboDigits.size() > requiredDigits) {
            comboDigits.remove(0);
        }

        for (int i = 0; i < requiredDigits; i++) {
            int digit = Character.getNumericValue(comboStr.charAt(i));
            comboDigits.get(i).setImage(digitImages[digit]);
        }

        comboContainer.getChildren().addAll(comboDigits);
        comboContainer.getChildren().add(comboXSymbol);
    }

    public void updateAccuracy(double accuracy) {
        // Format accuracy to 2 decimal places (e.g., 96.24% -> "9624")
        int accuracyInt = (int) Math.round(accuracy * 100);
        String accuracyStr = String.format("%04d", Math.min(accuracyInt, 10000));

        for (int i = 0; i < 4; i++) {
            int digit = Character.getNumericValue(accuracyStr.charAt(i));
            accuracyDigits[i].setImage(digitImages[digit]);
        }
    }

    public void updateHitCounts(int perfectHits, int gekiHits, int greatHits,
                                int katuHits, int goodHits, int misses) {
        int[] hitCounts = {perfectHits, gekiHits, greatHits, katuHits, goodHits, misses};

        for (int i = 0; i < 6; i++) {
            updateHitCountDigits(i, hitCounts[i]);
        }

        for (int i = 0; i < 3; i++) {
            hitCountRows[i].getChildren().clear();
        }

        hitCountRows[0].getChildren().addAll(
                createHitCountDisplay(0),
                createHitCountDisplay(1)
        );

        hitCountRows[1].getChildren().addAll(
                createHitCountDisplay(2),
                createHitCountDisplay(3)
        );

        hitCountRows[2].getChildren().addAll(
                createHitCountDisplay(4),
                createHitCountDisplay(5)
        );
    }

    private void updateHitCountDigits(int hitType, int count) {
        String countStr = String.valueOf(count);
        int requiredDigits = countStr.length();

        List<ImageView> digits = hitCountDigits.get(hitType);

        while (digits.size() < requiredDigits) {
            ImageView newDigit = new ImageView(digitImages[0]);
            newDigit.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.06);
            newDigit.setPreserveRatio(true);
            digits.add(0, newDigit);
        }

        while (digits.size() > requiredDigits) {
            digits.remove(0);
        }

        for (int i = 0; i < requiredDigits; i++) {
            int digit = Character.getNumericValue(countStr.charAt(i));
            digits.get(i).setImage(digitImages[digit]);
        }
    }

    private HBox createHitCountDisplay(int hitType) {
        HBox display = new HBox(0);
        display.setAlignment(Pos.CENTER_LEFT);

        HBox digitsContainer = new HBox(3);
        digitsContainer.setAlignment(Pos.CENTER_LEFT);
        digitsContainer.getChildren().addAll(hitCountDigits.get(hitType));

        HBox.setMargin(digitsContainer, new Insets(0, ScreenManager.SCREEN_WIDTH * 0.001, 0, ScreenManager.SCREEN_WIDTH * 0.036));

        display.getChildren().add(hitCountLabels[hitType]);
        display.getChildren().add(digitsContainer);
        display.getChildren().add(hitCountXSymbols[hitType]);

        display.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.225);
        display.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.225);
        display.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.225);

        return display;
    }
}
