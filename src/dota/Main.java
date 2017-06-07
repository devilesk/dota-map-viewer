package dota;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MapViewer.fxml"));
        Parent root = loader.load();
        MapViewer controller = loader.getController();
        controller.setStageAndSetupListeners(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}