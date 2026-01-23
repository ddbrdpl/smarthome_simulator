package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import java.util.EnumSet;
import java.util.Set;

public class PermissionRule {
    private final Role role;
    private final DeviceType deviceType;
    private final Set<DeviceAction> actions;

    public PermissionRule(Role role, DeviceType deviceType, Set<DeviceAction> actions) {
        this.role = role;
        this.deviceType = deviceType;
        this.actions = EnumSet.copyOf(actions);
    }

    public Role getRole() { return role; }
    public DeviceType getDeviceType() { return deviceType; }
    public Set<DeviceAction> getActions() { return actions; }
}