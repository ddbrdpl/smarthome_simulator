package cz.cvut.fel.omo.smarthome.simulation;

import cz.cvut.fel.omo.smarthome.devices.FoodType;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public enum MealTime {

    BREAKFAST("Breakfast", 8, 10,  7, 30),
    LUNCH    ("Lunch",    12, 14, 11, 30),
    DINNER   ("Dinner",   18, 20, 17, 30),
    NONE     ("None",     -1, -1, -1, -1);

    private final String displayName;
    private final int startHour;
    private final int endHour;
    private final int checkHour;   // hour when Father checks fridge
    private final int checkMinute; // minute when Father checks fridge

    MealTime(String displayName, int startHour, int endHour, int checkHour, int checkMinute) {
        this.displayName = displayName;
        this.startHour   = startHour;
        this.endHour     = endHour;
        this.checkHour   = checkHour;
        this.checkMinute = checkMinute;
    }

    // Food cost per meal (for the whole family)
    public Map<FoodType, Integer> getCost() {
        Map<FoodType, Integer> cost = new EnumMap<>(FoodType.class);
        switch (this) {
            case BREAKFAST -> { cost.put(FoodType.EGGS, 2);   cost.put(FoodType.OATMEAL, 1); }
            case LUNCH     -> { cost.put(FoodType.SOUP, 2);  }
            case DINNER    -> { cost.put(FoodType.STEAK, 2); }
            default        -> {}
        }
        return cost;
    }

    // Is it currently mealtime?
    public static MealTime current(LocalDateTime time) {
        int h = time.getHour();
        for (MealTime mt : values()) {
            if (mt == NONE) continue;
            if (h >= mt.startHour && h < mt.endHour) return mt;
        }
        return NONE;
    }

    // Is it time for Father to check the fridge?
    public static MealTime checkTime(LocalDateTime time) {
        int h = time.getHour();
        int m = time.getMinute();
        for (MealTime mt : values()) {
            if (mt == NONE) continue;
            if (h == mt.checkHour && m == mt.checkMinute) return mt;
        }
        return NONE;
    }

    public String getDisplayName() { return displayName; }
    public int getStartHour()      { return startHour; }
}