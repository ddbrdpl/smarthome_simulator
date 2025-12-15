package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

public class Main {
    public static void main(String[] args) {
        Configuration cfg = new Configuration("house.json"); // put house.json into resources
        HomeDefinition def = cfg.load();

        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Rooms loaded: " + ctx.getFloors().get(0).getRooms().size());
        System.out.println("Residents loaded: " + ctx.getResidents().size());
        System.out.println("SW3 step-2 OK");
    }
}
