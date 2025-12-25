package cz.cvut.fel.omo.smarthome.house;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single room inside the smart home.
 *
 * <p>A room is a central aggregation unit that connects:</p>
 * <ul>
 *   <li>{@link Device} instances (smart devices installed in the room),</li>
 *   <li>{@link Person} instances (people currently present in the room),</li>
 *   <li>{@link SportEquipment} instances (sport equipment available in the room).</li>
 * </ul>
 *
 * <p>The room also has a {@link RoomType} which defines its purpose
 * (e.g. kitchen, bathroom, living room) and is used for:</p>
 * <ul>
 *   <li>permission checks,</li>
 *   <li>automatic device placement,</li>
 *   <li>context-aware behavior (e.g. auto-buy logic).</li>
 * </ul>
 *
 * <p>All internal collections are exposed only as unmodifiable views
 * to preserve consistency of the simulation state.</p>
 */
public class Room {

    /** Human-readable name of the room (e.g. "Kitchen"). */
    private final String name;

    /** Functional type of the room (kitchen, bathroom, etc.). */
    private final RoomType type;

    /** Devices installed in this room. */
    private final List<Device> devices = new ArrayList<>();

    /** People currently present in this room. */
    private final List<Person> personsPresent = new ArrayList<>();

    /** Sport equipment available in this room. */
    private final List<SportEquipment> sportEquipment = new ArrayList<>();

    /**
     * Creates a new room.
     *
     * @param name human-readable room name
     * @param type functional room type
     */
    public Room(String name, RoomType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return name of the room
     */
    public String getName() {
        return name;
    }

    /**
     * @return functional type of the room
     */
    public RoomType getType() {
        return type;
    }

    /**
     * Returns an immutable list of devices installed in the room.
     *
     * @return unmodifiable list of devices
     */
    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    /**
     * Returns an immutable list of people currently present in the room.
     *
     * @return unmodifiable list of persons
     */
    public List<Person> getPersonsPresent() {
        return Collections.unmodifiableList(personsPresent);
    }

    /**
     * Returns an immutable list of sport equipment available in the room.
     *
     * @return unmodifiable list of sport equipment
     */
    public List<SportEquipment> getSportEquipment() {
        return Collections.unmodifiableList(sportEquipment);
    }

    /**
     * Adds a device to this room.
     *
     * <p>This method is typically used during initialization or
     * automatic device purchase.</p>
     *
     * @param d device to add
     */
    public void addDevice(Device d) {
        devices.add(d);
    }

    /**
     * Registers a person as present in this room.
     *
     * <p>Used when people move between rooms during the simulation.</p>
     *
     * @param p person entering the room
     */
    public void addPerson(Person p) {
        personsPresent.add(p);
    }

    /**
     * Adds sport equipment to this room.
     *
     * <p>Sport equipment is static and typically defined during
     * configuration loading.</p>
     *
     * @param s sport equipment to add
     */
    public void addSportEquipment(SportEquipment s) {
        sportEquipment.add(s);
    }
}
