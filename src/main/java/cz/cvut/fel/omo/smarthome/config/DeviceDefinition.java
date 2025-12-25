package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.devices.DeviceType;

/**
 * Configuration definition of a smart device.
 *
 * <p>This class defines which device exists in the smart home,
 * its type and the room where it is installed.</p>
 *
 * <p>It is converted into a runtime {@code Device} instance
 * using {@link DeviceFactory}.</p>
 */
public class DeviceDefinition {

    /** Unique identifier of the device. */
    public String id;

    /** Human-readable name of the device. */
    public String name;

    /** Type of the device (e.g. SMART_LIGHT, THERMOSTAT). */
    public DeviceType type;

    /** Name of the room where the device is located. */
    public String room;
}
