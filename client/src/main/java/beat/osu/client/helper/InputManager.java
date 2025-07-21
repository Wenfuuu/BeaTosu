package beat.osu.client.helper;

import beat.osu.client.config.ConfigurationManager;
import beat.osu.client.enums.SfxType;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class InputManager {
    @Getter
    private final Set<KeyCode> pressedKeys;
    @Getter
    private final Set<MouseButton> mouseClicks;
    private final StringBuffer typedChars;

    @Getter
    private static KeyCode keybind1;
    @Getter
    private static KeyCode keybind2;
    @Getter
    @Setter
    private boolean sfxDisabled = false;

    public static void setKeybind1(KeyCode keybind1) {
        InputManager.keybind1 = keybind1;
        ConfigurationManager.getInstance().setKeybind1(keybind1.name());
    }

    public static void setKeybind2(KeyCode keybind2) {
        InputManager.keybind2 = keybind2;
        ConfigurationManager.getInstance().setKeybind2(keybind2.name());
    }

    static {
        keybind1 = KeyCode.valueOf(ConfigurationManager.getInstance().getKeybind1());
        keybind2 = KeyCode.valueOf(ConfigurationManager.getInstance().getKeybind2());
    }

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

    public void setTypedChars(String chars) {
        typedChars.append(chars);
    }

    public void clearTypedChars() {
        typedChars.setLength(0);
    }

    public boolean checkCheat() {
        String cheatCode = "betty";
        if (typedChars.toString().equalsIgnoreCase(cheatCode)) {
            clearTypedChars();
            return true;
        }
        return false;
    }

    private void handlePlayerInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            KeyCode keyCode = e.getCode();
            if (Objects.requireNonNull(keyCode) == KeyCode.BACK_SPACE) {
                if (typedChars.length() > 0) {
                    typedChars.deleteCharAt(typedChars.length() - 1);
                }
                if (!sfxDisabled) {
                    SfxManager.playMenuSfx(SfxType.KEY_DELETE);
                }
            } else if (keyCode == KeyCode.SPACE) {
                typedChars.append(" ");
                if (!sfxDisabled) {
                    SfxManager.playMenuSfx(SfxType.KEY_PRESS);
                }
            } else if (keyCode.isLetterKey() || keyCode.isDigitKey()) {
                typedChars.append(keyCode.getChar().toLowerCase().charAt(0));
                pressedKeys.add(keyCode);
                if (!sfxDisabled) {
                    SfxManager.playMenuSfx(SfxType.KEY_PRESS);
                }
            } else {
                pressedKeys.add(keyCode);
                if (!sfxDisabled) {
                    SfxManager.playMenuSfx(SfxType.KEY_PRESS);
                }
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
