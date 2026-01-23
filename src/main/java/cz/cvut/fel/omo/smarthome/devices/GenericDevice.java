package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

// Simple implementation for devices defined in config.
public class GenericDevice extends Device {
    public GenericDevice(String id, String name, DeviceType type, Room location) {
        super(id, name, type, location);
    }
}