package cz.cvut.fel.omo.smarthome.devices;

public class DeviceLifeCycle {

    private double health = 1.0; // 1.0 = 100%, 0.0 = broken

    public double getHealth() {
        return health;
    }

    public void decrease(double delta) {
        health -= delta;
        if (health < 0.0) health = 0.0;
    }

    public boolean isBroken() {
        return health <= 0.0;
    }

    public void repair() {
        health = 1.0;
    }
}
