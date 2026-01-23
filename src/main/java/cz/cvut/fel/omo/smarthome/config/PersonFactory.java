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

        if (role == Role.CAT) return ps; // Cat has no power here

        // Father has full access
        if (role == Role.FATHER) {
            for (DeviceType t : DeviceType.values()) {
                ps.addRule(new PermissionRule(role, t, EnumSet.allOf(DeviceAction.class)));
            }
            return ps;
        }

        // Mother: almost everything, except repairing (?)
        if (role == Role.MOTHER) {
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

        // Grandfather: Old school, basic controls + thermostat + laundry
        if (role == Role.GRANDFATHER) {
            for (DeviceType t : DeviceType.values()) {
                ps.addRule(new PermissionRule(role, t, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            }
            ps.addRule(new PermissionRule(role, DeviceType.SMART_LOCK, EnumSet.of(DeviceAction.LOCK, DeviceAction.UNLOCK)));
            ps.addRule(new PermissionRule(role, DeviceType.THERMOSTAT, EnumSet.of(DeviceAction.SET_TEMPERATURE)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_WASHING_MACHINE, EnumSet.of(DeviceAction.START_PROGRAM)));
            return ps;
        }

        // Kids: Lights, TV, Music
        if (role == Role.DAUGHTER || role == Role.SON) {
            var commonDevices = EnumSet.of(
                    DeviceType.SMART_LIGHT, DeviceType.GROUP_LIGHT,
                    DeviceType.SMART_TV, DeviceType.MULTIROOM_AUDIO
            );

            for (DeviceType t : commonDevices) {
                ps.addRule(new PermissionRule(role, t, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            }

            if (role == Role.DAUGHTER) {
                ps.addRule(new PermissionRule(role, DeviceType.SMART_BLINDS, EnumSet.of(DeviceAction.OPEN, DeviceAction.CLOSE)));
                ps.addRule(new PermissionRule(role, DeviceType.SMART_MIRROR, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
                ps.addRule(new PermissionRule(role, DeviceType.SMART_COFFEE_MACHINE, EnumSet.of(DeviceAction.START_PROGRAM)));
                ps.addRule(new PermissionRule(role, DeviceType.SMART_WASHING_MACHINE, EnumSet.of(DeviceAction.START_PROGRAM)));
            }
        }

        return ps;
    }
}