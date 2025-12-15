package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.devices.Device;

public class BrokenState implements DeviceState {

    @Override
    public void turnOn(Device device) {
        // broken -> ignore
    }

    @Override
    public void turnOff(Device device) {
        // broken -> ignore
    }

    @Override
    public void tick(Device device) {
        // nothing
    }

    @Override
    public String getName() {
        return "BROKEN";
    }
}
