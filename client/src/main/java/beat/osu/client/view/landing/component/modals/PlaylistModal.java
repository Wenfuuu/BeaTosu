package beat.osu.client.view.landing.component.modals;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import beat.osu.client.view.landing.component.ui.PlaylistContent;
import beat.osu.client.view.landing.component.ui.PlaylistItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlaylistModal extends StackPane {
    
    private VBox contentContainer;
    private PlaylistContent playlistContent;
    
    public PlaylistModal() {
        super();
        initializeComponents();
        setupLayout();
        setupStyling();
        populatePlaylist();
        loadStyles();
        
        System.out.println("PlaylistModal created and initialized");
    }
    
    private void loadStyles() {
        URL modalCssUrl = CssManager.getLandingCssURL("PlaylistModal.css");
        if (modalCssUrl != null) {
            this.getStylesheets().add(modalCssUrl.toExternalForm());
        } else {
            System.err.println("PlaylistModal.css file not found!");
        }
        
        URL itemCssUrl = CssManager.getLandingCssURL("PlaylistItem.css");
        if (itemCssUrl != null) {
            this.getStylesheets().add(itemCssUrl.toExternalForm());
        } else {
            System.err.println("PlaylistItem.css file not found!");
        }
    }

    private void initializeComponents() {
        contentContainer = new VBox();
        playlistContent = new PlaylistContent();
    }
    
    private void setupLayout() {
        HBox header = new HBox();
        Label playlistTitle = new Label("Touhou");
        playlistTitle.getStyleClass().add("playlist-title");
        header.getChildren().add(playlistTitle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 20, 10, 20));
        
        HBox searchArea = new HBox();
        Label searchLabel = new Label("Type to search!");
        searchLabel.getStyleClass().add("search-label");
        searchArea.getChildren().add(searchLabel);
        searchArea.setAlignment(Pos.CENTER_RIGHT);
        searchArea.setPadding(new Insets(0, 20, 10, 20));
        
        contentContainer.getChildren().addAll(header, searchArea, playlistContent);
        contentContainer.setAlignment(Pos.TOP_CENTER);
        
        this.getChildren().add(contentContainer);
        this.setAlignment(Pos.CENTER);
    }

    private void setupStyling() {
        this.getStyleClass().add("playlist-modal");
        contentContainer.getStyleClass().add("playlist-modal-content");
    }
    
    private void populatePlaylist() {
        VBox playlistItemsContainer = new VBox();
        playlistItemsContainer.getStyleClass().add("playlist-items-container");
        
        String[] songs = {
            "Nico Nico Douga - Nichijou of Knight",
            "Demetori - Crimson Belvedere ~ Eastern Dream",
            "Dark PHOENiX - Green-Eyed Jealousy",
            "UNDEAD CORPORATION - Yoru Naku Usagi wa Yume wo Miru",
            "Ayakura Mei - Romantic Fall",
            "Demetori - Emotional Skyscraper ~ World's End",
            "3L - Three Magic",
            "Demetori - Kagayaku Hari no Kobito-zoku ~ Counter-Attack of the Weak",
            "Hanataba - Night of Knights",
            "Halozy - PLAZMA",
            "UNDEAD CORPORATION - Everything will freeze",
            "EastNewSound - Lucid Dream",
            "Nico Nico Douga - Nichijou of Knight",
            "Demetori - Crimson Belvedere ~ Eastern Dream",
            "Dark PHOENiX - Green-Eyed Jealousy",
            "UNDEAD CORPORATION - Yoru Naku Usagi wa Yume wo Miru",
            "Ayakura Mei - Romantic Fall",
            "Demetori - Emotional Skyscraper ~ World's End",
            "3L - Three Magic",
            "Demetori - Kagayaku Hari no Kobito-zoku ~ Counter-Attack of the Weak",
            "Hanataba - Night of Knights",
            "Halozy - PLAZMA",
            "UNDEAD CORPORATION - Everything will freeze",
            "EastNewSound - Lucid Dream",
            "Nico Nico Douga - Nichijou of Knight",
            "Demetori - Crimson Belvedere ~ Eastern Dream",
            "Dark PHOENiX - Green-Eyed Jealousy",
            "UNDEAD CORPORATION - Yoru Naku Usagi wa Yume wo Miru",
            "Ayakura Mei - Romantic Fall",
            "Demetori - Emotional Skyscraper ~ World's End",
            "3L - Three Magic",
            "Demetori - Kagayaku Hari no Kobito-zoku ~ Counter-Attack of the Weak",
            "Hanataba - Night of Knights",
            "Halozy - PLAZMA",
            "UNDEAD CORPORATION - Everything will freeze",
            "EastNewSound - Lucid Dream",
            "Halozy - PLAZMA",
            "UNDEAD CORPORATION - Everything will freeze",
            "EastNewSound - Lucid Dream",
            "Nico Nico Douga - Nichijou of Knight",
            "Demetori - Crimson Belvedere ~ Eastern Dream",
            "Dark PHOENiX - Green-Eyed Jealousy",
            "UNDEAD CORPORATION - Yoru Naku Usagi wa Yume wo Miru",
            "Ayakura Mei - Romantic Fall",
            "Demetori - Emotional Skyscraper ~ World's End",
            "3L - Three Magic",
            "Demetori - Kagayaku Hari no Kobito-zoku ~ Counter-Attack of the Weak",
            "Hanataba - Night of Knights",
            "Halozy - PLAZMA",
            "UNDEAD CORPORATION - Everything will freeze",
            "EastNewSound - Lucid Dream",
        };
        
        for (String song : songs) {
            PlaylistItem playlistItem = new PlaylistItem(song);
            playlistItemsContainer.getChildren().add(playlistItem);
        }
        
        playlistContent.setContent(playlistItemsContainer);

        System.out.println("Populated playlist with " + songs.length + " items");
    }
}
