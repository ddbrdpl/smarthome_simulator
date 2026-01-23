package cz.cvut.fel.omo.smarthome.config;

import java.util.ArrayList;
import java.util.List;

// Root DTO for JSON mapping.
public class HomeDefinition {
    public List<RoomDefinition> rooms = new ArrayList<>();
    public List<PersonDefinition> persons = new ArrayList<>();
    public List<DeviceDefinition> devices = new ArrayList<>();
    public List<SportDefinition> sports = new ArrayList<>();
}