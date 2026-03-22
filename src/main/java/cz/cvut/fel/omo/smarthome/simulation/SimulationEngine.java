package cz.cvut.fel.omo.smarthome.simulation;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.Animal;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

public class SimulationEngine {

    private final SmartHomeContext ctx;

    public SimulationEngine(SmartHomeContext ctx) {
        this.ctx = ctx;
    }

    public void runStep() {
        ctx.advanceTime(15);

        for (Person p : ctx.getResidents()) {
            p.performStep(ctx);
        }

        for (Animal a : ctx.getAnimals()) {
            a.performStep(ctx);
        }

        for (Device d : ctx.getAllDevices()) {
            d.tick();
            d.accumulateConsumption(15, ctx.getConsumptionLog());
        }

        for (SportEquipment s : ctx.getAllSportEquipment()) {
            s.tick();
        }
    }

    public void run(int steps) {
        for (int i = 0; i < steps; i++) {
            System.out.println("--- Step " + (i + 1) + " / " + steps
                    + " --- Time: " + ctx.getCurrentTime());
            runStep();
        }
        System.out.println("Simulation finished.");
    }
}