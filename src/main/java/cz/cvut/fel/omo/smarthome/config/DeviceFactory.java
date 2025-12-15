package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Room;

public class DeviceFactory {

    public Device createDevice(DeviceDefinition def, Room location) {
        // For SW3 step-2 we return a generic device.
        // Later you can replace switch-case and create concrete subclasses per DeviceType.
        return new GenericDevice(def.id, def.name, def.type, location);
    }
}
