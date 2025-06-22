package beat.osu.client.view.shared.bancho.buttons;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import lombok.Getter;

import java.net.URL;

public class SortUsersButton extends Button {

    @Getter
    private boolean selected = false;

    public SortUsersButton(String text) {
        super();
        this.setText(text);

        URL cssUrl = CssManager.getSharedCssURL("SortUsersButton.css");
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
}
