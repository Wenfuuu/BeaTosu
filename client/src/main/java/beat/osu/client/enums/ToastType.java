package beat.osu.client.enums;

import lombok.Getter;

@Getter
public enum ToastType {
    SUCCESS("#4CAF50", "#FFFFFF", "✓"),
    INFORMATION("#2196F3", "#FFFFFF", "ℹ"),
    ERROR("#F44336", "#FFFFFF", "✕");

    private final String backgroundColor;
    private final String textColor;
    private final String icon;

    ToastType(String backgroundColor, String textColor, String icon) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.icon = icon;
    }
}
