package beat.osu.client.helper;

import javafx.application.HostServices;

public class UrlManager {
    private static HostServices hostServices;

    public static void setHostServices(HostServices hostServices) {
        UrlManager.hostServices = hostServices;
    }

    public static void openURL(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
        } else {
            System.err.println("HostServices not initialized");
        }
    }
}
