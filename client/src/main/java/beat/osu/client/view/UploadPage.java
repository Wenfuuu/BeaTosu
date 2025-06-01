package beat.osu.client.view;

import beat.osu.client.helper.CssManager;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.utils.OszExtractor;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UploadPage extends Page {

    BorderPane bp;
    VBox mainBox;
    VBox dropBox;
    VBox uploadedFile;
    Label dropText;
    Button chooseFileBtn;
    FileChooser fileChooser;
//    ProgressBar progressBar;

    @Override
    public void init() {
        bp = new BorderPane();
        mainBox = new VBox(20);
        dropBox = new VBox();
        dropBox.getStyleClass().add("drop-box");
        dropText = new Label("Upload beatmap here");
        chooseFileBtn = new Button("Choose File");
        fileChooser = new FileChooser();

        uploadedFile = new VBox(5); // spacing between file entries
        uploadedFile.setPadding(new Insets(10));
        uploadedFile.setAlignment(Pos.TOP_LEFT);

//        progressBar = new ProgressBar(0);
//        progressBar.setPrefWidth(300);
//        progressBar.setVisible(false);

        scene = new Scene(bp, 800, 600);
        URL cssUrl = CssManager.getCssURL("UploadPage.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    @Override
    public void setLayout() {
        bp.setCenter(mainBox);
        BorderPane.setAlignment(mainBox, Pos.CENTER);

        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(20));

        dropBox.setAlignment(Pos.CENTER);
        dropBox.setPadding(new Insets(50));

        dropBox.getChildren().add(dropText);
//        mainBox.getChildren().addAll(dropBox, chooseFileBtn, progressBar, uploadedFile);
        mainBox.getChildren().addAll(dropBox, chooseFileBtn, uploadedFile);
    }

    public void handleEvent() {
        // drag and drop
        dropBox.setOnDragOver(event -> {
            if (event.getGestureSource() != dropBox && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                handleFileUpload(db.getFiles());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        dropBox.setOnMouseClicked(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null) {
                handleFileUpload(files);
            }
        });

        // file chooser
        chooseFileBtn.setOnMouseClicked(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null) {
                handleFileUpload(files);
            }
        });
    }

    private void handleFileUpload(List<File> files) {
        File beatmapDir = new File("./src/main/resources/assets/beatmap");
        if(!beatmapDir.exists()) {
            if(beatmapDir.mkdirs()){
                System.out.println("creating beatmap directory");
            }
        }

        uploadedFile.getChildren().clear();
        // progress bar
//        progressBar.setProgress(0);
//        progressBar.setVisible(true);

        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() {
                int totalFiles = files.size();

                for (int i = 0; i < totalFiles; i++) {
                    File file = files.get(i);
                    // Use replaceAll with regular expression to remove "[no video]"
                    String filename = file.getName().replaceAll("\\s*\\[no video\\]", "");
                    Path destPath = new File(beatmapDir, filename).toPath();

                    if(!file.getName().endsWith(".osz")) continue;

                    try {
                        Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied: " + filename);

                        //extract .osz and store in temp folder
                        String filePath = String.format("./src/main/resources/assets/beatmap/%s", filename);
                        File oszFile = new File(filePath);
                        File outputDir = new File("./src/main/resources/assets/temp");
                        OszExtractor.extractOsz(oszFile, outputDir);
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
                        File songFile = new File("./src/main/resources/assets/temp/audio.mp3");
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
                                        OsuParser.parse(f);

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
//                progressBar.setVisible(false);

                // add file labels
                for (File file : files) {
                    if(!file.getName().endsWith(".osz")) continue;
                    Label fileLabel = new Label("• " + file.getName());
                    uploadedFile.getChildren().add(fileLabel);
                }
            }

            @Override
            protected void failed() {
                dropText.setText("Upload failed, beatmap already exists");
//                progressBar.setVisible(false);
            }
        };

//        progressBar.progressProperty().bind(uploadTask.progressProperty());
        new Thread(uploadTask).start();
    }

    public UploadPage(Stage stage) {
        super(stage);
        handleEvent();
    }
}
