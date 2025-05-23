package beat.osu.beatosu.helper;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class InputManager {
    @Getter
    private Set<KeyCode> pressedKeys;
    @Getter
    private Set<MouseButton> mouseClicks;
    private final StringBuffer typedChars;

    @Getter
    private static KeyCode keybind1 = KeyCode.Z;
    @Getter
    private static KeyCode keybind2 = KeyCode.X;

    public InputManager(Scene scene) {
        pressedKeys = new HashSet<>();
        mouseClicks = new HashSet<>();
        typedChars = new StringBuffer();

        clearTypedChars();
        handlePlayerInput(scene);
    }

    public String getTypedChars() {
        return typedChars.toString();
    }
    public void clearTypedChars() {
        typedChars.setLength(0);
    }

    private void handlePlayerInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            KeyCode keyCode = e.getCode();
            switch (keyCode) {
//                case Z:
//                case X:
//                case SPACE:
//                case ESCAPE:
//                    pressedKeys.add(keyCode);
//                    break;
                case BACK_SPACE:
                    if (typedChars.length() > 0) {
                        typedChars.deleteCharAt(typedChars.length() - 1);
                    }
//                    System.out.println("Typed: " + typedChars);
                    break;
                default:
                    pressedKeys.add(keyCode);
                    break;
            }

//            System.out.println(keyCode);
            if (keyCode.isLetterKey() || keyCode.isDigitKey()) {
                typedChars.append(keyCode.getChar().toLowerCase().charAt(0));
//                System.out.println("Typed: " + typedChars);
            } else if (keyCode == KeyCode.SPACE) {
                typedChars.append(' ');
//                System.out.println("Typed: " + typedChars);
            }
        });

        scene.setOnKeyReleased(e -> {
            KeyCode keyCode = e.getCode();
            pressedKeys.remove(keyCode);
        });

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            mouseClicks.add(e.getButton());
        });

        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            mouseClicks.remove(e.getButton());
        });
    }
}
