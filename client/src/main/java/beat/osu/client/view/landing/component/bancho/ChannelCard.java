package beat.osu.client.view.landing.component.bancho;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;

public class ChannelCard extends HBox {
    private static final double CARD_WIDTH = ScreenManager.SCREEN_WIDTH * 0.50;
    private Integer id;

    @Getter
    private String name;

    @Getter
    private String description;

    @Getter
    private Integer memberCount;

    @Getter
    private Boolean isJoined;

    private Label nameLabel;
    private Label descriptionLabel;
    private Label memberCountLabel;

    public ChannelCard(int id, String name, String description, int memberCount, boolean isJoined) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.isJoined = isJoined;

        initializeUI();
        updateStyles();
    }

    private void initializeUI() {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(8, 18, 8, 18));
        this.setSpacing(15);
        this.setPrefHeight(60);
        
        this.setPrefWidth(CARD_WIDTH);
        this.setMinWidth(CARD_WIDTH);
        this.setMaxWidth(CARD_WIDTH);
        
        this.getStyleClass().add("channel-card");

        nameLabel = new Label(name);
        nameLabel.getStyleClass().add("channel-name");
        nameLabel.setMinWidth(120);
        nameLabel.setPrefWidth(120);

        HBox descriptionContainer = new HBox();
        descriptionContainer.setAlignment(Pos.CENTER_LEFT);
        descriptionContainer.getStyleClass().add("description-container");

        descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("channel-description");

        memberCountLabel = new Label("(" + memberCount + " users)");
        memberCountLabel.getStyleClass().add("channel-description");

        descriptionContainer.getChildren().addAll(descriptionLabel, memberCountLabel);
        HBox.setHgrow(descriptionContainer, Priority.ALWAYS);
        this.getChildren().addAll(nameLabel, descriptionContainer);
    }

    private void updateStyles() {
        URL cssUrl = CssManager.getLandingCssURL("ChannelCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.getStyleClass().removeAll("channel-card-joined", "channel-card-not-joined");

        if (isJoined) {
            this.getStyleClass().add("channel-card-joined");
        } else {
            this.getStyleClass().add("channel-card-not-joined");
        }
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
        this.memberCountLabel.setText("(" + memberCount + " users)");
    }

    public void setJoined(boolean joined) {
        this.isJoined = joined;
        updateStyles();
    }
}
