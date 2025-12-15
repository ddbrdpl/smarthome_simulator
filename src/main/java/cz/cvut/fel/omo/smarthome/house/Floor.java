package cz.cvut.fel.omo.smarthome.house;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Floor {
    private final String name;
    private final int level;
    private final List<Room> rooms = new ArrayList<>();

    public Floor(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }

    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public void addRoom(Room room) { rooms.add(room); }
}
