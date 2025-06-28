package beat.osu.client.view.game.component;

import beat.osu.client.helper.CssManager;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;

public class MatchScoreContent extends ScrollPane {

    private final VBox scoreListBox;
    @Getter
    private SequentialTransition hideTransition;

    public MatchScoreContent(ArrayList<MatchScoreEvent> matchScores) {
        this.scoreListBox = new VBox();
        this.getStyleClass().add("match-score-content");
        this.setFitToWidth(false);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        initializeComponents();
        setupLayout();
        setupAnimations();
        loadStyles();

        populateScores(matchScores);
        handleEvent();
    }

    private void setupAnimations() {
        FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(500), this);
        fadeOutTransition.setFromValue(1);
        fadeOutTransition.setToValue(0);

        hideTransition = new SequentialTransition(
                fadeOutTransition,
                new PauseTransition(Duration.millis(500)));
    }

    private void initializeComponents() {
        scoreListBox.getStyleClass().add("match-score-list");
    }

    private void setupLayout() {
        this.setContent(scoreListBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getGameCssURL("MatchScoreContent.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public void populateScores(ArrayList<MatchScoreEvent> matchScores) {
        System.out.println("Populating match scores: " + matchScores.size());
        scoreListBox.getChildren().clear();
        for (MatchScoreEvent score : matchScores) {
            MatchScoreItem scoreItem = new MatchScoreItem(score);
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
    }
}
