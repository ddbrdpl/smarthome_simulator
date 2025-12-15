package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;

import java.util.Random;

public class OnState implements DeviceState {

    private static final Random RANDOM = new Random();



    @Override
    public void turnOn(Device device) {
        // already on
    }

    @Override
    public void turnOff(Device device) {
        device.setState(new OffState());
    }

    @Override
    public void tick(Device device) {
        // small chance of breakdown
        if (RANDOM.nextInt(100) < 2) { // ~2% chance
            device.setState(new BrokenState());
            device.getEventBus().publish(
                    new Event(EventType.DEVICE_BROKEN, device, null)
            );
        }
    }

    @Override
    public String getName() {
        return "ON";
    }


}
