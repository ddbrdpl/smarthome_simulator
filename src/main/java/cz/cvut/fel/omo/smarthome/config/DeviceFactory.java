package cz.cvut.fel.omo.smarthome.config;

import cz.cvut.fel.omo.smarthome.consumption.ConsumptionProfile;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Room;

public class DeviceFactory {

    public Device createDevice(DeviceDefinition def, Room location) {

        Device device = new GenericDevice(
                def.id,
                def.name,
                def.type,
                location
        );

        // ---------- CONSUMPTION PROFILES ----------
        switch (device.getType()) {

            case SMART_LIGHT ->
                    device.setConsumptionProfile(new ConsumptionProfile(9, 0, 0));

            case GROUP_LIGHT ->
                    device.setConsumptionProfile(new ConsumptionProfile(40, 0, 0));

            case SMART_TV ->
                    device.setConsumptionProfile(new ConsumptionProfile(120, 0, 0));

            case MULTIROOM_AUDIO ->
                    device.setConsumptionProfile(new ConsumptionProfile(30, 0, 0));

            case HUMIDIFIER_AC ->
                    device.setConsumptionProfile(new ConsumptionProfile(600, 0.2, 0));

            case SMART_WASHING_MACHINE ->
                    device.setConsumptionProfile(new ConsumptionProfile(500, 15, 0));

            case IRRIGATION_SYSTEM ->
                    device.setConsumptionProfile(new ConsumptionProfile(5, 30, 0));

            case THERMOSTAT ->
                    device.setConsumptionProfile(new ConsumptionProfile(3, 0, 0));

            case SMART_COFFEE_MACHINE ->
                    device.setConsumptionProfile(new ConsumptionProfile(800, 0.5, 0));

            case PET_FEEDER ->
                    device.setConsumptionProfile(new ConsumptionProfile(10, 0, 0));

            case SMART_MIRROR ->
                    device.setConsumptionProfile(new ConsumptionProfile(50, 0, 0));

            case OUTDOOR_CAMERA ->
                    device.setConsumptionProfile(new ConsumptionProfile(15, 0, 0));

            default ->
                // Sensors, locks, detectors → almost zero consumption
                    device.setConsumptionProfile(new ConsumptionProfile(1, 0, 0));
        }

        return device;
    }
}
