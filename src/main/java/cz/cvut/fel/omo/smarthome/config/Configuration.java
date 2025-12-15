package cz.cvut.fel.omo.smarthome.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class Configuration {
    private final String resourcePath;

    public Configuration(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public HomeDefinition load() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Config resource not found: " + resourcePath);
            }
            return mapper.readValue(is, HomeDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config: " + resourcePath, e);
        }
    }
}
