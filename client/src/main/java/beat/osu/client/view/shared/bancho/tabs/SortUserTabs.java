package beat.osu.client.view.shared.bancho.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import beat.osu.client.view.shared.bancho.buttons.SortUsersButton;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

public class SortUserTabs extends HBox {

    private List<SortUsersButton> buttons;
    @Getter
    private SortUsersButton selectedButton;
    @Setter
    private Consumer<String> onSelectionChanged;

    public SortUserTabs() {
        super();
        this.buttons = new ArrayList<>();
        setupUI();
    }

    private void setupUI() {
        this.setPadding(new Insets(0, 0, 0, 0));

        SortUsersButton nameButton = new SortUsersButton("Name");
        SortUsersButton rankButton = new SortUsersButton("Rank");
        SortUsersButton locationButton = new SortUsersButton("Location");
        SortUsersButton timeZoneButton = new SortUsersButton("Time Zone");

        buttons.add(nameButton);
        buttons.add(rankButton);
        buttons.add(locationButton);
        buttons.add(timeZoneButton);

        for (SortUsersButton button : buttons) {
            button.setOnAction(event -> selectButton(button));
        }

        this.getChildren().addAll(nameButton, rankButton, locationButton, timeZoneButton);
        
        selectButton(nameButton);
    }

    private void selectButton(SortUsersButton buttonToSelect) {
        for (SortUsersButton button : buttons) {
            button.setSelected(false);
        }
        
        buttonToSelect.setSelected(true);
        this.selectedButton = buttonToSelect;
        
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(buttonToSelect.getText());
        }
    }

    public String getSelectedSortType() {
        return selectedButton != null ? selectedButton.getText() : null;
    }

    public void selectByText(String text) {
        for (SortUsersButton button : buttons) {
            if (button.getText().equals(text)) {
                selectButton(button);
                break;
            }
        }
    }
}
