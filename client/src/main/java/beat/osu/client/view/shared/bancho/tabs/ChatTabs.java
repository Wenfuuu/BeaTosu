package beat.osu.client.view.shared.bancho.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.shared.bancho.buttons.AddChatButton;
import beat.osu.client.view.shared.bancho.buttons.ChatTabButton;
import beat.osu.shared.dto.chat.ChannelDto;
import beat.osu.shared.dto.chat.PrivateChatDto;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

public class ChatTabs extends HBox {

    private List<ChannelDto> joinedChannels;
    private List<PrivateChatDto> privateChats;

    private AddChatButton addChatButton;

    @Getter
    private Object currentSelectedTab; 

    @Setter
    private Consumer<Object> onTabSelected; 
    @Setter
    private Consumer<ChatTabButton> onTabClosed;
    @Setter
    private Runnable onAddChannelRequested;
    
    public ChatTabs() {
        super();
        this.joinedChannels = new ArrayList<>();
        this.privateChats = new ArrayList<>();
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
        addChatButton.setOnMouseClicked(e -> {
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
        int removedIndex = -1;
        for (int i = 0; i < joinedChannels.size(); i++) {
            if (joinedChannels.get(i).getId() == channelId) {
                removedIndex = i;
                break;
            }
        }
        
        boolean removed = joinedChannels.removeIf(channel -> channel.getId() == channelId);
        
        if (removed) {
            Object newSelectedTab = null;
            
            if (currentSelectedTab instanceof ChannelDto && ((ChannelDto) currentSelectedTab).getId() == channelId) {
                newSelectedTab = selectNewTabAfterRemoval(removedIndex);
                currentSelectedTab = newSelectedTab;
            }
            
            refreshDisplay();
            
            if (newSelectedTab != null && onTabSelected != null) {
                onTabSelected.accept(newSelectedTab);
            }
        }
    }
    
    public void addPrivateChat(PrivateChatDto privateChat) {
        boolean alreadyExists = privateChats.stream()
                .anyMatch(c -> c.getOtherUserId() == privateChat.getOtherUserId());
        if (!alreadyExists) {
            privateChats.add(privateChat);
            refreshDisplay();
        }
    }

    public void removePrivateChat(int otherUserId) {
        int removedIndex = joinedChannels.size(); 
        for (int i = 0; i < privateChats.size(); i++) {
            if (privateChats.get(i).getOtherUserId() == otherUserId) {
                removedIndex += i;
                break;
            }
        }
        
        boolean removed = privateChats.removeIf(chat -> chat.getOtherUserId() == otherUserId);
        if (removed) {
            Object newSelectedTab = null;
            
            if (currentSelectedTab instanceof PrivateChatDto && ((PrivateChatDto) currentSelectedTab).getOtherUserId() == otherUserId) {
                newSelectedTab = selectNewTabAfterRemoval(removedIndex);
                currentSelectedTab = newSelectedTab;
            }
            
            refreshDisplay();
            
            if (newSelectedTab != null && onTabSelected != null) {
                onTabSelected.accept(newSelectedTab);
            }
        }
    }

    private Object selectNewTabAfterRemoval(int removedIndex) {
        List<Object> allTabs = new ArrayList<>();
        allTabs.addAll(joinedChannels);
        allTabs.addAll(privateChats);
        
        if (allTabs.isEmpty()) {
            return null;
        }
        
        if (removedIndex < allTabs.size()) {
            return allTabs.get(removedIndex);
        }
        
        return allTabs.get(allTabs.size() - 1);
    }

    public void selectTab(Object tab) {
        currentSelectedTab = tab;
        updateTabSelection();
        if (onTabSelected != null) {
            onTabSelected.accept(tab);
        }
    }

    private void refreshDisplay() {
        this.getChildren().clear();
        this.getChildren().add(addChatButton);
        for (ChannelDto channel : joinedChannels) {
            ChatTabButton tabButton = new ChatTabButton(channel.getName());
            tabButton.setOnMouseClicked(e -> {
                SfxManager.playMenuSfx(SfxType.MENU_HIT);
                selectTab(channel);
            });
            tabButton.setOnCloseAction(this::handleTabClose);
            if (currentSelectedTab instanceof ChannelDto && Objects.equals(((ChannelDto) currentSelectedTab).getId(), channel.getId())) {
                tabButton.setSelected(true);
            }
            this.getChildren().add(this.getChildren().size() - 1, tabButton);
        }
        for (PrivateChatDto privateChat : privateChats) {
            String tabName = privateChat.getOtherUserName();
            ChatTabButton tabButton = new ChatTabButton(tabName);
            tabButton.setOnMouseClicked(e -> {
                SfxManager.playMenuSfx(SfxType.MENU_HIT);
                selectTab(privateChat);
            });
            tabButton.setOnCloseAction(this::handleTabClose);
            if (currentSelectedTab instanceof PrivateChatDto && ((PrivateChatDto) currentSelectedTab).getOtherUserId() == privateChat.getOtherUserId()) {
                tabButton.setSelected(true);
            }
            this.getChildren().add(this.getChildren().size() - 1, tabButton);
        }

        if (currentSelectedTab == null && (!joinedChannels.isEmpty() || !privateChats.isEmpty())) {
            if (!joinedChannels.isEmpty()) {
                selectTab(joinedChannels.get(0));
            } else {
                selectTab(privateChats.get(0));
            }
        }
    }

    private void updateTabSelection() {
        for (Node node : this.getChildren()) {
            if (node instanceof ChatTabButton) {
                ChatTabButton tab = (ChatTabButton) node;
                boolean selected = false;
                if (currentSelectedTab instanceof ChannelDto) {
                    selected = tab.getTabText().equals(((ChannelDto) currentSelectedTab).getName());
                } else if (currentSelectedTab instanceof PrivateChatDto) {
                    selected = tab.getTabText().equals(((PrivateChatDto) currentSelectedTab).getOtherUserName());
                }
                tab.setSelected(selected);
            }
        }
    }

    private void handleTabClose(ChatTabButton tabButton) {
        if (onTabClosed != null) {
            onTabClosed.accept(tabButton);
        }
    }

    public List<ChannelDto> getJoinedChannels() {
        return new ArrayList<>(joinedChannels);
    }

    public List<PrivateChatDto> getPrivateChats() {
        return new ArrayList<>(privateChats);
    }
}