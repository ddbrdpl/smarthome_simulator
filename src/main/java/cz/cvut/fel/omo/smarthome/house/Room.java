package cz.cvut.fel.omo.smarthome.house;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.people.Animal;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    private final String name;
    private final RoomType type;

    private final List<Device> devices = new ArrayList<>();
    private final List<Person> personsPresent = new ArrayList<>();
    private final List<Animal> animalsPresent = new ArrayList<>();
    private final List<SportEquipment> sportEquipment = new ArrayList<>();

    public Room(String name, RoomType type) {
        this.name = name;
        this.type = type;
    }

    public void addDevice(Device d)         { devices.add(d); }
    public void addPerson(Person p)         { personsPresent.add(p); }
    public void removePerson(Person p)      { personsPresent.remove(p); }
    public void addAnimal(Animal a)         { animalsPresent.add(a); }
    public void removeAnimal(Animal a)      { animalsPresent.remove(a); }
    public void addSportEquipment(SportEquipment s) { sportEquipment.add(s); }

    public List<Device> getDevices()           { return Collections.unmodifiableList(devices); }
    public List<Person> getPersonsPresent()    { return Collections.unmodifiableList(personsPresent); }
    public List<Animal> getAnimalsPresent()    { return Collections.unmodifiableList(animalsPresent); }
    public List<SportEquipment> getSportEquipment() { return Collections.unmodifiableList(sportEquipment); }

    public String getName()   { return name; }
    public RoomType getType() { return type; }
}