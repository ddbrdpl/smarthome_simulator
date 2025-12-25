package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionProfile;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventBus;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.Person;

/**
 * Abstract base class representing a smart home device.
 *
 * <p>A device has:</p>
 * <ul>
 *   <li>Identity (id, name, type)</li>
 *   <li>Physical location ({@link Room})</li>
 *   <li>Internal state handled via the State pattern ({@link DeviceState})</li>
 *   <li>Optional {@link ConsumptionProfile} for resource usage</li>
 *   <li>Connection to {@link EventBus} for publishing events</li>
 * </ul>
 *
 * <p>The device lifecycle is controlled by {@link DeviceState} implementations
 * such as {@code OnState}, {@code OffState}, and {@code BrokenState}.</p>
 *
 * <p>The device also tracks the last {@link Person} who interacted with it,
 * which is later used to determine responsibility for breakdowns.</p>
 */
public abstract class Device {

    /** Unique device identifier */
    private final String id;

    /** Human-readable device name */
    private final String name;

    /** Logical device type */
    private final DeviceType type;

    /** Room where the device is installed */
    private final Room location;

    /** Current internal device state */
    private DeviceState state;

    /** Event bus used to publish device-related events */
    private EventBus eventBus;

    /** Consumption model associated with the device */
    private ConsumptionProfile consumptionProfile;

    /** Person who last interacted with the device */
    private Person lastUsedBy;

    /**
     * Creates a new device instance.
     *
     * <p>Devices start in {@link OffState} by default.</p>
     *
     * @param id unique identifier
     * @param name human-readable name
     * @param type device type
     * @param location room where the device is installed
     */
    protected Device(String id, String name, DeviceType type, Room location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.state = new OffState();
    }

    /**
     * Connects the device to the global event bus.
     *
     * @param eventBus event bus used for publishing events
     */
    public void connectEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * @return event bus associated with the device
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Turns the device on.
     *
     * <p>The behavior depends on the current {@link DeviceState}.</p>
     */
    public void turnOn() {
        state.turnOn(this);
    }

    /**
     * Turns the device off.
     *
     * <p>The behavior depends on the current {@link DeviceState}.</p>
     */
    public void turnOff() {
        state.turnOff(this);
    }

    /**
     * Performs one simulation tick on the device.
     *
     * <p>This method is called once per simulation step and delegates
     * behavior to the current {@link DeviceState}.</p>
     */
    public void tick() {
        state.tick(this);
    }

    /**
     * Changes the internal device state.
     *
     * @param newState new state to be set
     */
    public void setState(DeviceState newState) {
        this.state = newState;
    }

    /**
     * @return name of the current device state
     */
    public String getStateName() {
        return state.getName();
    }

    /**
     * Repairs the device by resetting it to {@link OffState}.
     *
     * <p>Used by event handlers when handling {@code DEVICE_BROKEN} events.</p>
     */
    public void repair() {
        this.setState(new OffState());
    }

    /**
     * Publishes an event to the event bus if connected.
     *
     * @param e event to publish
     */
    public void publishEvent(Event e) {
        if (eventBus != null) {
            eventBus.publish(e);
        }
    }

    /**
     * Assigns a consumption profile to the device.
     *
     * @param profile consumption profile
     */
    public void setConsumptionProfile(ConsumptionProfile profile) {
        this.consumptionProfile = profile;
    }

    /**
     * @return consumption profile of the device
     */
    public ConsumptionProfile getConsumptionProfile() {
        return consumptionProfile;
    }

    /**
     * Accumulates resource consumption for the current simulation step.
     *
     * <p>Consumption is added only if:</p>
     * <ul>
     *   <li>A consumption profile is defined</li>
     *   <li>The device is currently in {@code ON} state</li>
     * </ul>
     *
     * @param stepMinutes length of simulation step in minutes
     * @param log consumption log where usage is recorded
     */
    public void accumulateConsumption(int stepMinutes, ConsumptionLog log) {
        if (consumptionProfile == null) return;
        if (!"ON".equals(getStateName())) return;

        double hours = stepMinutes / 60.0;

        double addPowerKWh = (consumptionProfile.getPowerW() * hours) / 1000.0;
        double addWaterL = consumptionProfile.getWaterLPerHour() * hours;
        double addGasM3 = consumptionProfile.getGasM3PerHour() * hours;

        log.addUsage(getId(), getName(), addPowerKWh, addWaterL, addGasM3);
    }

    /**
     * Marks the person who last interacted with the device.
     *
     * <p>This information is later used to determine who caused
     * a device breakdown.</p>
     *
     * @param p person who used the device
     */
    public void markUsedBy(Person p) {
        this.lastUsedBy = p;
    }

    /**
     * @return person who last used the device, or {@code null} if unknown
     */
    public Person getLastUsedBy() {
        return lastUsedBy;
    }

    /** @return device identifier */
    public String getId() { return id; }

    /** @return device name */
    public String getName() { return name; }

    /** @return device type */
    public DeviceType getType() { return type; }

    /** @return room where the device is installed */
    public Room getLocation() { return location; }
}
