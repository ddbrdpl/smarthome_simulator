package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.*;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.reports.HouseConfigurationReportGenerator;
import cz.cvut.fel.omo.smarthome.simulation.SimulationEngine;

public class Main {
    public static void main(String[] args) {
        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Rooms loaded: " + ctx.getFloors().get(0).getRooms().size());
        System.out.println("Residents loaded: " + ctx.getResidents().size());


        SimulationEngine engine = new SimulationEngine(ctx);
        engine.run(30);


        new HouseConfigurationReportGenerator(ctx)
                .generate("output/house_configuration_report.txt");

        System.out.println("Simulation finished, report generated.");

        new cz.cvut.fel.omo.smarthome.reports.ActivityReportGenerator(
                ctx.getActivityLog()
        ).generate("output/activity_report.txt");

    }
}
