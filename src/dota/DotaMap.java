package dota;

import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.awt.geom.Point2D;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class DotaMap {
    private static final Integer map_w = 16384;
    private static final Integer map_h = 16384;
    private static final float map_x_min = -8507.4f;
    private static final float map_x_max = 9515;
    private static final float map_y_min = 8888.12001679f;
    private static final float map_y_max = -8953.45782627f;
    private Integer zoom_level = 4;
    public static Integer gameStartTime = 0;
    public Integer filterType = 0;
    public List<Boolean> playerFilter = new ArrayList<>(Arrays.asList(true, true, true, true, true, true, true, true, true, true));
    public static HashMap<Integer, Long> slot_to_steamid = new HashMap<>();
    public static HashMap<Long, Integer> steamid_to_slot = new HashMap<>();
    public static HashMap<Long, String> steamid_to_playerName = new HashMap<>();

    final ObservableList<Entry> wardsView = FXCollections.observableArrayList();
    private List<Entry> wards = null;

    public void init(Parse parser) {
        wards = parser.wards;
        setWards(wards);
        slot_to_steamid = parser.slot_to_steamid;
        steamid_to_slot = parser.steamid_to_slot;
        steamid_to_playerName = parser.steamid_to_playerName;
    }

    public static String getPlayerName(Integer slot) {
        return steamid_to_playerName.get(slot_to_steamid.get(slot));
    }

    private void setWards(List<Entry> c) {
        wardsView.clear();
        wardsView.addAll(c);
    }

    public List<Entry> getWards() {
        return wards;
    }

    public static List<Entry> filterObserver(List<Entry> wards) {
        return wards.stream().filter(o -> !o.isObserver()).collect(Collectors.toList());
    }

    public static List<Entry> filterSentry(List<Entry> wards) {
        return wards.stream().filter(o -> !o.isSentry()).collect(Collectors.toList());
    }

    public static List<Entry> filterTimeGreaterThan(List<Entry> wards, float t) {
        return wards.stream().filter(o -> (float) o.time <= t && (o.expireTime == null || (float) o.expireTime > t)).collect(Collectors.toList());
    }

    public static List<Entry> filterPlayers(List<Entry> wards, List<Boolean> playerFilter) {
        List<Entry> list = wards.stream().filter(o -> playerFilter.get(o.slot)).collect(Collectors.toList());
        return list;
    }

    public List<Entry> filter(boolean filterTimeGreaterThan, float t) {
        List<Entry> c = getWards();
        if (filterTimeGreaterThan) {
            c = filterTimeGreaterThan(c, t);
        }
        c = filterPlayers(c, playerFilter);
        switch (filterType) {
            case 0:
                return c;
            case 1:
                return filterSentry(c);
            case 2:
                return filterObserver(c);
            default:
                return c;
        }
    }

    public void setListView(boolean filterTimeGreaterThan, float t) {
        wardsView.clear();
        if (wards != null) {
            wardsView.addAll(filter(filterTimeGreaterThan, t));
        }
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
        circle.setStroke(ward.getTeamColor());
        circle.setStrokeWidth(3);
        mapRegion.getChildren().add(circle);
    }
}
