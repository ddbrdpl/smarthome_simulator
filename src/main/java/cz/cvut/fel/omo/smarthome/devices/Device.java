package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionProfile;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventBus;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.Person;

public abstract class Device {

    private final String id;
    private final String name;
    private final DeviceType type;
    private final Room location;

    // State Pattern
    private DeviceState state = new OffState();

    private EventBus eventBus;
    private ConsumptionProfile consumptionProfile;
    private Person lastUsedBy;

    protected Device(String id, String name, DeviceType type, Room location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
    }

    // --- State Management ---

    public void turnOn() {
        state.turnOn(this);
    }

    public void turnOff() {
        state.turnOff(this);
    }

    public void tick() {
        state.tick(this);
    }

    public void repair() {
        setState(new OffState());
    }

    public void setState(DeviceState newState) {
        this.state = newState;
    }

    public String getStateName() {
        return state.getName();
    }

    // --- Simulation Logic ---

    public void accumulateConsumption(int stepMinutes, ConsumptionLog log) {
        if (consumptionProfile == null || !"ON".equals(getStateName())) return;

        double hours = stepMinutes / 60.0;
        double kwh = (consumptionProfile.getPowerW() * hours) / 1000.0;
        double water = consumptionProfile.getWaterLPerHour() * hours;
        double gas = consumptionProfile.getGasM3PerHour() * hours;

        log.addUsage(id, name, kwh, water, gas);
    }

    public void publishEvent(Event e) {
        if (eventBus != null) eventBus.publish(e);
    }

    public void markUsedBy(Person p) {
        this.lastUsedBy = p;
    }

    // --- Getters / Setters ---

    public void connectEventBus(EventBus eventBus) { this.eventBus = eventBus; }
    public EventBus getEventBus() { return eventBus; }

    public void setConsumptionProfile(ConsumptionProfile profile) { this.consumptionProfile = profile; }
    public ConsumptionProfile getConsumptionProfile() { return consumptionProfile; }

    public Person getLastUsedBy() { return lastUsedBy; }
    public String getId() { return id; }
    public String getName() { return name; }
    public DeviceType getType() { return type; }
    public Room getLocation() { return location; }
}