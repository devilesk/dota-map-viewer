package dota;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private final AnchorPane mapRegion = new AnchorPane();
    private final DotaMap map = new DotaMap();
    private final ImageView mapBackground = new ImageView();
    private final Slider slider = new Slider();

    public static void main(String[] args) {
        Application.launch(args);
    }

    private InputStream readFile(File file) throws IOException {
        try {
            InputStream stream = new FileInputStream(file);
            BufferedInputStream bstream = new BufferedInputStream(stream);
            return new BZip2CompressorInputStream(bstream);
        }
        catch (IOException e) {
            return new FileInputStream(file);
        }
    }

    private void openReplay(File file) {
        try {
            InputStream stream = readFile(file);
            Parse parser = new Parse(stream);
            stream.close();
            clearMap();
            DotaMap.gameStartTime = parser.gameStartTime;
            map.initWards(parser.wards);
            map.render(mapRegion);
            slider.setMax(parser.gameEndTime);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void clearMap() {
        mapRegion.getChildren().clear();
        mapRegion.getChildren().add(mapBackground);
    }

    @Override
    public void start(Stage primaryStage) {
        final FileChooser fileChooser = new FileChooser();

        final Button openButton = new Button("Open a replay...");
        openButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        openReplay(file);
                    }
                });

        primaryStage.setTitle("Title");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 330, Color.WHITE);

        ScrollPane s1 = new ScrollPane();
        s1.setPrefSize(500, 500);
        s1.setPannable(true);

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);

        final Image im = new Image(Main.class.getResourceAsStream(DotaMap.mapImage));
        mapBackground.setImage(im);
        gridpane.add(s1, 1, 4);

        s1.setContent(mapRegion);

        mapRegion.setPrefSize(1024, 1024);
        mapRegion.getChildren().add(mapBackground);

        GridPane.setConstraints(openButton, 0, 0);
        gridpane.add(openButton, 1, 1);

        GridPane btnContainer = new GridPane();
        btnContainer.setHgap(10);
        gridpane.add(btnContainer, 2, 3);

        Label showLabel = new Label("Show:");
        btnContainer.add(showLabel, 1, 1);

        final Button showAll = new Button("All");
        showAll.setOnAction(
                e -> {
                    clearMap();
                    map.filterNone();
                    map.render(mapRegion);
                });
        GridPane.setConstraints(showAll, 0, 0);
        btnContainer.add(showAll, 2, 1);

        final Button showObserver = new Button("Observer");
        showObserver.setOnAction(
                e -> {
                    clearMap();
                    map.filterSentry();
                    map.render(mapRegion);
                });
        GridPane.setConstraints(showObserver, 0, 0);
        btnContainer.add(showObserver, 3, 1);

        final Button showSentry = new Button("Sentry");
        showSentry.setOnAction(
                e -> {
                    clearMap();
                    map.filterObserver();
                    map.render(mapRegion);
                });
        GridPane.setConstraints(showSentry, 0, 0);
        btnContainer.add(showSentry, 4, 1);

        ListView<Entry> list = new ListView<>();
        list.setItems(map.wardsView);
        list.setCellFactory(listView -> new WardListViewCell());
        list.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> {
                    clearMap();
                    map.render(mapRegion, new_val);
                });
        gridpane.add(list, 2, 4);

        Label sliderLabel = new Label("00:00:00");
        gridpane.add(sliderLabel, 2, 2);

        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(300);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(10);
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            sliderLabel.setText(String.format("%.2f", new_val.floatValue()));
            clearMap();
            map.filterTimeGreaterThan(new_val.floatValue());
            map.render(mapRegion);
        });
        StringConverter<Double> stringConverter = new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                long rawSeconds = object.longValue() - DotaMap.gameStartTime;
                long seconds = Math.abs(rawSeconds);
                long minutes = TimeUnit.SECONDS.toMinutes(seconds);
                long remainingseconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
                if (rawSeconds < 0) {
                    return String.format("-%02d", minutes) + ":" + String.format("%02d", remainingseconds);
                }
                else {
                    return String.format("%02d", minutes) + ":" + String.format("%02d", remainingseconds);
                }
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        };
        slider.setLabelFormatter(stringConverter);
        gridpane.add(slider, 1, 2);

        root.getChildren().add(gridpane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,
                        durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                        elapsedMinutes, elapsedSeconds);
            }
        }
    }
}