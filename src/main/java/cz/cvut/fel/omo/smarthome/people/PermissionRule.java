package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.DeviceType;

import java.util.EnumSet;
import java.util.Set;

/**
 * One permission rule that allows a specific {@link Role}
 * to perform a set of {@link DeviceAction actions} on a given {@link DeviceType}.
 *
 * <p>This class is used inside {@link PermissionSet}.</p>
 */
public class PermissionRule {

    /** Role to which this rule applies. */
    private final Role role;

    /** Device type to which this rule applies. */
    private final DeviceType deviceType;

    /** Allowed actions for that role-device combination. */
    private final Set<DeviceAction> actions;

    /**
     * Creates a new permission rule.
     *
     * @param role       role that will be checked
     * @param deviceType device type the rule applies to
     * @param actions    allowed actions (copied into an {@link EnumSet})
     */
    public PermissionRule(Role role, DeviceType deviceType, Set<DeviceAction> actions) {
        this.role = role;
        this.deviceType = deviceType;
        this.actions = EnumSet.copyOf(actions);
    }

    /** @return role for this rule */
    public Role getRole() { return role; }

    /** @return device type for this rule */
    public DeviceType getDeviceType() { return deviceType; }

    /**
     * Returns the allowed actions.
     *
     * <p>Note: returned set is the internal {@link EnumSet}.</p>
     *
     * @return allowed actions
     */
    public Set<DeviceAction> getActions() { return actions; }
}
