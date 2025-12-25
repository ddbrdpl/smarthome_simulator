package cz.cvut.fel.omo.smarthome.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration object representing the entire smart home setup.
 *
 * <p>This class is used as a data container for JSON deserialization.
 * It defines all structural elements of the smart home:</p>
 *
 * <ul>
 *   <li>rooms</li>
 *   <li>persons (residents)</li>
 *   <li>devices</li>
 *   <li>sport equipment</li>
 * </ul>
 *
 * <p>The class contains only public fields on purpose
 * to simplify Jackson mapping.</p>
 */
public class HomeDefinition {

    /** List of room definitions present in the house. */
    public List<RoomDefinition> rooms = new ArrayList<>();

    /** List of persons living in the smart home. */
    public List<PersonDefinition> persons = new ArrayList<>();

    /** List of devices installed in the smart home. */
    public List<DeviceDefinition> devices = new ArrayList<>();

    /** List of sport equipment available in the smart home. */
    public List<SportDefinition> sports = new ArrayList<>();
}
