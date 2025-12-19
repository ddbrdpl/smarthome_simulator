package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.Configuration;
import cz.cvut.fel.omo.smarthome.config.DeviceFactory;
import cz.cvut.fel.omo.smarthome.config.HomeDefinition;
import cz.cvut.fel.omo.smarthome.config.PersonFactory;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.reports.ActivityReportGenerator;
import cz.cvut.fel.omo.smarthome.reports.EventReportGenerator;
import cz.cvut.fel.omo.smarthome.reports.HouseConfigurationReportGenerator;
import cz.cvut.fel.omo.smarthome.simulation.SimulationEngine;

public class    Main {

    public static void main(String[] args) {

        // Load configuration
        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        // Initialize Smart Home
        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Rooms loaded: " + ctx.getFloors().get(0).getRooms().size());
        System.out.println("Residents loaded: " + ctx.getResidents().size());

        // Run simulation
        SimulationEngine engine = new SimulationEngine(ctx);
        engine.run(30);

        // Generate reports
        new HouseConfigurationReportGenerator(ctx)
                .generate("output/house_configuration_report.txt");

        new ActivityReportGenerator(ctx.getActivityLog())
                .generate("output/activity_report.txt");

        new EventReportGenerator(ctx.getEventLog())
                .generate("output/event_report.txt");

        System.out.println("All reports generated.");

    }
}
