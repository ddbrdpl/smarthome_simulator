package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.people.Role;

import java.util.Random;

/**
 * Device state representing an active (ON) device.
 *
 * <p>While in this state, the device may randomly break during simulation ticks.
 * The probability of breakdown can depend on the {@link Person} who last used
 * the device.</p>
 *
 * <p>When a breakdown occurs, the device transitions to {@link BrokenState}
 * and publishes a {@link EventType#DEVICE_BROKEN} event.</p>
 */
public class OnState implements DeviceState {

    /** Random generator used for breakdown probability */
    private static final Random RANDOM = new Random();

    /**
     * Turning on a device that is already ON has no effect.
     *
     * @param device device already in ON state
     */
    @Override
    public void turnOn(Device device) {
        // already on
    }

    /**
     * Turns the device off by transitioning to {@link OffState}.
     *
     * @param device device to turn off
     */
    @Override
    public void turnOff(Device device) {
        device.setState(new OffState());
    }

    /**
     * Performs a simulation tick.
     *
     * <p>There is a random chance that the device will break.
     * The probability depends on the role of the person who last used the device.</p>
     *
     * <p>If a breakdown occurs, the device transitions to {@link BrokenState}
     * and publishes a {@link EventType#DEVICE_BROKEN} event.</p>
     *
     * @param device device being simulated
     */
    @Override
    public void tick(Device device) {
        int chance = 2; // default breakdown probability (%)

        Person breaker = device.getLastUsedBy();
        if (breaker != null) {
            Role r = breaker.getRole();
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

            device.getEventBus().publish(
                    new Event(EventType.DEVICE_BROKEN, device, breaker)
            );
        }
    }

    /**
     * @return state name {@code "ON"}
     */
    @Override
    public String getName() {
        return "ON";
    }
}
