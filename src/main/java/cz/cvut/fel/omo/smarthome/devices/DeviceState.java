package cz.cvut.fel.omo.smarthome.devices;

public interface DeviceState {
    void turnOn(Device device);
    void turnOff(Device device);
    void tick(Device device);
}