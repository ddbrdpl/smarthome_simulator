package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

public abstract class Device {
    private final String id;
    private final String name;
    private final DeviceType type;
    private final Room location;

    protected Device(String id, String name, DeviceType type, Room location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public DeviceType getType() { return type; }
    public Room getLocation() { return location; }
}
