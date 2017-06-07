package dota;

import javafx.scene.paint.Color;
import java.time.LocalTime;
import javafx.beans.property.SimpleStringProperty;

public class Entry {
    public final Integer time;
    public String type;
    public String key;
    public Integer slot;
    public Integer x;
    public Integer y;
    public Integer z;
    public float vecX;
    public float vecY;
    public Boolean entityleft;
    public Integer ehandle;
    public Integer expireTime;

    private final SimpleStringProperty propTime;
    private final SimpleStringProperty propType;
    private final SimpleStringProperty propPlayerName;

    public Entry(Integer time, String type, Integer slot, String playerName) {
        this.time = time;
        this.type = type;
        this.slot = slot;
        propTime = new SimpleStringProperty(getTime());
        propType = new SimpleStringProperty(getName());
        propPlayerName = new SimpleStringProperty(playerName);
    }

    public String getPropTime() {
        return propTime.get();
    }

    public String getPropType() {
        return propType.get();
    }

    public String getPropPlayerName() {
        return propPlayerName.get();
    }

    public void setPropTime(String s) {
        propTime.set(s);
    }

    public void setPropType(String s) {
        propType.set(s);
    }

    public void setPropPlayerName(String s) {
        propPlayerName.set(s);
    }

    public String getTime() {
        Integer t = time - DotaMap.gameStartTime;
        LocalTime timeOfDay = LocalTime.ofSecondOfDay(Math.abs(t));
        if (t < 0) {
            return "-" + timeOfDay.toString();
        }
        else {
            return timeOfDay.toString();
        }
    }

    public float getWorldX() {
        return x * 128 + vecX - 16384;
    }

    public float getWorldY() {
        return y * 128 + vecY - 16384;
    }

    public Integer getRadius() {
        return 4;
    }

    public boolean isObserver() {
        return type.equals("obs");
    }

    public boolean isSentry() {
        return type.equals("sen");
    }

    public Color getColor() {
        return isObserver() ? Color.YELLOW : Color.BLUE;
    }

    public Color getTeamColor() { return slot > 4 ? Color.web("#FF0000") : Color.web("00FF00"); }

    public String getName() {
        return isObserver() ? "Observer" : "Sentry";
    }

    public boolean isDead() {
        return entityleft;
    }
}