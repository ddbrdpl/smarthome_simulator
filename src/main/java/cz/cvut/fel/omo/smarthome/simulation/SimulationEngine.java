package cz.cvut.fel.omo.smarthome.simulation;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

public class SimulationEngine {

    private final SmartHomeContext ctx;

    public SimulationEngine(SmartHomeContext ctx) {
        this.ctx = ctx;
    }

    public void run(int steps) {
        for (int i = 0; i < steps; i++) {
            // Print current step info
            System.out.println("--- Step " + (i + 1) + " / " + steps + " --- Time: " + ctx.getCurrentTime());

            // 1. Advance simulation time by 15 minutes
            ctx.advanceTime(15);

            // 2. Residents perform their actions (decide between sport, devices, or shopping)
            for (Person p : ctx.getResidents()) {
                p.performStep(ctx);
            }

            // 3. Devices consume resources and update state (may break down here)
            for (Device d : ctx.getAllDevices()) {
                d.tick();
            }

            // 4. Update sport equipment timers (free up equipment if usage time is over)
            for (Floor f : ctx.getFloors()) {
                for (Room r : f.getRooms()) {
                    for (SportEquipment s : r.getSportEquipment()) {
                        s.tick();
                    }
                }
                System.out.println("Simulation finished.");
            }
        }
    }
}
