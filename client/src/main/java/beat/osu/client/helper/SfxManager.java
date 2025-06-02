package beat.osu.client.helper;

import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class SfxManager {
    private static final String SFX_DIR = "./src/main/resources/assets/sfx/";
    private static final String TEMP_DIR = "./src/main/resources/assets/temp/";

    public static MediaPlayer createSfxPlayer(String sfxName) {
        String sfxPath = SFX_DIR + sfxName;
        File sfxFile = new File(sfxPath);
        if (!sfxFile.exists()) {
            sfxFile = new File(TEMP_DIR + sfxName);
            if (!sfxFile.exists()) {
                System.err.println("SFX file not found: " + sfxName);
                return null;
            }
        }

        Media media = new Media(sfxFile.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);

        player.setOnReady(() -> {
            player.setOnEndOfMedia(player::play);
        });

        player.setVolume(0.2);
        return player;
    }

    public static void playSfx(String sfxName) {
        String sfxPath = SFX_DIR + sfxName;
        File sfxFile = new File(sfxPath);

        if (!sfxFile.exists()) {
            Beatmap beatmap = OsuParser.getCurrentBeatmap();
            sfxFile = new File(TEMP_DIR + beatmap.getBeatmapSetId() + "/" + sfxName);
            if (!sfxFile.exists()) {
                System.err.println("SFX file not found in both SFX and TEMP directories: " + sfxName);
                return;
            }
        }

        Media media = new Media(sfxFile.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setVolume(0.2);
        player.setOnEndOfMedia(player::dispose);
        player.play();
    }
}
