package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.Configuration;
import cz.cvut.fel.omo.smarthome.config.DeviceFactory;
import cz.cvut.fel.omo.smarthome.config.HomeDefinition;
import cz.cvut.fel.omo.smarthome.config.PersonFactory;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

public class Main {
    public static void main(String[] args) {
        // Load configuration from resources
        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        // Initialize home context
        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        // Basic info
        int roomsCount = ctx.getFloors().get(0).getRooms().size();
        int residentsCount = ctx.getResidents().size();

        System.out.println("Rooms loaded: " + roomsCount);
        System.out.println("Residents loaded: " + residentsCount);

        // Demo events (Observer + Chain of Responsibility)
        ctx.getEventBus().publish(new Event(EventType.WATER_LEAK, "WaterLeakSensor", null));
        ctx.getEventBus().publish(new Event(EventType.SMOKE_ALERT, "SmokeGasSensor", null));
        ctx.getEventBus().publish(new Event(EventType.PET_OUTSIDE_LONG, "OutdoorCamera", null));
        ctx.getEventBus().publish(new Event(EventType.POOL_ALERT, "PoolSensor", null));

        System.out.println("SW3 OK");
        ctx.getFloors().get(0).getRooms().get(0).getDevices().forEach(d -> {
            d.turnOn();
            d.tick();
            System.out.println(d.getName() + " state=" + d.getStateName());
        });

    }

}
