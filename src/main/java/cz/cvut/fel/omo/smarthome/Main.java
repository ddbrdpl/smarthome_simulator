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

        // 2. Initialize System (House, Residents, Devices)
        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Loaded: " + ctx.getResidents().size() + " residents, "
                + ctx.getFloors().get(0).getRooms().size() + " rooms.");

        // 3. Run Simulation
        SimulationEngine engine = new SimulationEngine(ctx);
        // Run for 50 steps (approx. 12.5 hours of in-game time)
        engine.run(50);

        // 4. Generate Reports
        System.out.println("Generating reports...");

        // Using the ReportGenerator interface for consistency (Polymorphism)

        // House Configuration Report
        ReportGenerator configRep = new HouseConfigurationReportGenerator(ctx);
        configRep.generate("output/house_configuration_report.txt");

        // Activity Report (Who turned on/bought what)
        ReportGenerator activityRep = new ActivityReportGenerator(ctx.getActivityLog());
        activityRep.generate("output/activity_report.txt");

        // Event Report (Breakdowns, Repairs, Handling)
        ReportGenerator eventRep = new EventReportGenerator(ctx.getEventLog());
        eventRep.generate("output/event_report.txt");

        // Consumption & Billing Report
        // Note: passing 'ctx' because this generator calculates costs based on usage
        ReportGenerator consumptionRep = new ConsumptionReportGenerator(ctx);
        consumptionRep.generate("output/consumption_report.txt");

        System.out.println("Done. Check 'output' folder.");
    }
}