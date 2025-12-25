package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

/**
 * Minimal concrete implementation of {@link Device}.
 *
 * <p>This class does not add any additional behavior and exists mainly
 * to allow instantiation of devices defined in configuration files.</p>
 *
 * <p>All behavior is inherited from {@link Device} and controlled via
 * {@link DeviceState} implementations.</p>
 */
public class GenericDevice extends Device {

    /**
     * Creates a generic smart home device.
     *
     * @param id unique device identifier
     * @param name human-readable device name
     * @param type device type
     * @param location room where the device is installed
     */
    public GenericDevice(String id, String name, DeviceType type, Room location) {
        super(id, name, type, location);
    }
}
