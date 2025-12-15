package cz.cvut.fel.omo.smarthome.house;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.*;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.*;

public class SmartHomeContext {

    private static SmartHomeContext instance;

    private final List<Floor> floors = new ArrayList<>();
    private final List<Person> residents = new ArrayList<>();

    private final EventBus eventBus = new EventBus();

    private SmartHomeContext() {}

    public static SmartHomeContext getInstance() {
        if (instance == null) {
            instance = new SmartHomeContext();
        }
        return instance;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public List<Person> getResidents() {
        return Collections.unmodifiableList(residents);
    }

    public void initialize(HomeDefinition def,
                           DeviceFactory deviceFactory,
                           PersonFactory personFactory) {

        floors.clear();
        residents.clear();

        // ---------- FLOOR ----------
        Floor floor = new Floor("Main floor", 0);

        Map<String, Room> roomsByName = new HashMap<>();
        for (RoomDefinition rd : def.rooms) {
            Room room = new Room(rd.name, rd.type);
            roomsByName.put(rd.name, room);
            floor.addRoom(room);
        }

        // ---------- DEVICES ----------
        for (DeviceDefinition dd : def.devices) {
            Room room = requireRoom(roomsByName, dd.room, "Device " + dd.id);
            Device device = deviceFactory.createDevice(dd, room);
            room.addDevice(device);
        }

        // ---------- PERSONS ----------
        for (PersonDefinition pd : def.persons) {
            Room room = requireRoom(roomsByName, pd.room, "Person " + pd.id);
            Person person = personFactory.createPerson(pd, room);
            residents.add(person);
            room.addPerson(person);
        }

        // Subscribe all persons
        for (Person p : residents) {
            eventBus.subscribe(p);
        }

        // ---------- EVENT HANDLER CHAIN ----------
        EventHandler h1 = new FatherHandler();
        EventHandler h2 = new MotherHandler();
        EventHandler h3 = new DaughterHandler();
        EventHandler h4 = new GrandfatherHandler();
        EventHandler h5 = new FallbackHandler();

        h1.setNext(h2);
        h2.setNext(h3);
        h3.setNext(h4);
        h4.setNext(h5);

        eventBus.subscribe(new SystemEventListener(h1));

        // ---------- SPORTS ----------
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
