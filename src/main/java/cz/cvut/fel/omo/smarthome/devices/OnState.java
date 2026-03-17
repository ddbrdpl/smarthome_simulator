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
        // Reduced breakdown chance: 0.2% base
        // 10000 bound: < 20 is 0.2%
        int chance = 500;

        Person user = device.getLastUsedBy();
        if (user != null) {
            chance = switch (user.getRole()) {
                case SON -> 1000;       // 0.6%
                case DAUGHTER -> 700;  // 0.3%
                default -> 500;        // 0.2%
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