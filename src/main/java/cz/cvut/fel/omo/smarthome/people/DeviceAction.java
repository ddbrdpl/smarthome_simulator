package cz.cvut.fel.omo.smarthome.people;

/**
 * Enumerates possible actions a person may attempt on a device.
 *
 * <p>Not all actions are used by every device. Permissions are defined
 * by pairing {@link Role}, device type, and allowed action set.</p>
 */
public enum DeviceAction {
    TURN_ON,
    TURN_OFF,
    OPEN,
    CLOSE,
    LOCK,
    UNLOCK,
    START_PROGRAM,
    SET_TEMPERATURE,
    WATER_PLANTS,
    DISPENSE_FOOD,
    REPAIR
}
