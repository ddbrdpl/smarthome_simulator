package cz.cvut.fel.omo.smarthome.simulation;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.Person;

public class SimulationEngine {

    private final SmartHomeContext ctx;

    public SimulationEngine(SmartHomeContext ctx) {
        this.ctx = ctx;
    }

    public void run(int steps) {
        System.out.println("Starting simulation for " + steps + " steps...");

        // Define step duration (e.g. 15 minutes per loop iteration)
        int stepDurationMinutes = 15;

        for (int i = 0; i < steps; i++) {

            // >>> ADVANCE TIME <<<
            ctx.advanceTime(stepDurationMinutes);

            // 1. People act
            for (Person p : ctx.getResidents()) {
                p.performStep(ctx);
            }
            // 2. Devices tick and accumulate consumption


            for (Floor f : ctx.getFloors()) {
                for (Room r : f.getRooms()) {
                    for (Device d : r.getDevices()) {
                        d.tick();
                        d.accumulateConsumption(stepDurationMinutes, ctx.getConsumptionLog());
                    }
                    // Sports equipment tick (cooldowns etc)
                    r.getSportEquipment().forEach(cz.cvut.fel.omo.smarthome.sports.SportEquipment::tick);
                }
            }
        }

        System.out.println("Simulation finished.");
    }
}