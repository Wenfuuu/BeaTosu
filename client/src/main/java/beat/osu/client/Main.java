package beat.osu.client;

import beat.osu.client.helper.StageManager;
import beat.osu.client.service.ClientService;
import beat.osu.client.view.UploadPage;
import beat.osu.client.view.landing.LandingView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage)  {
        ClientService clientService = new ClientService();
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

        new LandingView(StageManager.getStage());
//        new HomeView(StageManager.getStage());
//        new UploadPage(StageManager.getStage());
    }

    public static void main(String[] args) {
        launch();
    }
}