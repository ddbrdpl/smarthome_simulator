package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventListener;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Person implements EventListener {
    private static final Random RANDOM = new Random();

    private final String id;
    private final String name;
    private final Role role;

    private Room location;
    private final PermissionSet permissions;

    public Person(String id, String name, Role role, Room location, PermissionSet permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.location = location;
        this.permissions = permissions;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    public Room getLocation() { return location; }
    public void setLocation(Room location) { this.location = location; }

    public PermissionSet getPermissions() { return permissions; }

    @Override
    public void onEvent(Event e) {
        System.out.println("[" + role + "] received event: " + e.getType());
    }

    public void performStep(SmartHomeContext ctx) {

        if (this.role == Role.CAT) {
            return;
        }


        List<Device> allDevices = new ArrayList<>();
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                allDevices.addAll(r.getDevices());
            }
        }
        if (allDevices.isEmpty()) return;

        Device target = allDevices.get(RANDOM.nextInt(allDevices.size()));
        Room targetRoom = target.getLocation();


        if (this.location != targetRoom) {
            this.location = targetRoom;
        }


        boolean on = RANDOM.nextBoolean();
        if (on) {
            target.turnOn();
            ctx.getActivityLog().add(new ActivityEntry(this.id, this.name, "TURN_ON", target.getName(), LocalDateTime.now()));
        } else {
            target.turnOff();
            ctx.getActivityLog().add(new ActivityEntry(this.id, this.name, "TURN_OFF", target.getName(), LocalDateTime.now()));
        }

    }
}
