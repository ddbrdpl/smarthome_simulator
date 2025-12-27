package cz.cvut.fel.omo.smarthome.house;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.*;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;
import cz.cvut.fel.omo.smarthome.logs.EventLog;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.*;

/**
 * Central runtime context (Singleton) for the SmartHome simulation.
 *
 * <p>This class is the main "composition root" of the application. It stores all runtime
 * objects (floors, rooms, people, devices) and shared infrastructure (event bus, logs,
 * consumption tracking and optional bonus components such as auto-buyer).</p>
 *
 * Key responsibilities
 * <ul>
 *   <li>Hold immutable views of the loaded house structure ({@link #getFloors()}, {@link #getResidents()}).</li>
 *   <li>Provide shared services: {@link EventBus}, {@link EventLog}, {@link ActivityLog}, {@link ConsumptionLog}.</li>
 *   <li>Initialize the whole system from {@link HomeDefinition} using factories.</li>
 *   <li>Build and register the event handler chain (Chain of Responsibility) and subscribe listeners.</li>
 * </ul>
 *
 * <p><b>Important:</b> {@link #initialize(HomeDefinition, DeviceFactory, PersonFactory)} clears previous state
 * (floors/residents/logs) so you can re-run simulation from a clean start.</p>
 */
public class SmartHomeContext {

    /** Singleton instance. */
    private static SmartHomeContext instance;

    /** Loaded floors in the house. */
    private final List<Floor> floors = new ArrayList<>();

    /** Residents (people) living in the house. */
    private final List<Person> residents = new ArrayList<>();

    /** Event bus used for publishing and delivering events to listeners. */
    private final EventBus eventBus = new EventBus();

    /** Central event log (stored by {@link SystemEventListener}). */
    private final EventLog eventLog = new EventLog();

    /** Activity log (people actions, system actions like buying devices, etc.). */
    private final ActivityLog activityLog = new ActivityLog();

    /** Aggregated consumption log (electricity/water/gas usage). */
    private final ConsumptionLog consumptionLog = new ConsumptionLog();

    /**
     * Bonus component: automatic buyer for missing devices.
     * Stored here so it can be accessed from other parts (e.g., {@code Person}).
     */
    private final cz.cvut.fel.omo.smarthome.shop.AutoBuyer autoBuyer =
            new cz.cvut.fel.omo.smarthome.shop.AutoBuyer();

    /** @return auto-buyer (bonus feature) */
    public cz.cvut.fel.omo.smarthome.shop.AutoBuyer getAutoBuyer() { return autoBuyer; }

    /** Private constructor for Singleton. */
    private SmartHomeContext() {}

    /**
     * Returns the singleton instance of {@link SmartHomeContext}.
     *
     * @return shared context instance
     */
    public static SmartHomeContext getInstance() {
        if (instance == null) {
            instance = new SmartHomeContext();
        }
        return instance;
    }

    /** @return aggregated consumption log */
    public ConsumptionLog getConsumptionLog() { return consumptionLog; }

    /** @return shared event bus */
    public EventBus getEventBus() { return eventBus; }

    /** @return event log */
    public EventLog getEventLog() { return eventLog; }

    /** @return activity log */
    public ActivityLog getActivityLog() { return activityLog; }

    /**
     * Returns an immutable view of floors list.
     *
     * @return unmodifiable list of floors
     */
    public List<Floor> getFloors() { return Collections.unmodifiableList(floors); }

    /**
     * Returns an immutable view of residents list.
     *
     * @return unmodifiable list of residents
     */
    public List<Person> getResidents() { return Collections.unmodifiableList(residents); }

    /**
     * Initializes the smart home runtime model from configuration.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Clear previous runtime state and logs.</li>
     *   <li>Create a floor and rooms based on {@link RoomDefinition}.</li>
     *   <li>Create devices using {@link DeviceFactory}, connect them to {@link EventBus}, add to rooms.</li>
     *   <li>Create people using {@link PersonFactory}, add to residents and rooms.</li>
     *   <li>Subscribe all residents to the {@link EventBus}.</li>
     *   <li>Build handler chain (Father → Mother → Daughter → Grandfather → Fallback).</li>
     *   <li>Subscribe {@link SystemEventListener} (it executes the chain and logs events).</li>
     *   <li>Create sport equipment and add it to rooms.</li>
     *   <li>Add the created floor to context floors.</li>
     * </ol>
     *
     * @param def           parsed configuration (rooms/devices/people/sports)
     * @param deviceFactory factory for constructing devices
     * @param personFactory factory for constructing people
     * @throws IllegalStateException if configuration references an unknown room
     */
    public void initialize(HomeDefinition def, DeviceFactory deviceFactory, PersonFactory personFactory) {

        floors.clear();
        residents.clear();
        eventLog.clear();
        activityLog.clear();

        Floor floor = new Floor("Main floor", 0);

        Map<String, Room> roomsByName = new HashMap<>();
        for (RoomDefinition rd : def.rooms) {
            Room room = new Room(rd.name, rd.type);
            roomsByName.put(rd.name, room);
            floor.addRoom(room);
        }

        for (DeviceDefinition dd : def.devices) {
            Room room = requireRoom(roomsByName, dd.room, "Device " + dd.id);
            Device device = deviceFactory.createDevice(dd, room);
            device.connectEventBus(eventBus);
            room.addDevice(device);
        }

        for (PersonDefinition pd : def.persons) {
            Room room = requireRoom(roomsByName, pd.room, "Person " + pd.id);
            Person person = personFactory.createPerson(pd, room);
            residents.add(person);
            room.addPerson(person);
        }

        // Residents can listen to events (for output / reaction logic).
        for (Person p : residents) {
            eventBus.subscribe(p);
        }

        // Chain of Responsibility for event processing.
        EventHandler h1 = new FatherHandler();
        EventHandler h2 = new MotherHandler();
        EventHandler h3 = new DaughterHandler();
        EventHandler h4 = new GrandfatherHandler();
        EventHandler h5 = new FallbackHandler();

        h1.setNext(h2);
        h2.setNext(h3);
        h3.setNext(h4);
        h4.setNext(h5);

        // System listener: runs chain + writes to EventLog.
        eventBus.subscribe(new SystemEventListener(h1, this));

        for (SportDefinition sd : def.sports) {
            Room room = requireRoom(roomsByName, sd.room, "Sport " + sd.id);
            SportEquipment se = new SportEquipment(sd.id, sd.type, room);
            room.addSportEquipment(se);
        }

        floors.add(floor);
    }

    /**
     * Resolves a room by name or fails fast with a clear error.
     *
     * @param roomsByName mapping roomName -> room instance
     * @param name        referenced room name
     * @param owner       textual label of the object that references the room (used in error message)
     * @return resolved room instance
     * @throws IllegalStateException if the room does not exist in configuration
     */
    private Room requireRoom(Map<String, Room> roomsByName, String name, String owner) {
        Room room = roomsByName.get(name);
        if (room == null) {
            throw new IllegalStateException(owner + " references unknown room: " + name);
        }
        return room;
    }
}
