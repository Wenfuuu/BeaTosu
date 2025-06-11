package beat.osu.client.view.shared.bancho.buttons;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AddChatButton extends Button {
    private final Button addButton;

    public AddChatButton() {
        super();
        Label plusLabel = new Label("+");
        plusLabel.getStyleClass().add("plus-icon");

        StackPane graphicWrapper = new StackPane(plusLabel);
        graphicWrapper.setPrefSize(20, 20);
        graphicWrapper.setMaxSize(20, 20);
        graphicWrapper.setMinSize(20, 20);
        graphicWrapper.setAlignment(Pos.CENTER);

        addButton = new Button();
        addButton.setGraphic(graphicWrapper);
        addButton.getStyleClass().add("add-button");

        this.setGraphic(addButton);

        URL cssUrl = CssManager.getLandingCssURL("AddChatButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}
