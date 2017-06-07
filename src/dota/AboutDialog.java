package dota;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class AboutDialog {

    @FXML
    private Button closeButton;

    @FXML
    void handleClose(ActionEvent event) {
        Stage stage = (Stage)closeButton.getScene().getWindow();
        stage.close();
    }
}
