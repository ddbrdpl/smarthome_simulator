package cz.cvut.fel.omo.smarthome.sports;

import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.Person;

/**
 * Represents a piece of sport equipment located in a room of the smart home.
 *
 * <p>Sport equipment is not a {@link cz.cvut.fel.omo.smarthome.devices.Device}:
 * it cannot be turned on/off and does not consume energy.
 * Instead, it can be occupied by a {@link Person} for a number of simulation steps.</p>
 *
 * <p>Usage model:</p>
 * <ul>
 *   <li>Only one person may use the equipment at a time</li>
 *   <li>Usage lasts for a fixed number of simulation steps</li>
 *   <li>After the time expires, the equipment becomes free again</li>
 * </ul>
 */
public class SportEquipment {

    /** Unique identifier of the sport equipment (from configuration). */
    private final String id;

    /** Type of sport equipment (e.g., treadmill, bike). */
    private final SportType type;

    /** Room where the equipment is located. */
    private final Room location;

    /** Person currently using the equipment, {@code null} if free. */
    private Person inUseBy;

    /** Number of simulation steps remaining until the equipment is freed. */
    private int busyStepsLeft = 0;

    /**
     * Creates a new sport equipment instance.
     *
     * @param id        unique identifier
     * @param type      sport equipment type
     * @param location  room where the equipment is placed
     */
    public SportEquipment(String id, SportType type, Room location) {
        this.id = id;
        this.type = type;
        this.location = location;
    }

    /** @return unique identifier of the equipment */
    public String getId() { return id; }

    /** @return type of the sport equipment */
    public SportType getType() { return type; }

    /** @return room where the equipment is located */
    public Room getLocation() { return location; }

    /**
     * Returns the person currently using the equipment.
     *
     * @return person using the equipment, or {@code null} if free
     */
    public Person getInUseBy() { return inUseBy; }

    /**
     * Checks whether the equipment is currently free.
     *
     * @return {@code true} if no one is using the equipment, otherwise {@code false}
     */
    public boolean isFree() { return inUseBy == null; }

    /**
     * Attempts to occupy the equipment for a given number of simulation steps.
     *
     * <p>If the equipment is already in use, the call fails.</p>
     *
     * @param p      person who wants to use the equipment
     * @param steps  number of simulation steps the usage should last
     * @return {@code true} if the equipment was successfully occupied,
     *         {@code false} if it was already in use
     */
    public boolean tryUse(Person p, int steps) {
        if (!isFree()) return false;
        this.inUseBy = p;
        this.busyStepsLeft = Math.max(1, steps);
        return true;
    }

    /**
     * Advances the internal state by one simulation step.
     *
     * <p>If the equipment is currently in use, the remaining time is decremented.
     * Once it reaches zero, the equipment becomes free again.</p>
     */
    public void tick() {
        if (inUseBy == null) return;

        busyStepsLeft--;
        if (busyStepsLeft <= 0) {
            inUseBy = null;
            busyStepsLeft = 0;
        }
    }
}
