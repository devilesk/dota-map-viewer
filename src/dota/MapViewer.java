package dota;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;

import javafx.application.Platform;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.*;

public class MapViewer {

    @FXML
    private CheckBox cbRadiant;

    @FXML
    private CheckBox cbPlayer0;

    @FXML
    private CheckBox cbPlayer1;

    @FXML
    private CheckBox cbPlayer2;

    @FXML
    private CheckBox cbPlayer3;

    @FXML
    private CheckBox cbPlayer4;

    @FXML
    private CheckBox cbDire;

    @FXML
    private CheckBox cbPlayer5;

    @FXML
    private CheckBox cbPlayer6;

    @FXML
    private CheckBox cbPlayer7;

    @FXML
    private CheckBox cbPlayer8;

    @FXML
    private CheckBox cbPlayer9;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ChoiceBox<String> wardTypeFilter;

    @FXML
    private AnchorPane mapRegion;

    @FXML
    private ImageView mapBackground;

    @FXML
    private TableView<Entry> tableView;

    @FXML
    private TableColumn<Entry, String> colTime;

    @FXML
    private TableColumn<Entry, String> colPlayer;

    @FXML
    private TableColumn<Entry, String> colType;

    @FXML
    private Slider slider;

    @FXML
    private Label sliderLabel;

    List<CheckBox> cbPlayers;
    Stage primaryStage = null;
    private final DotaMap map = new DotaMap();
    final FileChooser fileChooser = new FileChooser();

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

    Parse parser;

    private void runTask(File file) {
        Task longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                InputStream stream = readFile(file);
                //updateProgress(1, 2);
                parser = new Parse(stream);
                stream.close();
                System.err.println("done.");
                //updateProgress(2, 2);
                return null;
            }
        };

        longTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.err.println("failed!");
            }
        });

        longTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                clearMap();
                DotaMap.gameStartTime = parser.gameStartTime;
                map.init(parser);
                map.playerFilter = getCheckedPlayers();
                map.render(mapRegion);
                slider.setMax(parser.gameEndTime);
                initPlayerCheckBoxes();
                System.err.println("done!");
                progressBar.setProgress(1);
            }
        });
        //progressBar.progressProperty().bind(longTask.progressProperty());
        new Thread(longTask).start();
        progressBar.setProgress(-1);
    }

    private void openReplay(File file) {
        try {
            runTask(file);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void initPlayerCheckBoxes() {
        for (int i = 0; i < 10; i++) {
            cbPlayers.get(i).setText(DotaMap.getPlayerName(i));
        }
    }

    private void clearMap() {
        mapRegion.getChildren().clear();
        mapRegion.getChildren().add(mapBackground);
    }

    public void handleOpen(ActionEvent actionEvent) {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            openReplay(file);
        }
    }

    public void handleQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void handleCheckBox(ActionEvent actionEvent) {
        CheckBox cb = (CheckBox)actionEvent.getSource();
        String id = cb.getId();
        if (id.equals("cbRadiant")) {
            cbPlayer0.setSelected(cb.isSelected());
            cbPlayer1.setSelected(cb.isSelected());
            cbPlayer2.setSelected(cb.isSelected());
            cbPlayer3.setSelected(cb.isSelected());
            cbPlayer4.setSelected(cb.isSelected());
        }
        else if (id.equals("cbDire")) {
            cbPlayer5.setSelected(cb.isSelected());
            cbPlayer6.setSelected(cb.isSelected());
            cbPlayer7.setSelected(cb.isSelected());
            cbPlayer8.setSelected(cb.isSelected());
            cbPlayer9.setSelected(cb.isSelected());
        }
        else if (id.startsWith("cbPlayer")) {
            Integer slot = Integer.parseInt(id.replace("cbPlayer", ""));
            CheckBox cbTeam = cbRadiant;
            if (slot > 4 ) {
                cbTeam = cbDire;
            }
            if (cb.isSelected()) {
                cbTeam.setSelected(true);
            }
            else {
                if (slot > 4 && !anyDireChecked()) {
                    cbTeam.setSelected(false);
                }
                else if (slot <= 4 && !anyRadiantChecked()) {
                    cbTeam.setSelected(false);
                }
            }
        }
        map.playerFilter = getCheckedPlayers();
        clearMap();
        map.setListView(false, 0);
        map.render(mapRegion);
    }

    public boolean anyRadiantChecked() {
        return cbPlayer0.isSelected() || cbPlayer1.isSelected() || cbPlayer2.isSelected() || cbPlayer3.isSelected() || cbPlayer4.isSelected();
    }

    public boolean anyDireChecked() {
        return cbPlayer5.isSelected() || cbPlayer6.isSelected() || cbPlayer7.isSelected() || cbPlayer8.isSelected() || cbPlayer9.isSelected();
    }

    public List<Boolean> getCheckedPlayers() {
        List<Boolean> list = new ArrayList<>();
        for (CheckBox o : cbPlayers) {
            Boolean selected = o.isSelected();
            list.add(selected);
        }
        return list;
    }

    public void setStageAndSetupListeners(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Dota Map Viewer");

        cbPlayers = new ArrayList<>(Arrays.asList(cbPlayer0, cbPlayer1, cbPlayer2, cbPlayer3, cbPlayer4, cbPlayer5, cbPlayer6, cbPlayer7, cbPlayer8, cbPlayer9));

        wardTypeFilter.setItems(FXCollections.observableArrayList("Observer & Sentry", "Observer Only", "Sentry Only"));
        wardTypeFilter.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> {
            map.filterType = new_value.intValue();
            clearMap();
            map.setListView(false, 0);
            map.render(mapRegion);
        });
        wardTypeFilter.getSelectionModel().selectFirst();

        colTime.setCellValueFactory(
                new PropertyValueFactory<>("propTime"));
        colPlayer.setCellValueFactory(
                new PropertyValueFactory<>("propPlayerName"));
        colType.setCellValueFactory(
                new PropertyValueFactory<>("propType"));

        tableView.setItems(map.wardsView);
        //tableView.setCellFactory(listView -> new WardListViewCell());
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> {
                    clearMap();
                    //map.setListView(false, 0);
                    map.render(mapRegion, new_val);
                });

        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            sliderLabel.setText(formatTime(new_val.intValue()));
            clearMap();
            map.setListView(true, new_val.floatValue());
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
    }

    private static String formatTime(Integer intElapsed) {
        long rawSeconds = intElapsed - DotaMap.gameStartTime;
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
}
