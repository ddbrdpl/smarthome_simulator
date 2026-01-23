package cz.cvut.fel.omo.smarthome.house;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.*;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;
import cz.cvut.fel.omo.smarthome.logs.EventLog;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.shop.AutoBuyer;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.time.LocalDateTime;
import java.util.*;

// Singleton Context: Holds the state of the entire simulation.
public class SmartHomeContext {

    private static SmartHomeContext instance;

    // Data Holders
    private final List<Floor> floors = new ArrayList<>();
    private final List<Person> residents = new ArrayList<>();

    private LocalDateTime currentSimulationTime = LocalDateTime.of(2026, 1, 1, 8, 0);

    // Services
    private final EventBus eventBus = new EventBus();
    private final EventLog eventLog = new EventLog();
    private final ActivityLog activityLog = new ActivityLog();
    private final ConsumptionLog consumptionLog = new ConsumptionLog();
    private final AutoBuyer autoBuyer = new AutoBuyer();

    private SmartHomeContext() {}

    public static synchronized SmartHomeContext getInstance() {
        if (instance == null) instance = new SmartHomeContext();
        return instance;
    }

    public void initialize(HomeDefinition def, DeviceFactory devFactory, PersonFactory personFactory) {
        // Reset state
        floors.clear();
        residents.clear();
        eventLog.clear();
        activityLog.clear();
        consumptionLog.clear();

        // 1. Build House Structure
        Floor mainFloor = new Floor("Main floor", 0);
        Map<String, Room> roomMap = new HashMap<>();

        for (RoomDefinition rd : def.rooms) {
            Room r = new Room(rd.name, rd.type);
            roomMap.put(rd.name, r);
            mainFloor.addRoom(r);
        }
        floors.add(mainFloor);

        // 2. Add Devices
        for (DeviceDefinition dd : def.devices) {
            Room room = requireRoom(roomMap, dd.room);
            Device device = devFactory.createDevice(dd, room);
            device.connectEventBus(eventBus);
            room.addDevice(device);
        }

        // 3. Add People
        for (PersonDefinition pd : def.persons) {
            Room room = requireRoom(roomMap, pd.room);
            Person person = personFactory.createPerson(pd, room);
            residents.add(person);
            room.addPerson(person);
            eventBus.subscribe(person); // People listen to events
        }

        // 4. Add Sport Equipment
        for (SportDefinition sd : def.sports) {
            Room room = requireRoom(roomMap, sd.room);
            room.addSportEquipment(new SportEquipment(sd.id, sd.type, room));
        }

        // 5. Setup Event System (Chain of Responsibility)
        EventHandler chain = buildHandlerChain();
        eventBus.subscribe(new SystemEventListener(chain, this));
    }

    private EventHandler buildHandlerChain() {
        EventHandler father = new FatherHandler();
        EventHandler mother = new MotherHandler();
        EventHandler daughter = new DaughterHandler();
        EventHandler grandfather = new GrandfatherHandler();
        EventHandler fallback = new FallbackHandler();

        father.setNext(mother);
        mother.setNext(daughter);
        daughter.setNext(grandfather);
        grandfather.setNext(fallback);

        return father;
    }

    private Room requireRoom(Map<String, Room> map, String name) {
        Room r = map.get(name);
        if (r == null) throw new IllegalStateException("Config references unknown room: " + name);
        return r;
    }

    public LocalDateTime getCurrentTime() {
        return currentSimulationTime;
    }
    public void advanceTime(int minutes) {
        this.currentSimulationTime = currentSimulationTime.plusMinutes(minutes);
    }

    // Getters
    public List<Floor> getFloors() { return Collections.unmodifiableList(floors); }
    public List<Person> getResidents() { return Collections.unmodifiableList(residents); }
    public EventBus getEventBus() { return eventBus; }
    public EventLog getEventLog() { return eventLog; }
    public ActivityLog getActivityLog() { return activityLog; }
    public ConsumptionLog getConsumptionLog() { return consumptionLog; }
    public AutoBuyer getAutoBuyer() { return autoBuyer; }
}