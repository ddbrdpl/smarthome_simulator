package cz.cvut.fel.omo.smarthome.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads smart home configuration from a JSON resource file.
 *
 * <p>This class is responsible for reading the initial configuration
 * of the smart home (rooms, devices, persons, sport equipment, etc.)
 * from a JSON file located on the application classpath.</p>
 *
 * <p>The JSON structure is mapped into {@link HomeDefinition}
 * using Jackson {@link ObjectMapper}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *     Configuration cfg = new Configuration("house.json");
 *     HomeDefinition def = cfg.load();
 * </pre>
 *
 * <p>If the configuration file cannot be found or parsed,
 * an {@link IllegalStateException} is thrown, because the application
 * cannot continue without a valid configuration.</p>
 */
public class Configuration {

    /** Path to the configuration resource on the classpath. */
    private final String resourcePath;

    /**
     * Creates a configuration loader for a given resource path.
     *
     * @param resourcePath path to JSON configuration file (e.g. {@code "house.json"})
     */
    public Configuration(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Loads and parses the configuration file into a {@link HomeDefinition}.
     *
     * @return parsed home definition
     *
     * @throws IllegalStateException if the resource is not found
     *                               or cannot be parsed
     */
    public HomeDefinition load() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new IllegalStateException(
                        "Config resource not found: " + resourcePath
                );
            }

            return mapper.readValue(is, HomeDefinition.class);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load config: " + resourcePath, e
            );
        }
    }
}
