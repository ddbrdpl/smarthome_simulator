package cz.cvut.fel.omo.smarthome.devices;

/**
 * Enumeration of all supported smart home device types.
 *
 * <p>This enum defines the category of each {@link Device} in the system.
 * Device types are used for:</p>
 * <ul>
 *   <li>permission checks</li>
 *   <li>automatic device purchasing</li>
 *   <li>assignment of consumption profiles</li>
 *   <li>logical grouping of devices</li>
 * </ul>
 *
 * <p>The enum covers sensors, actuators, multimedia devices,
 * appliances, and infrastructure components.</p>
 */
public enum DeviceType {

    SMART_LIGHT,
    GROUP_LIGHT,
    SMART_BLINDS,
    GARDEN_LIGHT,

    SMART_LOCK,
    MOTION_SENSOR,
    DOOR_WINDOW_SENSOR,
    SMOKE_GAS_SENSOR,
    WATER_LEAK_SENSOR,
    OUTDOOR_CAMERA,

    THERMOSTAT,
    HUMIDIFIER_AC,
    AIR_QUALITY_SENSOR,

    SMART_COFFEE_MACHINE,
    SMART_WASHING_MACHINE,

    SMART_TV,
    MULTIROOM_AUDIO,
    SMART_MIRROR,

    IRRIGATION_SYSTEM,
    PET_FEEDER
}
