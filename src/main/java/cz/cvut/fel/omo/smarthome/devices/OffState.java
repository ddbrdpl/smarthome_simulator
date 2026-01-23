package cz.cvut.fel.omo.smarthome.devices;

public class OffState implements DeviceState {
    @Override
    public void turnOn(Device device) {
        device.setState(new OnState());
    }

    @Override
    public void turnOff(Device device) {
        // already off, ignore
    }

    @Override
    public void tick(Device device) {
        // idle
    }

    @Override
    public String getName() { return "OFF"; }
}