package cz.cvut.fel.omo.smarthome;

import cz.cvut.fel.omo.smarthome.config.Configuration;
import cz.cvut.fel.omo.smarthome.config.DeviceFactory;
import cz.cvut.fel.omo.smarthome.config.HomeDefinition;
import cz.cvut.fel.omo.smarthome.config.PersonFactory;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.reports.ActivityReportGenerator;
import cz.cvut.fel.omo.smarthome.reports.EventReportGenerator;
import cz.cvut.fel.omo.smarthome.reports.HouseConfigurationReportGenerator;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

public class Main {

    public static void main(String[] args) {
        // ---- LOAD CONFIG ----
        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        // ---- INIT CONTEXT ----
        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        System.out.println("Rooms loaded: " + ctx.getFloors().get(0).getRooms().size());
        System.out.println("Residents loaded: " + ctx.getResidents().size());

        // ---- SIMULATION ----
        int steps = 50; // increase if you want more WAIT_SPORT
        for (int step = 0; step < steps; step++) {

            // People actions
            for (Person p : ctx.getResidents()) {
                p.performStep(ctx);
            }

            // Devices tick (breakdowns etc.)
            for (Floor f : ctx.getFloors()) {
                for (Room r : f.getRooms()) {
                    for (Device d : r.getDevices()) {
                        d.tick();
                    }
                }
            }

            // Sports tick (release after N steps)
            for (Floor f : ctx.getFloors()) {
                for (Room r : f.getRooms()) {
                    for (SportEquipment se : r.getSportEquipment()) {
                        se.tick();
                    }
                }
            }

            // Small delay to make timestamps less "same"
            try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        }

        // ---- REPORTS ----
        new HouseConfigurationReportGenerator(ctx).generate("output/house_configuration_report.txt");
        new ActivityReportGenerator(ctx.getActivityLog()).generate("output/activity_report.txt");
        new EventReportGenerator(ctx.getEventLog()).generate("output/event_report.txt");

        System.out.println("SW3 OK");
    }
}
