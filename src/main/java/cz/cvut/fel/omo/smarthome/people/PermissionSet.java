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
    }public boolean canControl(Role role, DeviceType type) {
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
