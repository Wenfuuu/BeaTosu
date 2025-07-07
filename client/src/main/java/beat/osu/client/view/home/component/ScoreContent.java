package beat.osu.client.view.home.component;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.shared.dto.score.ScoreDto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

public class ScoreContent extends ScrollPane {

    private final VBox scoreListBox;
    private final StackPane contentContainer;
    private final ImageView noRecordsImageView;
    @Setter
    private Consumer<ScoreDto> onScoreSelectedCallback;

    public ScoreContent(ArrayList<ScoreDto> scores) {
        this.scoreListBox = new VBox();
        this.contentContainer = new StackPane();
        
        Image noRecordsImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/misc/no-records-img.png")).toExternalForm());
        this.noRecordsImageView = new ImageView(noRecordsImage);
        this.noRecordsImageView.setPreserveRatio(true);
        this.noRecordsImageView.setSmooth(true);
        this.noRecordsImageView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.25);

        StackPane.setMargin(noRecordsImageView, new Insets(ScreenManager.SCREEN_HEIGHT * 0.26, 0, 0, 0));

        this.getStyleClass().add("score-content");
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        initializeComponents();
        setupLayout();
        loadStyles();

        populateScores(scores);
        handleEvent();
    }

    private void initializeComponents() {
        scoreListBox.getStyleClass().add("score-list");
        contentContainer.getChildren().add(scoreListBox);
        contentContainer.setAlignment(Pos.CENTER);
    }

    private void setupLayout() {
        this.setContent(contentContainer);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("ScoreContent.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public void populateScores(ArrayList<ScoreDto> scores) {
        scoreListBox.getChildren().clear();
        contentContainer.getChildren().clear();
        
        if (scores.isEmpty()) {
            contentContainer.getChildren().add(noRecordsImageView);
        } else {
            contentContainer.getChildren().add(scoreListBox);
            for (ScoreDto score : scores) {
                ScoreItem scoreItem = new ScoreItem(score);
                scoreListBox.getChildren().add(scoreItem);
            }
        }
    }

    private void handleEvent() {
        scoreListBox.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double width = getContent().getBoundsInLocal().getWidth();
            double vvalue = getVvalue();

            setVvalue(vvalue - deltaY / width);

            event.consume();
        });

        scoreListBox.setOnMouseClicked(e -> {
            Node clickedNode = e.getPickResult().getIntersectedNode();

            while (clickedNode != null && clickedNode.getParent() != scoreListBox) {
                clickedNode = clickedNode.getParent();
            }

            if (clickedNode instanceof ScoreItem) {
                ScoreItem scoreItem = (ScoreItem) clickedNode;
                if (onScoreSelectedCallback != null) {
                    onScoreSelectedCallback.accept(scoreItem.getScore());
                }
            }
        });
    }
}
