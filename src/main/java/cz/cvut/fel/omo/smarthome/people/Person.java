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
    private final PermissionSet permissions;

    private Room location;

    public Person(String id, String name, Role role, Room location, PermissionSet permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.location = location;
        this.permissions = permissions;
    }

    @Override
    public void onEvent(Event e) {
        // Simple console reaction
        System.out.println(" [" + role + " " + name + "] saw event: " + e.getType());
    }

    public void performStep(SmartHomeContext ctx) {
        if (role == Role.CAT) return;

        // 1. Priority for FATHER: Check for broken devices and repair them
        if (role == Role.FATHER) {
            if (tryRepair(ctx)) return; // If repaired, skip other actions this turn
        }



        // 3. Interact with devices (Watch TV, Turn on lights, etc.)
        interactWithDevice(ctx);
    }

    /**
     * Finds a broken device, moves to it, and repairs it.
     * @return true if a repair action was performed
     */
    private boolean tryRepair(SmartHomeContext ctx) {
        List<Device> allDevices = collectAllDevices(ctx);

        for (Device d : allDevices) {
            if ("BROKEN".equals(d.getStateName())) {
                // Move to the room
                if (this.location != d.getLocation()) {
                    this.location = d.getLocation();
                }

                // Repair
                d.repair();

                // Create an event about the repair
                Event fixed = new Event(cz.cvut.fel.omo.smarthome.events.EventType.DEVICE_REPAIRED, d, this);
                fixed.setHandledBy(this.name); // Father himself handled it
                d.publishEvent(fixed);

                // Log activity
                ctx.getActivityLog().add(new ActivityEntry(
                        id, name, "REPAIRED", d.getName(),
                        ctx.getCurrentTime()
                ));

                System.out.println(" [" + role + " " + name + "] repaired " + d.getName());
                return true; // Used this turn for repair
            }
        }
        return false;
    }

    private void interactWithDevice(SmartHomeContext ctx) {
        List<Device> allDevices = collectAllDevices(ctx);
        if (allDevices.isEmpty()) return;

        // Try 3 times to find a suitable device (to avoid infinite loops if only sensors exist)
        Device target = null;
        for (int i = 0; i < 3; i++) {
            Device candidate = allDevices.get(RANDOM.nextInt(allDevices.size()));
            // Only interact with things humans actually touch (Lights, TV, etc.)
            if (isInteractive(candidate.getType())) {
                target = candidate;
                break;
            }
        }

        if (target == null) return; // Didn't find anything interesting to touch

        // Move to device room
        if (this.location != target.getLocation()) {
            this.location = target.getLocation();
        }

        // Decide action
        boolean turnOn = RANDOM.nextBoolean();

        // SMART CHECK: Don't turn ON what is already ON, don't turn OFF what is already OFF
        String currentState = target.getStateName();
        if (turnOn && "ON".equals(currentState)) return;   // Skip
        if (!turnOn && "OFF".equals(currentState)) return; // Skip
        if ("BROKEN".equals(currentState)) return;         // Can't use broken stuff

        DeviceAction action = turnOn ? DeviceAction.TURN_ON : DeviceAction.TURN_OFF;

        // Check Permissions
        if (permissions.canPerform(this, target, action)) {
            if (turnOn) target.turnOn(); else target.turnOff();
            target.markUsedBy(this);
            logActivity(ctx, action.name(), target.getName());
        } else {
            // Log Denial
            logActivity(ctx, "DENIED_" + action, target.getName());
        }
    }

    /**
     * Determines if a device is meant for manual human interaction.
     */
    private boolean isInteractive(DeviceType type) {
        return switch (type) {
            case SMOKE_GAS_SENSOR, WATER_LEAK_SENSOR,
                 MOTION_SENSOR, DOOR_WINDOW_SENSOR,
                 AIR_QUALITY_SENSOR, OUTDOOR_CAMERA -> false;
            default -> true;
        };
    }

    private List<Device> collectAllDevices(SmartHomeContext ctx) {
        List<Device> list = new ArrayList<>();
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                list.addAll(r.getDevices());
            }
        }
        return list;
    }

    private void logActivity(SmartHomeContext ctx, String action, String target) {
        ctx.getActivityLog().add(new ActivityEntry(
                id, name, action, target,
                ctx.getCurrentTime()
        ));
    }
    private void tryShop(SmartHomeContext ctx) {
        // FORCE SHOPPING: 100% chance (всегда пробуем купить)
        if (RANDOM.nextInt(100) < 100) {
            DeviceType[] types = DeviceType.values();
            DeviceType wanted = types[RANDOM.nextInt(types.length)];

            ctx.getAutoBuyer().buyDevice(ctx, wanted);
        }
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public Room getLocation() { return location; }
    public PermissionSet getPermissions() { return permissions; }
}