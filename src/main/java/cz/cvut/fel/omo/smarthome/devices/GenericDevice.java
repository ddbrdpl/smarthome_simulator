package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

// Minimal concrete implementation to keep SW3 step-2 compilable.
public class GenericDevice extends Device {
    public GenericDevice(String id, String name, DeviceType type, Room location) {
        super(id, name, type, location);
    }
}
