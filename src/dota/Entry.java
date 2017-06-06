package dota;

import javafx.scene.paint.Color;
import java.time.LocalTime;

class Entry {
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

    public Entry(Integer time) {
        this.time = time;
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
        return 3;
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

    public String getName() {
        return isObserver() ? "Observer" : "Sentry";
    }

    public boolean isDead() {
        return entityleft;
    }
}