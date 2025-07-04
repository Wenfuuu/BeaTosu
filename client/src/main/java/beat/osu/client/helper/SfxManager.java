package beat.osu.client.helper;

import beat.osu.client.Main;
import beat.osu.client.config.ConfigurationManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;

import java.io.File;
import java.net.URL;

public class SfxManager {
    @Getter
    private static double SFX_VOLUME;
    private static boolean ignoreBeatmapSFX;

    public static void setSFX_VOLUME(double SFX_VOLUME) {
        SfxManager.SFX_VOLUME = SFX_VOLUME;
        ConfigurationManager.getInstance().setSfxVolume(SFX_VOLUME);
    }

    public static void setIgnoreBeatmapSFX(boolean ignore) {
        SfxManager.ignoreBeatmapSFX = ignore;
        ConfigurationManager.getInstance().setIgnoreBeatmapHitsounds(ignore);
    }

    public static boolean isIgnoreBeatmapSFX() {
        return ignoreBeatmapSFX;
    }

    static {
        SFX_VOLUME = ConfigurationManager.getInstance().getSfxVolume();
        ignoreBeatmapSFX = ConfigurationManager.getInstance().getIgnoreBeatmapHitsounds();
    }

    // private static final String SFX_DIR = "./src/main/resources/assets/sfx/";
    private static URL getSfxResource(String sfxName) {
        return Main.class.getResource("/assets/sfx/" + sfxName);
    }

    public static MediaPlayer createSfxPlayer(String sfxName) {
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        URL sfxUrl = getSfxResource(sfxName);
        Media media = null;

        if (sfxUrl != null) {
            media = new Media(sfxUrl.toString());
        } else {
            File beatmapDir = new File(ResourceManager.getTempDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
            File sfxFile = new File(beatmapDir, sfxName);
            if (!sfxFile.exists()) {
                System.err.println("SFX file not found: " + sfxName);
                return null;
            }
            media = new Media(sfxFile.toURI().toString());
        }

        MediaPlayer player = new MediaPlayer(media);
        player.setOnReady(() -> {
            player.setOnEndOfMedia(player::play);
        });

        player.setVolume(SFX_VOLUME);
        return player;
    }

    public static void playSfx(String sfxName) {
//        System.out.println("Playing SFX: " + sfxName);
        URL sfxUrl = getSfxResource(sfxName);
        Media media = null;

        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        File beatmapDir = new File(ResourceManager.getTempDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
        File sfxFile = new File(beatmapDir, sfxName);
        if (sfxFile.exists() && !ignoreBeatmapSFX) {
            System.out.println("Beatmap SFX found!");
            media = new Media(sfxFile.toURI().toString());
        } else {
            System.out.println("Playing default SFX");
            media = new Media(sfxUrl.toString());
        }

        MediaPlayer player = new MediaPlayer(media);
        player.setVolume(SFX_VOLUME);
        player.setOnEndOfMedia(player::dispose);
        player.play();
    }
}
