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
import cz.cvut.fel.omo.smarthome.reports.ConsumptionReportGenerator;
import cz.cvut.fel.omo.smarthome.reports.EventReportGenerator;
import cz.cvut.fel.omo.smarthome.reports.HouseConfigurationReportGenerator;
import cz.cvut.fel.omo.smarthome.shop.AutoBuyer;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

/**
 * Application entry point.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load house configuration from {@code house.json}.</li>
 *   <li>Initialize {@link SmartHomeContext} with devices and people factories.</li>
 *   <li>Run a simple time-stepped simulation (people actions, device ticks, sport equipment ticks).</li>
 *   <li>Generate output reports: configuration, activity, event and consumption.</li>
 * </ul>
 *
 * <p>Simulation model:</p>
 * <ul>
 *   <li>One iteration of the loop represents {@code stepMinutes} minutes.</li>
 *   <li>People choose random devices and attempt actions (permission check is inside {@link Person}).</li>
 *   <li>Devices tick their internal state machine and may publish events.</li>
 *   <li>Devices accumulate consumption while they are in {@code ON} state.</li>
 * </ul>
 */
public class Main {

    /**
     * Starts the SmartHome simulation.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {

        // ---------- LOAD CONFIG ----------
        Configuration cfg = new Configuration("house.json");
        HomeDefinition def = cfg.load();

        // ---------- INIT CONTEXT ----------
        SmartHomeContext ctx = SmartHomeContext.getInstance();
        ctx.initialize(def, new DeviceFactory(), new PersonFactory());

        // Bonus feature: automatic purchasing (currently created here, actual usage may be triggered elsewhere).
        AutoBuyer autoBuyer = new AutoBuyer();

        System.out.println("Rooms loaded: " + ctx.getFloors().get(0).getRooms().size());
        System.out.println("Residents loaded: " + ctx.getResidents().size());

        // ---------- SIMULATION ----------
        int steps = 50;
        int stepMinutes = 10; // one simulation step = 10 minutes

        for (int step = 0; step < steps; step++) {

            // ---- PEOPLE ACTIONS ----
            for (Person p : ctx.getResidents()) {
                p.performStep(ctx);
            }

            // ---- DEVICES ----
            for (Floor f : ctx.getFloors()) {
                for (Room r : f.getRooms()) {
                    for (Device d : r.getDevices()) {
                        d.tick(); // breakdowns etc.
                        d.accumulateConsumption(stepMinutes, ctx.getConsumptionLog());
                    }
                }
            }

            // ---- SPORTS ----
            for (Floor f : ctx.getFloors()) {
                for (Room r : f.getRooms()) {
                    for (SportEquipment se : r.getSportEquipment()) {
                        se.tick();
                    }
                }
            }

            // Small delay to diversify timestamps
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {}
        }

        // ---------- REPORTS ----------
        new HouseConfigurationReportGenerator(ctx)
                .generate("output/house_configuration_report.txt");

        new ActivityReportGenerator(ctx.getActivityLog())
                .generate("output/activity_report.txt");

        new EventReportGenerator(ctx.getEventLog())
                .generate("output/event_report.txt");

        new ConsumptionReportGenerator(ctx.getConsumptionLog())
                .generate("output/consumption_report.txt");

        System.out.println("SW3 OK");
    }
}
