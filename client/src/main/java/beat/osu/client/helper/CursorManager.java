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

    private static final double CURSOR_SIZE = 54.0;

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

                Image scaledCursor = new Image(
                        Objects.requireNonNull(Main.class.getResource(CURSOR_IMAGE_PATH)).toExternalForm(),
                        CURSOR_SIZE, CURSOR_SIZE, true, true
                );

                imageCursor = new ImageCursor(scaledCursor, CURSOR_SIZE / 2, CURSOR_SIZE / 2);
            }
        }
        return imageCursor;
    }

    public static void applyCursor(Scene scene) {
        if (scene == null) return;

        try {
            ImageCursor cursor = getImageCursor();
            if (cursor != null) {
                scene.setCursor(cursor);
            }
        } catch (Exception e) {
            // System.err.println("Failed to apply cursor to scene: " + e.getMessage());
        }
    }

    public static ImageView createCursorImageView() {
        try {
            Image cursor = getCursorImage();
            if (cursor != null) {
                return new ImageView(new Image(Objects.requireNonNull(Main.class
                        .getResource(CURSOR_IMAGE_PATH)).toExternalForm(), CURSOR_SIZE, CURSOR_SIZE, true, true));
            }
        } catch (Exception e) {
        }
        return null;
    }
}
