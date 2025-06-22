package beat.osu.client.view.home.component;

import beat.osu.client.helper.CssManager;
import beat.osu.shared.dto.score.ScoreDto;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ScoreContent extends ScrollPane {

    private final VBox scoreListBox;
    @Setter
    private Consumer<ScoreDto> onScoreSelectedCallback;

    public ScoreContent(ArrayList<ScoreDto> scores) {
        this.scoreListBox = new VBox();
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
    }

    private void setupLayout() {
        this.setContent(scoreListBox);
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
        for (ScoreDto score : scores) {
            ScoreItem scoreItem = new ScoreItem(score);
            scoreListBox.getChildren().add(scoreItem);
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
