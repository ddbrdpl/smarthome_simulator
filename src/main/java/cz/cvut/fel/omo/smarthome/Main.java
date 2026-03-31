package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.Configuration;
import cz.cvut.fel.omo.smarthome.config.DeviceFactory;
import cz.cvut.fel.omo.smarthome.config.HomeDefinition;
import cz.cvut.fel.omo.smarthome.config.PersonFactory;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.visualization.SimulationVisualizer;
import cz.cvut.fel.omo.smarthome.reports.*;

public class Main {

    public static void main(String[] args) {
        System.out.println(">>> Smart Home Simulation Started <<<");

        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Loaded: " + ctx.getResidents().size() + " residents, "
                + ctx.getFloors().get(0).getRooms().size() + " rooms.");

        // Launch visual replay — reports generated on demand via UI button
        SimulationVisualizer.show(ctx, 60, () -> generateReports(ctx));
    }

    public static void generateReports(SmartHomeContext ctx) {
        System.out.println("Generating reports...");

        new HouseConfigurationReportGenerator(ctx)
                .generate("output/house_configuration_report.txt");

        new ActivityReportGenerator(ctx.getActivityLog())
                .generate("output/activity_report.txt");

        new EventReportGenerator(ctx.getEventLog())
                .generate("output/event_report.txt");

        new ConsumptionReportGenerator(ctx)
                .generate("output/consumption_report.txt");

        System.out.println("Done. Check 'output' folder.");
    }
}