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
                System.err.println("Failed to load cursor image: " + e.getMessage());
                return null;
            }
        }
        return cursorImage;
    }

    public static ImageCursor getImageCursor() {
        if (imageCursor == null) {
            Image cursor = getCursorImage();
            if (cursor != null) {
                imageCursor = new ImageCursor(cursor, cursor.getWidth() / 2, cursor.getHeight() / 2);
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
            System.err.println("Failed to apply cursor to scene: " + e.getMessage());
        }
    }

    public static ImageView createCursorImageView(double width, double height) {
        try {
            Image cursor = getCursorImage();
            if (cursor != null) {
                return new ImageView(new Image(Objects.requireNonNull(Main.class
                        .getResource(CURSOR_IMAGE_PATH)).toExternalForm(), width, height, true, true));
            }
        } catch (Exception e) {
            System.err.println("Failed to create cursor ImageView: " + e.getMessage());
        }
        return null;
    }
}
