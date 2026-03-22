package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

public abstract class Animal {

    protected final String id;
    protected final String name;
    protected Room location;

    public Animal(String id, String name, Room location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public abstract void performStep(SmartHomeContext ctx);

    protected void moveTo(Room target) {
        if (this.location == target) return;
        this.location.removeAnimal(this);
        this.location = target;
        this.location.addAnimal(this);
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public Room getLocation()   { return location; }
}