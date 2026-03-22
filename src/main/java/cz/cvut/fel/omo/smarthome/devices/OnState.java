package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;
import cz.cvut.fel.omo.smarthome.people.Person;
import java.util.Random;

public class OnState implements DeviceState {

    private static final Random RANDOM = new Random();

    @Override
    public void turnOn(Device device) {
        // already on
    }

    @Override
    public void turnOff(Device device) {
        device.setState(new OffState());
    }

    @Override
    public void tick(Device device) {
        // Breakdown chance per tick (out of 10000):
        // base  = 20  → 0.20%
        // son   = 50  → 0.50%
        // daughter = 35 → 0.35%
        int chance = 20;

        Person user = device.getLastUsedBy();
        if (user != null) {
            chance = switch (user.getRole()) {
                case SON      -> 50;
                case DAUGHTER -> 35;
                default       -> 20;
            };
        }

        if (RANDOM.nextInt(10000) < chance) {
            device.setState(new BrokenState());
            device.publishEvent(new Event(EventType.DEVICE_BROKEN, device, user));
        }
    }

    @Override
    public String toString() { return "ON"; }
}