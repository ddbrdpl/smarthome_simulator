package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class PermissionSet {
    private final List<PermissionRule> rules = new ArrayList<>();

    public void addRule(PermissionRule rule) {
        rules.add(rule);
    }

    public boolean canPerform(Person p, Device d, DeviceAction a) {
        DeviceType type = d.getType();
        for (PermissionRule r : rules) {
            if (r.getRole() == p.getRole() && r.getDeviceType() == type && r.getActions().contains(a)) {
                return true;
            }
        }
        return false;
    }
}
