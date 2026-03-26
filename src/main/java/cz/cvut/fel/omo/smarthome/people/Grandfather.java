package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.simulation.Weather;

public class Grandfather extends Person {

    private boolean thermostatActivated = false;

    public Grandfather(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        Weather weather = ctx.getWeatherService().getCurrent();

        // Cold weather → turn on thermostat immediately
        if (weather == Weather.COLD && !thermostatActivated) {
            if (tryActivateThermostat(ctx)) {
                thermostatActivated = true;
                return;
            }
        }

        // Weather improved → reset flag so he can activate again next cold spell
        if (weather != Weather.COLD) {
            thermostatActivated = false;
        }

        super.performDeviceLogic(ctx);
    }

    private boolean tryActivateThermostat(SmartHomeContext ctx) {
        for (Device d : ctx.getAllDevices()) {
            if (d.getType() == DeviceType.THERMOSTAT && d.isOff()) {
                moveTo(d.getLocation());
                d.turnOn();
                d.markUsedBy(this);
                logActivity(ctx, "TURN_ON_THERMOSTAT",
                        d.getName() + " (cold weather: " + ctx.getWeatherService().getCurrent().getDisplayName() + ")");
                System.out.println(" [" + name + "] It's cold! Turning on thermostat.");
                return true;
            }
        }
        return false;
    }
}