package beat.osu.client.view.home.component;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.helper.StageManager;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.utils.OszExtractor;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UploadBox extends VBox {

    private Label dropText;
    private FileChooser fileChooser;
    @Setter
    private Runnable onUploadCompleteCallback;

    public UploadBox() {
        this.dropText = new Label("Upload your beatmap here");
        this.fileChooser = new FileChooser();

        initializeComponents();
        setupLayout();
        loadStyles();

        handleEvent();
    }

    private void initializeComponents() {
        this.getStyleClass().add("drop-box");
    }

    private void setupLayout() {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(50));

        this.getChildren().add(dropText);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("UploadBox.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void handleEvent() {
        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                handleFileUpload(db.getFiles());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        this.setOnMouseClicked(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(StageManager.getStage());
            if (files != null) {
                handleFileUpload(files);
            }
        });
    }

    private void handleFileUpload(List<File> files) {
        File beatmapDir = ResourceManager.getBeatmapDirectory();

        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() {
                int totalFiles = files.size();

                for (int i = 0; i < totalFiles; i++) {
                    File file = files.get(i);
                    // Use replaceAll with regular expression to remove "[no video]"
                    String filename = file.getName().replaceAll("\\s*\\[no video\\]", "");
                    Path destPath = new File(beatmapDir, filename).toPath();

                    if(!filename.endsWith(".osz")) continue;
                    try {
                        Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied: " + filename);

                        String[] tempStr = filename.split(" ");
                        String beatmapSetId = tempStr[0];

                        //extract .osz and store in temp folder
                        File tempDir = ResourceManager.getTempDirectory();
                        File outputDir = new File(tempDir, beatmapSetId);

                        System.out.println("extracting beatmap set id: " + beatmapSetId);
                        OszExtractor.extractOsz(new File(beatmapDir, filename), outputDir);
                        //parse all .osu file in temp folder & insert db
                        File []files = outputDir.listFiles();
                        File detectedAudioFile = null;

                        if (files != null) {
                            for (File f : files) {
                                String name = f.getName().toLowerCase();
                                if (name.endsWith(".mp3")) {
                                    detectedAudioFile = f;
                                    break;
                                }
                            }
                        }

                        if(detectedAudioFile != null) {
                            File renamedAudioFile = new File(outputDir, "audio.mp3");
                            try {
                                Files.move(detectedAudioFile.toPath(), renamedAudioFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                System.out.println("Failed to rename audio file.");
                                e.printStackTrace();
                            }
                        }

                        CountDownLatch latch = new CountDownLatch(1);
                        // Store the duration in an array to access it from the lambda
                        final double[] audioDuration = {0.0};
//                        File songFile = new File("./src/main/resources/temp/" + beatmapSetId + "/audio.mp3");
                        File songFile = new File(outputDir, "audio.mp3");
                        Media song = new Media(songFile.toURI().toString());
                        MediaPlayer player = new MediaPlayer(song);
                        player.setOnReady(() -> {
                            audioDuration[0] = player.getMedia().getDuration().toSeconds();
                            latch.countDown(); // Signal that media is ready
                        });
                        // Wait for media to be ready (with timeout)
                        try {
                            latch.await(10, TimeUnit.SECONDS); // Wait up to 10 seconds
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        int minutes = (int) audioDuration[0] / 60;
                        int seconds = (int) audioDuration[0] % 60;
                        String timeString = String.format("%02d:%02d", minutes, seconds);

                        if(files != null) {
                            boolean insertSet = false;
                            for(File f: files) {
                                if(f.getName().endsWith(".osu")) {
                                    try {
                                        OsuParser.parseOsuFile(f);

                                        if (!insertSet) {
                                            OsuParser.insertBeatmapSet(timeString);
                                            insertSet = true;
                                            Thread.sleep(500);
                                        }

                                        OsuParser.insertData();
                                        Thread.sleep(100);

                                    } catch (Exception e) {
                                        System.err.println("Error processing .osu file: " + f.getName());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Failed to copy: " + file.getName());
                        throw new RuntimeException(e);
                    }

                    double progress = (i + 1) / (double) totalFiles;
                    updateProgress(progress, 1);
                }

                return null;
            }

            @Override
            protected void succeeded() {
                dropText.setText("Uploaded " + files.size() + " file(s)");
                if (onUploadCompleteCallback != null) {
                    onUploadCompleteCallback.run();
                }
            }

            @Override
            protected void failed() {
                dropText.setText("Upload failed, beatmap already exists");
            }
        };

//        progressBar.progressProperty().bind(uploadTask.progressProperty());
        new Thread(uploadTask).start();
    }
}
