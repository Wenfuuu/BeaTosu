package beat.osu.client.view.shared.bancho.buttons;

import java.net.URL;
import java.util.function.Consumer;

import beat.osu.client.helper.CssManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.Getter;

public class ChatTabButton extends Button {
    private final Label textLabel;
    private final Button closeButton;
    private final HBox container;

    @Getter
    private boolean selected = false;
    private Consumer<ChatTabButton> onCloseAction;

    public ChatTabButton(String text) {
        super();

        container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setSpacing(0);

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        textLabel = new Label(text);
        textLabel.getStyleClass().add("chat-tab-text");
        textLabel.setAlignment(Pos.CENTER);
        HBox.setMargin(textLabel, new Insets(0, 20, 0, 20));

        Label crossLabel = new Label("×");
        crossLabel.getStyleClass().add("cross-icon");

        StackPane graphicWrapper = new StackPane(crossLabel);
        graphicWrapper.setPrefSize(20, 20);
        graphicWrapper.setMaxSize(20, 20);
        graphicWrapper.setMinSize(20, 20);
        graphicWrapper.setAlignment(Pos.CENTER);

        closeButton = new Button();
        closeButton.setGraphic(graphicWrapper);
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnMouseClicked(e -> {
            e.consume();
            if (onCloseAction != null) {
                onCloseAction.accept(this);
            }
        });
        
        this.setOnMouseEntered(e -> closeButton.getStyleClass().add("visible"));
        this.setOnMouseExited(e -> closeButton.getStyleClass().remove("visible"));

        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        leftSpacer.minWidthProperty().bind(rightSpacer.widthProperty().add(20));
        leftSpacer.prefWidthProperty().bind(rightSpacer.widthProperty().add(20));
        leftSpacer.maxWidthProperty().bind(rightSpacer.widthProperty().add(20));

        container.getChildren().addAll(leftSpacer, textLabel, rightSpacer, closeButton);
        container.setPadding(new Insets(0, 4, 0, 4));

        this.setGraphic(container);
        this.setText("");

        URL cssUrl = CssManager.getSharedCssURL("ChatTabButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            this.getStyleClass().add("selected");
        } else {
            this.getStyleClass().remove("selected");
        }
    }

    public void setOnCloseAction(Consumer<ChatTabButton> onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setTabText(String text) {
        textLabel.setText(text);
    }

    public String getTabText() {
        return textLabel.getText();
    }
}