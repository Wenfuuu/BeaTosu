package beat.osu.client.utils;

import beat.osu.client.events.game.ReplayEventData;
import beat.osu.client.helper.ResourceManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ReplayUtils {

    public static void saveReplay(ArrayList<ReplayEventData> replayEvents,
                                  String osrFileName) throws IOException {
        File replayDir = ResourceManager.getReplayDirectory();
        File osrFile = new File(replayDir, osrFileName);
        FileWriter fw = new FileWriter(osrFile);
        for (ReplayEventData event : replayEvents) {
            // save in file with this format
            String line = "ReplayEventOsu(time_delta=" + event.getTimeDelta() +
                    ", x=" + event.getX() + ", y=" + event.getY() +
                    ", keys=" + event.getKeyMask() + ")\n";
            fw.write(line);
        }
        fw.close();
    }
}
