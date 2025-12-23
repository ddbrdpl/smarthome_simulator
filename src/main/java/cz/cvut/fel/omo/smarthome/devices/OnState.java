// src/main/java/cz/cvut/fel/omo/smarthome/devices/OnState.java
package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.people.Role;

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
        // NEW: breakdown chance can depend on who last used the device
        int chance = 2; // default ~2%

        Person breaker = device.getLastUsedBy();
        if (breaker != null) {
            Role r = breaker.getRole();
            // tune as you like
            chance = switch (r) {
                case SON -> 6;
                case DAUGHTER -> 3;
                case GRANDFATHER -> 1;
                case MOTHER -> 2;
                case FATHER -> 2;
                default -> 2;
            };
        }

        if (RANDOM.nextInt(100) < chance) {
            device.setState(new BrokenState());

            // target = who caused it (can be null)
            device.getEventBus().publish(
                    new Event(EventType.DEVICE_BROKEN, device, breaker)
            );
        }
    }

    @Override
    public String getName() {
        return "ON";
    }
}
