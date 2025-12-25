package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.people.Role;

/**
 * Configuration definition of a person living in the smart home.
 *
 * <p>This class specifies the identity, role and initial location
 * of a person. During initialization it is transformed
 * into a runtime {@code Person} instance.</p>
 */
public class PersonDefinition {

    /** Unique identifier of the person. */
    public String id;

    /** Display name of the person. */
    public String name;

    /** Role of the person in the smart home (permissions, behavior). */
    public Role role;

    /** Name of the room where the person initially starts. */
    public String room;
}
