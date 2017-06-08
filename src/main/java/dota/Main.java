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
        Class c = getClass();
        java.net.URL resource = c.getResource("/MapViewer.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        MapViewer controller = loader.getController();
        controller.setStageAndSetupListeners(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}