package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.Configuration;
import cz.cvut.fel.omo.smarthome.config.DeviceFactory;
import cz.cvut.fel.omo.smarthome.config.HomeDefinition;
import cz.cvut.fel.omo.smarthome.config.PersonFactory;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.reports.*;
import cz.cvut.fel.omo.smarthome.simulation.SimulationEngine;

public class Main {

    public static void main(String[] args) {
        System.out.println(">>> Smart Home Simulation Started <<<");

        // 1. Load Configuration
        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        // 2. Initialize System
        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Loaded: " + ctx.getResidents().size() + " residents, "
                + ctx.getFloors().get(0).getRooms().size() + " rooms.");

        // 3. Run Simulation
        // We delegate the logic to the Engine to keep Main clean.
        // Let's run for 50 steps.
        SimulationEngine engine = new SimulationEngine(ctx);
        engine.run(50);

        // 4. Generate Reports
        System.out.println("Generating reports...");

        new HouseConfigurationReportGenerator(ctx)
                .generate("output/house_configuration_report.txt");

        new ActivityReportGenerator(ctx.getActivityLog())
                .generate("output/activity_report.txt");

        new EventReportGenerator(ctx.getEventLog())
                .generate("output/event_report.txt");

        new ConsumptionReportGenerator(ctx.getConsumptionLog())
                .generate("output/consumption_report.txt");

        System.out.println("Done. Check 'output' folder.");
    }
}