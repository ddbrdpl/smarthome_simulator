package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.devices.DeviceState;
import cz.cvut.fel.omo.smarthome.devices.OffState;
import cz.cvut.fel.omo.smarthome.events.EventBus;
import cz.cvut.fel.omo.smarthome.house.Room;

public abstract class Device {

    private final String id;
    private final String name;
    private final DeviceType type;
    private final Room location;

    private DeviceState state;
    private EventBus eventBus;

    protected Device(String id, String name, DeviceType type, Room location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.state = new OffState();
    }

    public void connectEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void turnOn() {
        state.turnOn(this);
    }

    public void turnOff() {
        state.turnOff(this);
    }

    public void tick() {
        state.tick(this);
    }

    public void setState(DeviceState newState) {
        this.state = newState;
    }

    public String getStateName() {
        return state.getName();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public DeviceType getType() { return type; }
    public Room getLocation() { return location; }
}
