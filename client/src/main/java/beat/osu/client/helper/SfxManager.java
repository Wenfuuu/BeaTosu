package beat.osu.client.helper;

import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class SfxManager {
    private static final String SFX_DIR = "./src/main/resources/assets/sfx/";

    public static MediaPlayer createSfxPlayer(String sfxName) {
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        String sfxPath = SFX_DIR + sfxName;
        File sfxFile = new File(sfxPath);
        if (!sfxFile.exists()) {
            File beatmapDir = new File(ResourceManager.getTempDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
            sfxFile = new File(beatmapDir, sfxName);
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
            File beatmapDir = new File(ResourceManager.getTempDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
            sfxFile = new File(beatmapDir, sfxName);
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
