package beat.osu.client;

import beat.osu.client.helper.StageManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.service.ClientService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage)  {
        ClientService clientService = ClientService.getInstance();
        if (!clientService.connect()) {
            System.out.println("Failed to connect to server");
        }

        StageManager.setStage(stage);

        StageManager.getStage().setOnCloseRequest(e -> {
            if (clientService.isConnected()) {
                clientService.disconnect();
            }
            Platform.exit();
        });

        ViewManager.getInstance().showLandingView();
//        ViewManager.getInstance().showUploadPage();
    }

    public static void main(String[] args) {
        launch();
    }
}