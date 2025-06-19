package beat.osu.client.utils;

import beat.osu.client.events.game.ReplayEventData;
import beat.osu.client.helper.ResourceManager;

import java.io.*;
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

    public static ArrayList<ReplayEventData> loadReplay(String osrFileName) throws IOException {
        File replayDir = ResourceManager.getReplayDirectory();
        File osrFile = new File(replayDir, osrFileName);
        ArrayList<ReplayEventData> replayEvents = new ArrayList<>();

        if (!osrFile.exists()) {
            throw new IOException("Replay file not found: " + osrFileName);
        }

        BufferedReader br = new BufferedReader(new FileReader(osrFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            long timeDelta = Long.parseLong(parts[0].split("=")[1]);
            int x = Integer.parseInt(parts[1].split("=")[1]);
            int y = Integer.parseInt(parts[2].split("=")[1]);
            int keyMask = Integer.parseInt(parts[3].split("=")[1].replace(")", ""));

            ReplayEventData event = new ReplayEventData(timeDelta, x, y, keyMask);
            replayEvents.add(event);
        }

        return replayEvents;
    }
}
