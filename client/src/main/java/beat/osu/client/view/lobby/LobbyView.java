package beat.osu.client.view.lobby;

import beat.osu.client.helper.CssManager;
import beat.osu.client.view.shared.general.Page;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class LobbyView extends Page {

    private StackPane root;
    private BorderPane mainLayout;

    public LobbyView(Stage stage) {
        super(stage);
    }

    @Override
    public void init() {
        root = new StackPane();
        root.getStyleClass().add("root");

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-layout");

        root.getChildren().add(mainLayout);

        scene.setRoot(root);
        URL cssUrl = CssManager.getLobbyCssURL("LobbyView.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    @Override
    public void setLayout() {

    }
}
