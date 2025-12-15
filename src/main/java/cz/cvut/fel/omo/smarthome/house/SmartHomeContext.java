package cz.cvut.fel.omo.smarthome.house;

public class SmartHomeContext {

    private static SmartHomeContext instance;

    private SmartHomeContext() {
        // init later
    }

    public static SmartHomeContext getInstance() {
        if (instance == null) {
            instance = new SmartHomeContext();
        }
        return instance;
    }
}
