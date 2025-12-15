package cz.cvut.fel.omo.smarthome.sports;

import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.Person;

public class SportEquipment {
    private final String id;
    private final SportType type;
    private final Room location;
    private Person inUseBy;

    public SportEquipment(String id, SportType type, Room location) {
        this.id = id;
        this.type = type;
        this.location = location;
    }

    public String getId() { return id; }
    public SportType getType() { return type; }
    public Room getLocation() { return location; }

    public Person getInUseBy() { return inUseBy; }
    public void setInUseBy(Person inUseBy) { this.inUseBy = inUseBy; }
}
