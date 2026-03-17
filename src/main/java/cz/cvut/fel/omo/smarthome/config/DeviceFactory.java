package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.consumption.ConsumptionProfile;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.Fridge;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Room;

public class DeviceFactory {

    // Creates a device and assigns a consumption profile based on its type.
    public Device createDevice(DeviceDefinition def, Room location) {
        Device device;
        if (def.type == DeviceType.FRIDGE) {
            device = new Fridge(def.id , def.name, location);
        } else {
            device = new GenericDevice(def.id, def.name, def.type, location);
        }

        // Define simplified consumption (Watts, Water L/h, Gas m3/h)
        ConsumptionProfile profile = switch (device.getType()) {
            case SMART_LIGHT -> new ConsumptionProfile(9, 0, 0);
            case GROUP_LIGHT -> new ConsumptionProfile(40, 0, 0);
            case SMART_TV -> new ConsumptionProfile(120, 0, 0);
            case MULTIROOM_AUDIO -> new ConsumptionProfile(30, 0, 0);

            case FRIDGE -> new ConsumptionProfile(150, 0, 0);

            // Heavy appliances
            case HUMIDIFIER_AC -> new ConsumptionProfile(600, 0.2, 0);
            case SMART_WASHING_MACHINE -> new ConsumptionProfile(500, 15, 0);
            case SMART_COFFEE_MACHINE -> new ConsumptionProfile(800, 0.5, 0);

            // Misc
            case IRRIGATION_SYSTEM -> new ConsumptionProfile(5, 30, 0);
            case THERMOSTAT -> new ConsumptionProfile(3, 0, 0);
            case PET_FEEDER -> new ConsumptionProfile(10, 0, 0);
            case SMART_MIRROR -> new ConsumptionProfile(50, 0, 0);
            case OUTDOOR_CAMERA -> new ConsumptionProfile(15, 0, 0);

            // Sensors/Passive -> negligible consumption
            default -> new ConsumptionProfile(1, 0, 0);
        };

        device.setConsumptionProfile(profile);
        return device;
    }
}