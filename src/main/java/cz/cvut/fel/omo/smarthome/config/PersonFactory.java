package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.people.DeviceAction;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PersonFactory {

    public Person createPerson(PersonDefinition def, Room location) {
        // 1. Build Permissions based on Role
        PermissionSet ps = buildPermissions(def.role);

        // 2. Create Specific Class based on Role (Polymorphism)
        // This ensures Father has repair logic, Son has TV logic, etc.
        Person person = switch (def.role) {
            case FATHER -> new Father(def.id, def.name, def.role, location, ps);
            case SON -> new Son(def.id, def.name, def.role, location, ps);
            default -> new StandardPerson(def.id, def.name, def.role, location, ps);
        };

        // 3. Assign Desires (Wishlist for AutoBuyer)
        // These are the items specific roles will ask to buy if missing
        List<DeviceType> desires = new ArrayList<>();

        if (def.role == Role.SON) {
            desires.add(DeviceType.SMART_TV);
            desires.add(DeviceType.MULTIROOM_AUDIO);
        } else if (def.role == Role.DAUGHTER) {
            desires.add(DeviceType.SMART_MIRROR);
            desires.add(DeviceType.SMART_BLINDS);
        } else if (def.role == Role.MOTHER) {
            desires.add(DeviceType.SMART_COFFEE_MACHINE);
        } else if (def.role == Role.GRANDFATHER) {
            desires.add(DeviceType.THERMOSTAT);
        }

        person.setDesires(desires);
        return person;
    }

    /**
     * Define detailed permissions for each role using DeviceAction enum.
     */
    private PermissionSet buildPermissions(Role role) {
        PermissionSet ps = new PermissionSet();

        if (role == Role.CAT) return ps; // Cat has no power here

        // Father has full access to everything
        if (role == Role.FATHER) {
            for (DeviceType t : DeviceType.values()) {
                ps.addRule(new PermissionRule(role, t, EnumSet.allOf(DeviceAction.class)));
            }
            return ps;
        }

        // Mother: almost everything
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