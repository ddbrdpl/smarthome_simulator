package cz.cvut.fel.omo.smarthome.house;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single floor in the smart home.
 *
 * <p>A floor groups multiple {@link Room} instances and serves as a structural
 * element of the house model. In the current implementation, the simulation
 * typically uses a single floor, but the design supports multiple floors.</p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Store basic metadata about the floor (name, level).</li>
 *   <li>Maintain a list of rooms located on this floor.</li>
 *   <li>Provide read-only access to rooms for external consumers.</li>
 * </ul>
 *
 * <p>The list of rooms can only be modified via {@link #addRoom(Room)},
 * enforcing controlled construction of the house layout.</p>
 */
public class Floor {

    /** Human-readable name of the floor (e.g. "Main floor"). */
    private final String name;

    /** Floor level (e.g. 0 = ground floor, 1 = first floor, etc.). */
    private final int level;

    /** Rooms located on this floor. */
    private final List<Room> rooms = new ArrayList<>();

    /**
     * Creates a new floor.
     *
     * @param name  display name of the floor
     * @param level numeric level of the floor
     */
    public Floor(String name, int level) {
        this.name = name;
        this.level = level;
    }

    /**
     * @return name of the floor
     */
    public String getName() {
        return name;
    }

    /**
     * @return numeric level of the floor
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns an immutable view of rooms on this floor.
     *
     * <p>This prevents external code from modifying the internal room list.</p>
     *
     * @return unmodifiable list of rooms
     */
    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    /**
     * Adds a room to this floor.
     *
     * <p>This method is typically used during initialization of the
     * {@link cz.cvut.fel.omo.smarthome.house.SmartHomeContext}.</p>
     *
     * @param room room to be added
     */
    public void addRoom(Room room) {
        rooms.add(room);
    }
}
