package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
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

        // ------- ORIGINAL: collect all devices -------
        List<Device> allDevices = new ArrayList<>();
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                allDevices.addAll(r.getDevices());
            }
        }

        // ------- NEW: sometimes "want a specific device type" -> buy if missing -------
        // Does NOT break original behavior; it only adds devices before picking random target.
        if (RANDOM.nextInt(100) < 15) { // ~15% of steps try "demand-based purchase"
            DeviceType desiredType = DeviceType.values()[RANDOM.nextInt(DeviceType.values().length)];

            if (!existsDeviceType(allDevices, desiredType)) {
                // buy ONLY when someone needs it
                ctx.getAutoBuyer().buyDevice(ctx, desiredType);

                // refresh list after purchase
                allDevices.clear();
                for (Floor f : ctx.getFloors()) {
                    for (Room r : f.getRooms()) {
                        allDevices.addAll(r.getDevices());
                    }
                }
            }
        }

        // ------- ORIGINAL: if still empty -> return -------
        if (allDevices.isEmpty()) return;

        // ------- ORIGINAL: choose random device -------
        Device target = allDevices.get(RANDOM.nextInt(allDevices.size()));
        Room targetRoom = target.getLocation();

        if (this.location != targetRoom) {
            this.location = targetRoom;
        }

        boolean on = RANDOM.nextBoolean();
        DeviceAction action = on ? DeviceAction.TURN_ON : DeviceAction.TURN_OFF;

        if (!permissions.canPerform(this, target, action)) {
            ctx.getActivityLog().add(new ActivityEntry(
                    this.id,
                    this.name,
                    "DENIED_" + action,
                    target.getName(),
                    LocalDateTime.now()
            ));
            return;
        }

        // ORIGINAL (your "NEW"): remember who interacted with the device
        target.markUsedBy(this);

        if (on) {
            target.turnOn();
            ctx.getActivityLog().add(new ActivityEntry(this.id, this.name, "TURN_ON", target.getName(), LocalDateTime.now()));
        } else {
            target.turnOff();
            ctx.getActivityLog().add(new ActivityEntry(this.id, this.name, "TURN_OFF", target.getName(), LocalDateTime.now()));
        }
    }

    private boolean existsDeviceType(List<Device> devices, DeviceType type) {
        for (Device d : devices) {
            if (d.getType() == type) return true;
        }
        return false;
    }
}
