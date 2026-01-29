package cz.cvut.fel.omo.smarthome.sports;

import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.Person;

public class SportEquipment {

    private final String id;
    private final SportType type;
    private final Room location;

    private Person inUseBy;
    private int busyStepsLeft = 0;

    public SportEquipment(String id, SportType type, Room location) {
        this.id = id;
        this.type = type;
        this.location = location;
    }

    public boolean tryUse(Person p, int steps) {
        if (!isFree()) return false;
        this.inUseBy = p;
        this.busyStepsLeft = Math.max(1, steps);
        return true;
    }

    public void tick() {
        if (inUseBy == null) return;

        busyStepsLeft--;
        if (busyStepsLeft <= 0) {
            inUseBy = null; // Освобождаем тренажер
            busyStepsLeft = 0;
        }
    }

    public boolean isFree() { return inUseBy == null; }

    public String getId() { return id; }
    public SportType getType() { return type; }
    public Room getLocation() { return location; }
    public Person getInUseBy() { return inUseBy; }
}