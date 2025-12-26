package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventListener;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a resident (or entity) living in the smart home simulation.
 *
 * <p>A {@code Person} can:</p>
 * <ul>
 *   <li>Receive events from the {@link cz.cvut.fel.omo.smarthome.events.EventBus}</li>
 *   <li>Perform actions each simulation step (turning devices on/off)</li>
 *   <li>Be restricted by permissions ({@link PermissionSet})</li>
 * </ul>
 *
 * <p>This implementation also supports a simple "demand-based purchase" behavior:
 * with a small probability, the person may require a specific {@link DeviceType}.
 * If no such device exists in the home yet, the system can buy it via {@code AutoBuyer}
 * through the {@link SmartHomeContext}.</p>
 */
public class Person implements EventListener {

    /** Shared random generator for simulation decisions. */
    private static final Random RANDOM = new Random();

    /** Unique identifier of the person (from configuration). */
    private final String id;

    /** Human-readable name of the person (from configuration). */
    private final String name;

    /** Role of the person, used for permissions and reporting. */
    private final Role role;

    /** Current room where the person is located. */
    private Room location;

    /** Permission rules defining what the person may do with devices. */
    private final PermissionSet permissions;

    /**
     * Creates a new person in the smart home.
     *
     * @param id           unique identifier
     * @param name         display name
     * @param role         resident role
     * @param location     initial room location
     * @param permissions  permission rules for this role/person
     */
    public Person(String id, String name, Role role, Room location, PermissionSet permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.location = location;
        this.permissions = permissions;
    }

    /** @return unique person identifier */
    public String getId() { return id; }

    /** @return person display name */
    public String getName() { return name; }

    /** @return person role */
    public Role getRole() { return role; }

    /** @return current room location */
    public Room getLocation() { return location; }

    /**
     * Updates current location (room) of the person.
     *
     * @param location new room location
     */
    public void setLocation(Room location) { this.location = location; }

    /** @return permission set of this person */
    public PermissionSet getPermissions() { return permissions; }

    /**
     * Called when an event is published on the event bus.
     *
     * <p>Current implementation logs the received event to console only.</p>
     *
     * @param e published event
     */
    @Override
    public void onEvent(Event e) {
        System.out.println("[" + role + "] received event: " + e.getType());
    }

    /**
     * Performs one simulation step (one "tick") of behavior for this person.
     *
     * <p>Main responsibilities:</p>
     * <ul>
     *   <li>Skip actions for {@link Role#CAT}</li>
     *   <li>Collect all devices in the home</li>
     *   <li>Optionally trigger demand-based purchase if a desired device type is missing</li>
     *   <li>Pick a random device and attempt TURN_ON / TURN_OFF</li>
     *   <li>Check permissions and log either action or denial to {@link cz.cvut.fel.omo.smarthome.logs.ActivityLog}</li>
     *   <li>Mark the device as used by this person (used later for “who broke it”)</li>
     * </ul>
     *
     * @param ctx simulation context (home state, floors/rooms/devices, logs, auto buyer)
     */
    public void performStep(SmartHomeContext ctx) {

        if (this.role == Role.CAT) {
            return;
        }

        // ------- ORIGINAL: collect all devices -------
        List<Device> allDevices = new ArrayList<>();
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                allDevices.addAll(r.getDevices());
            }
        }

        // ------- NEW: sometimes "want a specific device type" -> buy if missing -------
        // Does NOT break original behavior; it only adds devices before picking random target.
        if (RANDOM.nextInt(100) < 15) { // ~15% of steps try "demand-based purchase"
            DeviceType desiredType = DeviceType.values()[RANDOM.nextInt(DeviceType.values().length)];

            if (!existsDeviceType(allDevices, desiredType)) {
                // buy ONLY when someone needs it
                ctx.getAutoBuyer().buyDevice(ctx, desiredType);

                // refresh list after purchase
                allDevices.clear();
                for (Floor f : ctx.getFloors()) {
                    for (Room r : f.getRooms()) {
                        allDevices.addAll(r.getDevices());
                    }
                }
            }
        }

        // ------- ORIGINAL: if still empty -> return -------
        if (allDevices.isEmpty()) return;

        // ------- ORIGINAL: choose random device -------
        Device target = allDevices.get(RANDOM.nextInt(allDevices.size()));
        Room targetRoom = target.getLocation();

        if (this.location != targetRoom) {
            this.location = targetRoom;
        }

        boolean on = RANDOM.nextBoolean();
        DeviceAction action = on ? DeviceAction.TURN_ON : DeviceAction.TURN_OFF;

        if (!permissions.canPerform(this, target, action)) {
            ctx.getActivityLog().add(new ActivityEntry(
                    this.id,
                    this.name,
                    "DENIED_" + action,
                    target.getName(),
                    LocalDateTime.now()
            ));
            return;
        }

        // remember who interacted with the device (used by breakdown logic)
        target.markUsedBy(this);

        if (on) {
            target.turnOn();
            ctx.getActivityLog().add(new ActivityEntry(this.id, this.name, "TURN_ON", target.getName(), LocalDateTime.now()));
        } else {
            target.turnOff();
            ctx.getActivityLog().add(new ActivityEntry(this.id, this.name, "TURN_OFF", target.getName(), LocalDateTime.now()));
        }
    }

    /**
     * Checks whether at least one device of the given type exists in the provided list.
     *
     * @param devices list of devices to scan
     * @param type desired device type
     * @return {@code true} if a device with that type exists, otherwise {@code false}
     */
    private boolean existsDeviceType(List<Device> devices, DeviceType type) {
        for (Device d : devices) {
            if (d.getType() == type) return true;
        }
        return false;
    }
}
