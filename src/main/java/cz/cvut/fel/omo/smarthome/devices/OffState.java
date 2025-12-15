package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.devices.Device;

public class OffState implements DeviceState {

    @Override
    public void turnOn(Device device) {
        device.setState(new OnState());
    }

    @Override
    public void turnOff(Device device) {
        // already off -> nothing
    }

    @Override
    public void tick(Device device) {
        // no activity when off
    }

    @Override
    public String getName() {
        return "OFF";
    }
}
