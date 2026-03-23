package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.Configuration;
import cz.cvut.fel.omo.smarthome.config.DeviceFactory;
import cz.cvut.fel.omo.smarthome.config.HomeDefinition;
import cz.cvut.fel.omo.smarthome.config.PersonFactory;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.visualization.SimulationVisualizer;

public class Main {

    public static void main(String[] args) {
        System.out.println(">>> Smart Home Simulation Started <<<");

        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Loaded: " + ctx.getResidents().size() + " residents, "
                + ctx.getFloors().get(0).getRooms().size() + " rooms.");

        // Launch visual replay — 60 steps, controlled by UI
        SimulationVisualizer.show(ctx, 60);
    }
}