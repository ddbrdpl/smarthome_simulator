package cz.cvut.fel.omo.smarthome.house;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.*;

public class SmartHomeContext {

    private static SmartHomeContext instance;

    private final List<Floor> floors = new ArrayList<>();
    private final List<Person> residents = new ArrayList<>();

    private final cz.cvut.fel.omo.smarthome.events.EventBus eventBus = new cz.cvut.fel.omo.smarthome.events.EventBus();
    public cz.cvut.fel.omo.smarthome.events.EventBus getEventBus() { return eventBus; }


    private SmartHomeContext() {}

    public static SmartHomeContext getInstance() {
        if (instance == null) {
            instance = new SmartHomeContext();
        }
        return instance;
    }

    public List<Floor> getFloors() { return Collections.unmodifiableList(floors); }
    public List<Person> getResidents() { return Collections.unmodifiableList(residents); }

    public void initialize(HomeDefinition def, DeviceFactory deviceFactory, PersonFactory personFactory) {
        floors.clear();
        residents.clear();

        // One-floor setup (can be extended later).
        Floor floor = new Floor("Main floor", 0);

        Map<String, Room> roomsByName = new HashMap<>();
        for (RoomDefinition rd : def.rooms) {
            Room room = new Room(rd.name, rd.type);
            roomsByName.put(rd.name, room);
            floor.addRoom(room);
        }

        // Devices
        for (DeviceDefinition dd : def.devices) {
            Room room = requireRoom(roomsByName, dd.room, "Device " + dd.id);
            Device device = deviceFactory.createDevice(dd, room);
            room.addDevice(device);
        }

        // Persons
        for (PersonDefinition pd : def.persons) {
            Room room = requireRoom(roomsByName, pd.room, "Person " + pd.id);
            Person person = personFactory.createPerson(pd, room);
            residents.add(person);
            room.addPerson(person);
        }

        // Sports
        for (SportDefinition sd : def.sports) {
            Room room = requireRoom(roomsByName, sd.room, "Sport " + sd.id);
            SportEquipment se = new SportEquipment(sd.id, sd.type, room);
            room.addSportEquipment(se);
        }

        floors.add(floor);
    }

    private Room requireRoom(Map<String, Room> roomsByName, String name, String owner) {
        Room room = roomsByName.get(name);
        if (room == null) {
            throw new IllegalStateException(owner + " references unknown room: " + name);
        }
        return room;
    }
}
