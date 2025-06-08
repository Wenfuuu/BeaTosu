package beat.osu.client.view.landing.component.bancho.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import beat.osu.client.view.landing.component.bancho.buttons.AddChatButton;
import beat.osu.client.view.landing.component.bancho.buttons.ChatTabButton;
import beat.osu.shared.dto.chat.ChannelDto;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import lombok.Setter;

public class ChatTabs extends HBox {
    
    private List<ChannelDto> joinedChannels;
    private ChannelDto currentSelectedChannel;
    private AddChatButton addChatButton;

    @Setter
    private Consumer<ChannelDto> onChannelSelected;
    @Setter
    private Consumer<ChatTabButton> onChannelClosed;
    @Setter
    private Runnable onAddChannelRequested;
    
    public ChatTabs() {
        super();
        this.joinedChannels = new ArrayList<>();
        
        setupUI();
    }
    
    private void setupUI() {
        this.setPadding(new Insets(0, 2, 0, 2));
        
        BackgroundFill backgroundFill = new BackgroundFill(
            Color.rgb(255, 255, 255, 0.1),
            null,null
        );
        this.setBackground(new Background(backgroundFill));

        addChatButton = new AddChatButton();
        addChatButton.setOnAction(e -> {
            if (onAddChannelRequested != null) {
                onAddChannelRequested.run();
            }
        });
        
        this.getChildren().add(addChatButton);
    }
    
    public void setJoinedChannels(List<ChannelDto> channels) {
        this.joinedChannels = new ArrayList<>(channels);
        refreshDisplay();
    }
    
    public void addChannel(ChannelDto channel) {
        boolean alreadyExists = joinedChannels.stream()
                .anyMatch(c -> Objects.equals(c.getId(), channel.getId()));
        
        if (!alreadyExists) {
            joinedChannels.add(channel);
            refreshDisplay();
        }
    }
    
    public void removeChannel(int channelId) {
        boolean removed = joinedChannels.removeIf(channel -> channel.getId() == channelId);
        
        if (removed) {
            if (currentSelectedChannel != null && currentSelectedChannel.getId() == channelId) {
                currentSelectedChannel = null;
                if (!joinedChannels.isEmpty() && onChannelSelected != null) {
                    selectChannel(joinedChannels.get(0));
                }
            }
            refreshDisplay();
        }
    }
    
    public void selectChannel(ChannelDto channel) {
        currentSelectedChannel = channel;
        updateTabSelection();
        
        if (onChannelSelected != null) {
            onChannelSelected.accept(channel);
        }
    }
    
    private void refreshDisplay() {
        this.getChildren().clear();
        this.getChildren().add(addChatButton);
        
        for (ChannelDto channel : joinedChannels) {
            ChatTabButton tabButton = new ChatTabButton(channel.getName());
            tabButton.setOnAction(e -> selectChannel(channel));
            tabButton.setOnCloseAction(this::handleChannelClose);
            
            if (currentSelectedChannel != null && Objects.equals(currentSelectedChannel.getId(), channel.getId())) {
                tabButton.setSelected(true);
            }
            
            this.getChildren().add(this.getChildren().size() - 1, tabButton);
        }
        
        if (currentSelectedChannel == null && !joinedChannels.isEmpty()) {
            selectChannel(joinedChannels.get(0));
        }
    }
    
    private void updateTabSelection() {
        for (Node node : this.getChildren()) {
            if (node instanceof ChatTabButton) {
                ChatTabButton tab = (ChatTabButton) node;
                tab.setSelected(currentSelectedChannel != null && 
                    tab.getTabText().equals(currentSelectedChannel.getName()));
            }
        }
    }
    
    private void handleChannelClose(ChatTabButton tabButton) {
        if (onChannelClosed != null) {
            onChannelClosed.accept(tabButton);
        }
    }
    
    public List<ChannelDto> getJoinedChannels() {
        return new ArrayList<>(joinedChannels);
    }

    public void setCurrentSelectedChannel(ChannelDto channel) {
        this.currentSelectedChannel = channel;
        updateTabSelection();
    }
    
    public boolean hasChannels() {
        return !joinedChannels.isEmpty();
    }
    
    public ChannelDto getFirstChannel() {
        return joinedChannels.isEmpty() ? null : joinedChannels.get(0);
    }

}
