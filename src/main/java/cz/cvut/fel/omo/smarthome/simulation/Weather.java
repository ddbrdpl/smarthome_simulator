package cz.cvut.fel.omo.smarthome.simulation;

import java.util.Random;

public enum Weather {

    SUNNY  ("Sunny",   50),
    CLOUDY ("Cloudy",  30),
    RAINY  ("Rainy",   15),
    COLD   ("Cold",     5);

    private final String displayName;
    private final int weight;

    Weather(String displayName, int weight) {
        this.displayName = displayName;
        this.weight      = weight;
    }

    // Weighted random pick
    public static Weather random(Random rnd) {
        int total = 0;
        for (Weather w : values()) total += w.weight;
        int roll = rnd.nextInt(total);
        int cum  = 0;
        for (Weather w : values()) {
            cum += w.weight;
            if (roll < cum) return w;
        }
        return SUNNY;
    }

    public boolean isOutdoorFriendly() {
        return this == SUNNY || this == CLOUDY;
    }

    public String getDisplayName() { return displayName; }
}