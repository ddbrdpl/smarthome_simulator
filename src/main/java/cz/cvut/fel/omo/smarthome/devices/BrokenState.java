package cz.cvut.fel.omo.smarthome.devices;

public class BrokenState implements DeviceState {
    @Override
    public void turnOn(Device device) {
        // broken -> cannot turn on
    }

    @Override
    public void turnOff(Device device) {
        // broken -> cannot turn off normally
    }

    @Override
    public void tick(Device device) {
        // waiting for repair
    }

    @Override
    public String toString() { return "BROKEN"; }
}