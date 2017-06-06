package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Paint;
import java.awt.geom.Point2D;

public class Main extends Application {

    final AnchorPane mapRegion = new AnchorPane();

    final Integer map_w = 1024;
    final Integer map_h = 1024;
    final float map_x_min = -8507.4f / 2;
    final float map_x_max = 9515 / 2;
    final float map_y_min = 8888.12001679f / 2;
    final float map_y_max = -8953.45782627f / 2;

    public static void main(String[] args) {
        Application.launch(args);
    }

    public float reverseLerp(float minVal, float maxVal, float pos) {
        return (pos - minVal) / (maxVal - minVal);
    }

    public Point2D worldToLatLon(float x, float y) {
        Point2D pt = new Point2D.Float(
                reverseLerp(map_x_min, map_x_max, x) * map_w,
                reverseLerp(map_y_min, map_y_max, y) * map_h
        );
        System.out.printf("(%s, %s), (%s, %s)\n", x, y, pt.getX(), pt.getY());
        return pt;
    }

    public void openReplay(File file) {
        try {
            InputStream stream = new FileInputStream(file);
            Parse parser = new Parse(stream);
            List<Parse.Entry> wards = parser.wards;
            for (Parse.Entry ward : wards) {
                Point2D pt = worldToLatLon((ward.x) * 64 - 8288, (ward.y) * 64 - 8288);
                System.out.printf("slot: %s, type: %s, loc: (%s, %s, %s)\n", ward.slot, ward.type, ward.x, ward.y, ward.z);

                Circle circle = new Circle();
                circle.setRadius(3);
                circle.setCenterX(pt.getX());
                circle.setCenterY(pt.getY());
                circle.setFill(Paint.valueOf("RED"));

                mapRegion.getChildren().add(circle);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        final FileChooser fileChooser = new FileChooser();
        final Button openButton = new Button("Open a replay...");

        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            openReplay(file);
                        }
                    }
                });

        primaryStage.setTitle("Title");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 330, Color.WHITE);

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);

        final ImageView imv = new ImageView();
        final Image image2 = new Image(Main.class.getResourceAsStream("dotamap5_25.jpg"));
        imv.setImage(image2);

        mapRegion.getChildren().add(imv);
        gridpane.add(mapRegion, 1, 2);

        GridPane.setConstraints(openButton, 0, 0);
        gridpane.add(openButton, 1, 1);


        root.getChildren().add(gridpane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}