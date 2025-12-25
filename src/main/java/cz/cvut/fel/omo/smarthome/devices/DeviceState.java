package cz.cvut.fel.omo.smarthome.devices;

/**
 * Represents a state of a {@link Device} in the State design pattern.
 *
 * <p>Each device delegates its behavior to an implementation of this interface.
 * The concrete state determines how the device reacts to actions such as
 * turning on, turning off, or performing a simulation tick.</p>
 *
 * <p>Typical implementations include:</p>
 * <ul>
 *   <li>{@link OnState}</li>
 *   <li>{@link OffState}</li>
 *   <li>{@link BrokenState}</li>
 * </ul>
 */
public interface DeviceState {

    /**
     * Handles a request to turn the device on.
     *
     * @param device device whose state is affected
     */
    void turnOn(Device device);

    /**
     * Handles a request to turn the device off.
     *
     * @param device device whose state is affected
     */
    void turnOff(Device device);

    /**
     * Performs one simulation step for the device.
     *
     * <p>This method is called periodically by the simulation engine.</p>
     *
     * @param device device whose state is updated
     */
    void tick(Device device);

    /**
     * @return human-readable name of the state
     */
    String getName();
}
