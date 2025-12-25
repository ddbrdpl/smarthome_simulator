package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.*;

import java.util.EnumSet;

/**
 * Factory responsible for creating {@link Person} instances from configuration definitions.
 *
 * <p>This class converts {@link PersonDefinition} (loaded from JSON) into runtime {@link Person} objects
 * placed into a starting {@link Room}.</p>
 *
 * <p>During creation it also builds a {@link PermissionSet} describing what actions
 * a role is allowed to perform on specific device types.</p>
 */
public class PersonFactory {

    /**
     * Creates a runtime person instance using configuration definition and initial location.
     *
     * @param def configuration definition of the person (id, name, role, room reference)
     * @param location starting room of the person
     * @return created person with permission set based on role
     */
    public Person createPerson(PersonDefinition def, Room location) {
        PermissionSet ps = buildPermissions(def.role);
        return new Person(def.id, def.name, def.role, location, ps);
    }

    /**
     * Builds permissions for the given role.
     *
     * <p>The permission model is role-based:</p>
     * <ul>
     *   <li>{@link Role#CAT} has no permissions.</li>
     *   <li>{@link Role#FATHER} has full permissions for all device types.</li>
     *   <li>{@link Role#MOTHER} can control most devices, but not repair.</li>
     *   <li>{@link Role#GRANDFATHER} has limited controls (on/off + selected actions).</li>
     *   <li>{@link Role#DAUGHTER} can control common household and media devices.</li>
     *   <li>{@link Role#SON} has limited access (mainly lights and entertainment).</li>
     * </ul>
     *
     * <p>This method returns a new {@link PermissionSet} instance each time it is called.</p>
     *
     * @param role role for which permissions should be built
     * @return permission set describing allowed actions for the role
     */
    private PermissionSet buildPermissions(Role role) {
        PermissionSet ps = new PermissionSet();

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
            ps.addRule(new PermissionRule(role, DeviceType.SMART_LIGHT, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.GROUP_LIGHT, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.SMART_TV, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            ps.addRule(new PermissionRule(role, DeviceType.MULTIROOM_AUDIO, EnumSet.of(DeviceAction.TURN_ON, DeviceAction.TURN_OFF)));
            return ps;
        }

        return ps;
    }
}
