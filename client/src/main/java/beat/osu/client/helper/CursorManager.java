package beat.osu.client.helper;

import beat.osu.client.Main;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class CursorManager {
    private static final String CURSOR_IMAGE_PATH = "/assets/images/misc/cursor.png";
    private static Image cursorImage;
    private static ImageCursor imageCursor;

    private CursorManager() {

    }

    public static Image getCursorImage() {
        if (cursorImage == null) {
            try {
                cursorImage = new Image(Objects.requireNonNull(Main.class
                        .getResource(CURSOR_IMAGE_PATH)).toExternalForm());
            } catch (Exception e) {
                return null;
            }
        }
        return cursorImage;
    }

    public static ImageCursor getImageCursor() {
        if (imageCursor == null) {
            Image cursor = getCursorImage();
            if (cursor != null) {
                double cursorSize = isWindows() ? 32.0 : 64.0;

                Image scaledCursor = new Image(
                        Objects.requireNonNull(Main.class.getResource(CURSOR_IMAGE_PATH)).toExternalForm(),
                        cursorSize, cursorSize, true, true
                );

                imageCursor = new ImageCursor(scaledCursor, cursorSize / 2, cursorSize / 2);
            }
        }
        return imageCursor;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static void applyCursor(Scene scene) {
        if (scene == null) return;

        try {
            ImageCursor cursor = getImageCursor();
            if (cursor != null) {
                scene.setCursor(cursor);
            }
        } catch (Exception e) {
        }
    }

    public static ImageView createCursorImageView() {
        try {
            Image cursor = getCursorImage();
            if (cursor != null) {
                double cursorSize = isWindows() ? 32.0 : 64.0;

                return new ImageView(new Image(Objects.requireNonNull(Main.class
                        .getResource(CURSOR_IMAGE_PATH)).toExternalForm(), cursorSize, cursorSize, true, true));
            }
        } catch (Exception e) {
        }
        return null;
    }
}
