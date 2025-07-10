package beat.osu.client.helper;

import beat.osu.client.Main;
import beat.osu.client.config.ConfigurationManager;
import beat.osu.client.enums.SfxType;
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
    @Getter
    private static boolean ignoreBeatmapSFX;

    public static void setSFX_VOLUME(double SFX_VOLUME) {
        SfxManager.SFX_VOLUME = SFX_VOLUME;
        ConfigurationManager.getInstance().setSfxVolume(SFX_VOLUME);
    }

    public static void setIgnoreBeatmapSFX(boolean ignore) {
        SfxManager.ignoreBeatmapSFX = ignore;
        ConfigurationManager.getInstance().setIgnoreBeatmapHitsounds(ignore);
    }

    static {
        SFX_VOLUME = ConfigurationManager.getInstance().getSfxVolume();
        ignoreBeatmapSFX = ConfigurationManager.getInstance().getIgnoreBeatmapHitsounds();
    }

    private static URL getSfxResource(String sfxName) {
        return Main.class.getResource("/assets/audio/sfx/" + sfxName);
    }

    private static void playSfx(Media media) {
        MediaPlayer player = new MediaPlayer(media);
        player.setVolume(SFX_VOLUME);
        player.setOnEndOfMedia(player::dispose);
        player.play();
    }

    public static void playBeatmapSfx(String sfxName) {
        Media media = null;

        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        File beatmapDir = new File(ResourceManager.getBeatmapDirectory(), String.valueOf(beatmap.getBeatmapSetId()));
        File sfxFile = new File(beatmapDir, sfxName);
        if (sfxFile.exists() && !ignoreBeatmapSFX) {
            System.out.println("Beatmap SFX found: " + sfxName);
            media = new Media(sfxFile.toURI().toString());
        } else {
            // remove the index from the sfxName, e.g. "soft-hitnormal1.wav" to "soft-hitnormal.wav"
            String baseName = sfxName;
            int dotIndex = sfxName.lastIndexOf('.');
            if (dotIndex > 0) {
                String nameWithoutExtension = sfxName.substring(0, dotIndex);
                String extension = sfxName.substring(dotIndex);
                String nameStripped = nameWithoutExtension.replaceAll("\\d+$", "");
                baseName = nameStripped + extension;
            }

            URL sfxUrl = getSfxResource("gameplay/"+ baseName);
            if (sfxUrl == null) {
                System.err.println("SFX resource not found: " + baseName);
                return;
            }
            System.out.println("Playing default SFX: " + baseName);
            media = new Media(sfxUrl.toString());
        }

        playSfx(media);
    }

    public static void playMenuSfx(SfxType sfxType) {
        URL sfxUrl = null;

        switch (sfxType) {
            case MENU_HIT:
                sfxUrl = getSfxResource("menu/menu-hit.wav");
                break;
            case MENU_HOVER:
                sfxUrl = getSfxResource("menu/menu-hover.wav");
                break;
            case MENU_BACK:
                sfxUrl = getSfxResource("menu/menu-back.wav");
                break;
            case SELECT_BEATMAP:
                sfxUrl = getSfxResource("menu/select-beatmap.mp3");
                break;
            case PAUSE_HOVER:
                sfxUrl = getSfxResource("menu/pause-hover.wav");
                break;
            case PAUSE_CLICK:
                sfxUrl = getSfxResource("menu/pause-click.wav");
                break;
            case KEY_DELETE:
                sfxUrl = getSfxResource("keys/key-delete.mp3");
                break;
            case KEY_PRESS:
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                sfxUrl = getSfxResource("keys/key-press-" + randomKeyPress + ".mp3");
                break;
            case SELECT_EXPAND:
                sfxUrl = getSfxResource("menu/select-expand.mp3");
                break;
            default:
                System.err.println("Unknown SFX type: " + sfxType);
                return;
        }

        Media media = new Media(sfxUrl.toString());
        playSfx(media);
    }
}
