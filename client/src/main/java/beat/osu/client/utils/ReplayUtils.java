package beat.osu.client.utils;

import beat.osu.client.events.game.ReplayEvent;
import beat.osu.client.helper.ResourceManager;

import java.io.*;
import java.util.ArrayList;

public class ReplayUtils {

    public static void saveReplay(ArrayList<ReplayEvent> replayEvents,
                                  String osrFileName) throws IOException {
        File replayDir = ResourceManager.getReplayDirectory();
        File osrFile = new File(replayDir, osrFileName);
        FileWriter fw = new FileWriter(osrFile);
        for (ReplayEvent event : replayEvents) {
            // save in file with this format
            String line = "ReplayEventOsu(time_delta=" + event.getTimeDelta() +
                    ", x=" + event.getX() + ", y=" + event.getY() +
                    ", keys=" + event.getKeyMask() + ")\n";
            fw.write(line);
        }
        fw.close();
    }

    public static ArrayList<ReplayEvent> loadReplay(String osrFileName) throws IOException {
        File replayDir = ResourceManager.getReplayDirectory();
        File osrFile = new File(replayDir, osrFileName);
        ArrayList<ReplayEvent> replayEvents = new ArrayList<>();

        if (!osrFile.exists()) {
            throw new IOException("Replay file not found: " + osrFileName);
        }

        BufferedReader br = new BufferedReader(new FileReader(osrFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            long timeDelta = Long.parseLong(parts[0].split("=")[1]);
            double x = Double.parseDouble(parts[1].split("=")[1]);
            double y = Double.parseDouble(parts[2].split("=")[1]);
            int keyMask = Integer.parseInt(parts[3].split("=")[1].replace(")", ""));

            ReplayEvent event = new ReplayEvent(timeDelta, x, y, keyMask);
            replayEvents.add(event);
        }

        return replayEvents;
    }
}
