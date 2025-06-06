package beat.osu.client.view.landing.component.bancho;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import beat.osu.client.controller.ChannelController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.view.landing.component.bancho.buttons.BanchoButtons;
import beat.osu.client.view.landing.component.bancho.panels.ChatPanel;
import beat.osu.client.view.landing.component.layout.BottomBar;
import beat.osu.client.view.landing.component.layout.TopBar;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.responses.GetAllChannelsResponse;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class SelectChannelModal extends VBox {

    private Label selectChannelTitle;
    private Label searchLabel;
    private TextField searchField;
    private Button closeButton;

    private ScrollPane scrollPane;
    private VBox channelContainer;
    private List<ChannelCard> channelCards;
    
    private ChannelController channelController;
    private List<ChannelDto> allChannels;

    @Setter
    private ChatPanel chatPanel;
    private BanchoButtons banchoButtons;
    private BottomBar bottomBar;
    private TopBar topBar;

    public SelectChannelModal(BanchoButtons banchoButtons, BottomBar bottomBar, TopBar topBar) {
        this.banchoButtons = banchoButtons;
        this.bottomBar = bottomBar;
        this.topBar = topBar;

        this.channelCards = new ArrayList<>();
        this.channelController = new ChannelController();
        this.allChannels = new ArrayList<>();
        
        initializeUI();
        loadStyles();
    }

    private void initializeUI() {
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.getStyleClass().add("select-channel-modal");
        this.setPrefWidth(600);
        this.setPrefHeight(500);

        selectChannelTitle = new Label("Select any channel you wish to join!");
        selectChannelTitle.getStyleClass().add("modal-title");

        HBox searchSection = new HBox(10);
        searchSection.setAlignment(Pos.CENTER_LEFT);
        
        searchLabel = new Label("Search:");
        searchLabel.getStyleClass().add("search-label");
        
        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterChannels(newValue));
        
        searchSection.getChildren().addAll(searchLabel, searchField);

        channelContainer = new VBox(8);
        channelContainer.setPadding(new Insets(10));
        
        scrollPane = new ScrollPane(channelContainer);
        scrollPane.getStyleClass().add("channel-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        closeButton = new Button("1. Close");
        closeButton.getStyleClass().add("close-button");
        closeButton.setPrefWidth(150);
        closeButton.setOnAction(e -> {
            this.setVisible(false);
            banchoButtons.setVisible(true);
            bottomBar.setFullOpacity();
            topBar.setFullOpacity();
            chatPanel.show();
        });

        this.getChildren().addAll(selectChannelTitle, searchSection, scrollPane, closeButton);
        this.setVisible(false);
    }

    private void loadChannels() {
        try {
            Result<GetAllChannelsResponse> result = channelController.getAllChannels().get();
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    GetAllChannelsResponse response = result.getValue();
                    for (ChannelDto channel : response.getChannels()) {
                        System.out.println("Channel: " + channel.getName() + " (ID: " + channel.getId() + ")");
                    }
                    this.allChannels = response.getChannels();
                    displayChannels(this.allChannels);
                } else {
                    System.out.println(result.getError().getMessage());
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayChannels(List<ChannelDto> channels) {
        channelCards.clear();
        channelContainer.getChildren().clear();

        for (ChannelDto channel : channels) {
            ChannelCard card = new ChannelCard(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getMemberCount(),
                channel.isJoined()
            );
            
            card.setOnMouseClicked(e -> handleChannelClick(card, channel));
            
            channelCards.add(card);
            channelContainer.getChildren().add(card);
        }
    }

    private void handleChannelClick(ChannelCard card, ChannelDto channel) {
        if (card.isJoined()) {
            channelController.leaveChannel(channel.getId()).thenAccept(result -> {
                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        channel.setJoined(false);
                        refreshChannelDisplay();
                    }
                });
            });
        } else {
            channelController.joinChannel(channel.getId()).thenAccept(result -> {
                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        channel.setJoined(true);
                        refreshChannelDisplay();
                    }
                });
            });
        }
    }

    private void refreshChannelDisplay() {
        String currentSearchText = searchField.getText();
        if (currentSearchText.isEmpty()) {
            displayChannels(allChannels);
        } else {
            filterChannels(currentSearchText);
        }
    }

    private void filterChannels(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            displayChannels(allChannels);
            return;
        }

        String lowerCaseSearch = searchText.toLowerCase().trim();
        List<ChannelDto> filteredChannels = allChannels.stream()
            .filter(channel -> 
                channel.getName().toLowerCase().contains(lowerCaseSearch) ||
                channel.getDescription().toLowerCase().contains(lowerCaseSearch))
            .collect(Collectors.toList());

        displayChannels(filteredChannels);
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.play();

        this.setVisible(false);
    }

    public void show() {
        this.loadChannels();
        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("SelectChannelModal.css");

        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}