package cz.cvut.fel.omo.smarthome.devices;

/**
 * Device state representing a broken device.
 *
 * <p>A broken device cannot be turned on or off and does not perform
 * any actions during simulation ticks.</p>
 *
 * <p>The device must be repaired externally (e.g. by an event handler)
 * to transition back to {@link OffState}.</p>
 */
public class BrokenState implements DeviceState {

    /**
     * Turning on a broken device has no effect.
     *
     * @param device broken device
     */
    @Override
    public void turnOn(Device device) {
        // broken -> ignore
    }

    /**
     * Turning off a broken device has no effect.
     *
     * @param device broken device
     */
    @Override
    public void turnOff(Device device) {
        // broken -> ignore
    }

    /**
     * No action is performed during simulation ticks while the device is broken.
     *
     * @param device broken device
     */
    @Override
    public void tick(Device device) {
        // nothing
    }

    /**
     * @return state name {@code "BROKEN"}
     */
    @Override
    public String getName() {
        return "BROKEN";
    }
}
