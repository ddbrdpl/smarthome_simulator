package cz.cvut.fel.omo.smarthome.simulation;

import java.util.Random;

public class WeatherService {

    private static final int CHANGE_EVERY_TICKS = 8; // ~2 hours

    private final Random random = new Random();
    private Weather current    = Weather.SUNNY;
    private int     ticksLeft  = CHANGE_EVERY_TICKS;

    public WeatherService() {
        this.current = Weather.random(random);
    }

    // Call once per simulation tick
    public void tick() {
        ticksLeft--;
        if (ticksLeft <= 0) {
            Weather prev = current;
            current   = Weather.random(random);
            ticksLeft = CHANGE_EVERY_TICKS;
            if (current != prev) {
                System.out.println(" [WEATHER] Changed: " + prev.getDisplayName()
                        + " → " + current.getDisplayName());
            }
        }
    }

    public Weather getCurrent() { return current; }

    public boolean isOutdoorFriendly() { return current.isOutdoorFriendly(); }
}