package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of permission rules used to authorize person-device actions.
 *
 * <p>There are two authorization styles present in this class:</p>
 * <ul>
 *   <li>{@link #canPerform(Person, Device, DeviceAction)} — strict rule-based check using stored {@link PermissionRule}s</li>
 *   <li>{@link #canControl(Role, DeviceType)} — simplified policy-based check (legacy/alternative)</li>
 * </ul>
 *
 * <p>In the current simulation, {@code Person.performStep()} uses {@link #canPerform(Person, Device, DeviceAction)}.</p>
 */
public class PermissionSet {

    /** Stored permission rules. */
    private final List<PermissionRule> rules = new ArrayList<>();

    /**
     * Adds a new permission rule to this set.
     *
     * @param rule rule to add
     */
    public void addRule(PermissionRule rule) {
        rules.add(rule);
    }

    /**
     * Checks if a person is allowed to perform an action on a device,
     * based strictly on the stored {@link PermissionRule}s.
     *
     * @param p person attempting the action
     * @param d target device
     * @param a requested action
     * @return {@code true} if there is a matching rule allowing the action, otherwise {@code false}
     */
    public boolean canPerform(Person p, Device d, DeviceAction a) {
        DeviceType type = d.getType();
        for (PermissionRule r : rules) {
            if (r.getRole() == p.getRole() && r.getDeviceType() == type && r.getActions().contains(a)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Alternative, simplified permission policy.
     *
     * <p>This method is not used by {@code Person.performStep()} in the provided code,
     * but can be useful for quick checks or older logic.</p>
     *
     * @param role resident role
     * @param type device type
     * @return {@code true} if the role can control that device type
     */
    public boolean canControl(Role role, DeviceType type) {
        // CAT: no control
        if (role == Role.CAT) return false;

        // SON (<18): no locks, no washing machine
        if (role == Role.SON) {
            if (type == DeviceType.SMART_LOCK) return false;
            if (type == DeviceType.SMART_WASHING_MACHINE) return false;
            return true;
        }

        if (role == Role.DAUGHTER) {
            return type != DeviceType.SMOKE_GAS_SENSOR && type != DeviceType.DOOR_WINDOW_SENSOR;
        }

        if (role == Role.GRANDFATHER) {
            return true;
        }

        // FATHER / MOTHER default: full control
        return true;
    }
}
