package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.util.List;
import java.util.Random;

public class Cat extends Animal {

    private static final Random RANDOM = new Random();

    public Cat(String id, String name, Room location) {
        super(id, name, location);
    }

    @Override
    public void performStep(SmartHomeContext ctx) {
        if (RANDOM.nextInt(100) < 30) {
            List<Room> allRooms = ctx.getAllRooms();
            if (!allRooms.isEmpty()) {
                Room target = allRooms.get(RANDOM.nextInt(allRooms.size()));
                if (target != this.location) {
                    moveTo(target);
                    logActivity(ctx, "WANDERED_TO", target.getName());
                }
            }
        } else {
            logActivity(ctx, "SLEEPING", location.getName());
        }
    }

    private void logActivity(SmartHomeContext ctx, String action, String target) {
        ctx.getActivityLog().add(new ActivityEntry(
                id, name, action, target, ctx.getCurrentTime()
        ));
    }
}