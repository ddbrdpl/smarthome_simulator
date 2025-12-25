package cz.cvut.fel.omo.smarthome.devices;

/**
 * Device state representing an inactive (OFF) device.
 *
 * <p>In this state, the device does not consume resources and
 * cannot break during simulation ticks.</p>
 */
public class OffState implements DeviceState {

    /**
     * Turns the device on by transitioning to {@link OnState}.
     *
     * @param device device to turn on
     */
    @Override
    public void turnOn(Device device) {
        device.setState(new OnState());
    }

    /**
     * Turning off a device that is already OFF has no effect.
     *
     * @param device device already off
     */
    @Override
    public void turnOff(Device device) {
        // already off
    }

    /**
     * No action is performed during simulation ticks while the device is OFF.
     *
     * @param device device being simulated
     */
    @Override
    public void tick(Device device) {
        // no activity when off
    }

    /**
     * @return state name {@code "OFF"}
     */
    @Override
    public String getName() {
        return "OFF";
    }
}
