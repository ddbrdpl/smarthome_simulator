package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.events.*;

public class Main {
    public static void main(String[] args) {
        Configuration cfg = new Configuration("house.json"); // put house.json into resources
        HomeDefinition def = cfg.load();

        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Rooms loaded: " + ctx.getFloors().get(0).getRooms().size());
        System.out.println("Residents loaded: " + ctx.getResidents().size());
        System.out.println("SW3 step-2 OK");

        EventBus bus = new EventBus();

        EventHandler h1 = new FatherHandler();
        EventHandler h2 = new MotherHandler();
        EventHandler h3 = new DaughterHandler();
        EventHandler h4 = new GrandfatherHandler();
        EventHandler h5 = new FallbackHandler();

        h1.setNext(h2);
        h2.setNext(h3);
        h3.setNext(h4);
        h4.setNext(h5);

        bus.subscribe(e -> h1.handle(e));

        bus.publish(new Event(EventType.WATER_LEAK, "WaterLeakSensor", null));
        bus.publish(new Event(EventType.SMOKE_ALERT, "SmokeGasSensor", null));
        bus.publish(new Event(EventType.PET_OUTSIDE_LONG, "OutdoorCamera", null));
        bus.publish(new Event(EventType.POOL_ALERT, "SmartPool", null));
    }
}
