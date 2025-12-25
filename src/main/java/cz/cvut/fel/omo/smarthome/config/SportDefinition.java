package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.sports.SportType;

/**
 * Configuration definition of sport equipment.
 *
 * <p>This class describes non-electronic equipment
 * (e.g. treadmill, bike) used in the smart home simulation.</p>
 */
public class SportDefinition {

    /** Unique identifier of the sport equipment. */
    public String id;

    /** Type of sport equipment. */
    public SportType type;

    /** Name of the room where the equipment is located. */
    public String room;
}
