package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.house.RoomType;

/**
 * Configuration definition of a single room.
 *
 * <p>This class describes a room before runtime initialization.
 * It is later converted into a {@code Room} object
 * inside {@link cz.cvut.fel.omo.smarthome.house.SmartHomeContext}.</p>
 */
public class RoomDefinition {

    /** Name of the room (must be unique). */
    public String name;

    /** Type of the room (e.g. KITCHEN, BATHROOM, LIVING_ROOM). */
    public RoomType type;
}
