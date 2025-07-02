package beat.osu.client.enums;

import lombok.Getter;

@Getter
public enum ToastType {
    SUCCESS("#48994b", "#FFFFFF"),
    INFORMATION("#2196F3", "#FFFFFF"),
    ERROR("#793622", "#FFFFFF");

    private final String borderColor;
    private final String textColor;

    ToastType(String borderColor, String textColor) {
        this.borderColor = borderColor;
        this.textColor = textColor;
    }
}
