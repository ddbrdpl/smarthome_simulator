package cz.cvut.fel.omo.smarthome.simulation;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.Person;

import java.util.List;

public class SimulationEngine {
    private final SmartHomeContext ctx;

    public SimulationEngine(SmartHomeContext ctx) {
        this.ctx = ctx;
    }

    public void run(int steps) {
        for (int i = 0; i < steps; i++) {
            // persons do something
            for (Person p : ctx.getResidents()) {
                p.performStep(ctx);
            }

            // devices tick
            List<Floor> floors = ctx.getFloors();
            for (Floor f : floors) {
                for (Room r : f.getRooms()) {
                    for (Device d : r.getDevices()) {
                        d.tick();
                    }
                }
            }
        }
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

    }
}
