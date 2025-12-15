package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.*;

import java.util.EnumSet;

public class PersonFactory {

    public Person createPerson(PersonDefinition def, Room location) {
        PermissionSet ps = buildPermissions(def.role);
        return new Person(def.id, def.name, def.role, location, ps);
    }

    private PermissionSet buildPermissions(Role role) {
        PermissionSet ps = new PermissionSet();

        // English comments only.
        // Default rules: CAT has none, FATHER has almost all, others limited.

        if (role == Role.CAT) {
            return ps;
        }

        if (role == Role.FATHER) {
            for (DeviceType t : DeviceType.values()) {
                ps.addRule(new PermissionRule(role, t, EnumSet.allOf(DeviceAction.class)));
            }
            return ps;
        }

        if (role == Role.GRANDFATHER) {
            for (DeviceType t : DeviceType.values()) {
                ps.addRule(new PermissionRule(role, t, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            }
            ps.addRule(new PermissionRule(role, DeviceType.SMART_LOCK, EnumSet.of(DeviceAction.LOCK, DeviceAction.UNLOCK)));
            ps.addRule(new PermissionRule(role, DeviceType.THERMOSTAT, EnumSet.of(DeviceAction.SET_TEMPERATURE)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_WASHING_MACHINE, EnumSet.of(DeviceAction.START_PROGRAM)));
            return ps;
        }

        if (role == Role.MOTHER) {
            // Most devices except REPAIR.
            for (DeviceType t : DeviceType.values()) {
                ps.addRule(new PermissionRule(role, t, EnumSet.of(
                        DeviceAction.TURN_ON, DeviceAction.TURN_OFF,
                        DeviceAction.OPEN, DeviceAction.CLOSE,
                        DeviceAction.LOCK, DeviceAction.UNLOCK,
                        DeviceAction.START_PROGRAM,
                        DeviceAction.SET_TEMPERATURE,
                        DeviceAction.WATER_PLANTS,
                        DeviceAction.DISPENSE_FOOD
                )));
            }
            return ps;
        }

        if (role == Role.DAUGHTER) {
            // Common devices, media, blinds, coffee, washer.
            ps.addRule(new PermissionRule(role, DeviceType.SMART_LIGHT, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.GROUP_LIGHT, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_BLINDS, EnumSet.of(DeviceAction.OPEN, DeviceAction.CLOSE)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_TV, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.MULTIROOM_AUDIO, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_MIRROR, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_COFFEE_MACHINE, EnumSet.of(DeviceAction.START_PROGRAM)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_WASHING_MACHINE, EnumSet.of(DeviceAction.START_PROGRAM)));
            return ps;
        }

        if (role == Role.SON) {
            // Limited: lights + TV + audio.
            ps.addRule(new PermissionRule(role, DeviceType.SMART_LIGHT, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.GROUP_LIGHT, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_TV, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.MULTIROOM_AUDIO, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            return ps;
        }

        return ps;
    }
}
