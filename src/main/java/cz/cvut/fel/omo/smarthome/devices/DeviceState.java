package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.devices.Device;

public interface DeviceState {
    void turnOn(Device device);
    void turnOff(Device device);
    void tick(Device device);
    String getName();
}
