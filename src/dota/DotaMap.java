package dota;

import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.awt.geom.Point2D;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.util.List;
import java.util.stream.Collectors;

class DotaMap {
    public static final String mapImage = "dotamap5_25.jpg";
    private static final Integer map_w = 16384;
    private static final Integer map_h = 16384;
    private static final float map_x_min = -8507.4f;
    private static final float map_x_max = 9515;
    private static final float map_y_min = 8888.12001679f;
    private static final float map_y_max = -8953.45782627f;
    private Integer zoom_level = 4;
    public static Integer gameStartTime = 0;

    final ObservableList<Entry> wardsView = FXCollections.observableArrayList();
    private List<Entry> wards = null;

    public void initWards(List<Entry> c) {
        wards = c;
        setWards(wards);
    }

    private void setWards(List<Entry> c) {
        wardsView.clear();
        wardsView.addAll(c);
    }

    public void filterNone() {
        setWards(wards);
    }

    public void filterObserver() {
        wardsView.clear();
        wardsView.addAll(wards.stream().filter(o -> !o.isObserver()).collect(Collectors.toList()));
    }

    public void filterSentry() {
        wardsView.clear();
        wardsView.addAll(wards.stream().filter(o -> !o.isSentry()).collect(Collectors.toList()));
    }

    public void filterTimeGreaterThan(float t) {
        wardsView.clear();
        wardsView.addAll(wards.stream().filter(o -> (float) o.time <= t && (o.expireTime == null || (float) o.expireTime > t)).collect(Collectors.toList()));
    }

    private static float reverseLerp(float minVal, float maxVal, float pos) {
        return (pos - minVal) / (maxVal - minVal);
    }

    private Point2D worldToLatLon(float x, float y) {
        Point2D pt = new Point2D.Float(
                reverseLerp(map_x_min, map_x_max, x) * map_w / (float)Math.pow(2, zoom_level),
                reverseLerp(map_y_min, map_y_max, y) * map_h / (float)Math.pow(2, zoom_level)
        );
        System.out.printf("(%s, %s), (%s, %s)\n", x, y, pt.getX(), pt.getY());
        return pt;
    }

    private Point2D entryToLatLon(Entry entry) {
        return worldToLatLon(entry.getWorldX(), entry.getWorldY());
    }

    public void render(AnchorPane mapRegion) {
        for (Entry ward : wardsView) {
            render(mapRegion, ward);
        }
    }

    public void render(AnchorPane mapRegion, List<Entry> wards) {
        for (Entry ward : wards) {
            render(mapRegion, ward);
        }
    }

    public void render(AnchorPane mapRegion, Entry ward) {
        Point2D pt = entryToLatLon(ward);
        System.out.printf("slot: %s, type: %s, loc: (%s, %s, %s), vec: (%s, %s), isDead: %s, ehandle: %s, expireTime: %s\n", ward.slot, ward.type, ward.x, ward.y, ward.z, ward.vecX, ward.vecY, ward.isDead(), ward.ehandle, ward.expireTime);

        Circle circle = new Circle(pt.getX(), pt.getY(), ward.getRadius(), ward.getColor());
        mapRegion.getChildren().add(circle);
    }
}
