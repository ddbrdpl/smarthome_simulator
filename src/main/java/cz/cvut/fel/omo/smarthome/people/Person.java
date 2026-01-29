package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.people.DeviceAction;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventListener;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Person implements EventListener {

    protected static final Random RANDOM = new Random();

    protected final String id;
    protected final String name;
    protected final Role role;
    protected final PermissionSet permissions;
    protected Room location;
    protected List<DeviceType> desires = new ArrayList<>();

    protected SportEquipment currentSport = null;

    public Person(String id, String name, Role role, Room location, PermissionSet permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.location = location;
        this.permissions = permissions;
    }

    /**
     * Main step logic.
     */
    public void performStep(SmartHomeContext ctx){
        if (currentSport != null) {
            if (currentSport.isFree() || currentSport.getInUseBy() != this) {
                logActivity(ctx, "FINISHED_SPORT", currentSport.getType().toString());
                currentSport = null; // Стал свободен
            } else {
                return;
            }
        }

        if (RANDOM.nextInt(100) < 30) {
            if (tryFindAndUseSport(ctx)) {
                return;
            }
            System.out.println(" [" + name + "] Wanted sport, but everything is busy. Waiting...");
            return;
        }

        performDeviceLogic(ctx);

    };

    protected abstract void performDeviceLogic(SmartHomeContext ctx);

    private boolean tryFindAndUseSport(SmartHomeContext ctx) {
        List<SportEquipment> allSports = new ArrayList<>();
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                allSports.addAll(r.getSportEquipment());
            }
        }

        if (allSports.isEmpty()) return false;

        for (SportEquipment s : allSports) {
            if (s.isFree()) {
                if (this.location != s.getLocation()) {
                    this.location = s.getLocation();
                }
                int duration = 2 + RANDOM.nextInt(4);
                boolean success = s.tryUse(this, duration);

                if (success) {
                    this.currentSport = s;
                    logActivity(ctx, "STARTED_SPORT", s.getType().toString() + " (" + duration + " steps)");
                    return true;
                }
            }
        }
        return false;
    }

    // --- SHARED TOOLS ---

    protected void checkAndBuyDesires(SmartHomeContext ctx) {
        if (RANDOM.nextInt(100) > 30) return;

        for (DeviceType wantedType : desires) {
            if (findDeviceByType(ctx, wantedType) == null) {
                System.out.println(" [" + name + "] I want " + wantedType + "! Asking to buy...");


                ctx.getAutoBuyer().buyDevice(ctx, wantedType, "Desire of " + name);

                break;
            }
        }
    }

    protected void interactWithDevice(SmartHomeContext ctx) {
        List<Device> allDevices = collectAllDevices(ctx);
        if (allDevices.isEmpty()) return;

        Device target = null;
        for (int i = 0; i < 3; i++) {
            Device candidate = allDevices.get(RANDOM.nextInt(allDevices.size()));
            if (isInteractive(candidate.getType())) {
                target = candidate;
                break;
            }
        }
        if (target == null) return;

        if (this.location != target.getLocation()) {
            this.location = target.getLocation();
        }

        String state = target.getStateName();
        if ("BROKEN".equals(state)) return;

        boolean turnOn = RANDOM.nextBoolean();
        if (turnOn && "ON".equals(state)) return;
        if (!turnOn && "OFF".equals(state)) return;

        DeviceAction action = turnOn ? DeviceAction.TURN_ON : DeviceAction.TURN_OFF;

        if (permissions.canPerform(this, target, action)) {
            if (turnOn) target.turnOn(); else target.turnOff();
            target.markUsedBy(this);
            logActivity(ctx, action.name(), target.getName());
        } else {
            logActivity(ctx, "DENIED_" + action, target.getName());
        }
    }

    protected void tryGeneralShop(SmartHomeContext ctx, int chance) {
        if (RANDOM.nextInt(100) < chance) {
            DeviceType[] types = DeviceType.values();
            DeviceType randomType = types[RANDOM.nextInt(types.length)];

            ctx.getAutoBuyer().buyDevice(ctx, randomType, "Impulse by " + name);
        }
    }

    // --- HELPERS ---

    @Override
    public void onEvent(Event e) {}

    protected List<Device> collectAllDevices(SmartHomeContext ctx) {
        List<Device> list = new ArrayList<>();
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) list.addAll(r.getDevices());
        }
        return list;
    }

    protected Device findDeviceByType(SmartHomeContext ctx, DeviceType type) {
        for (Device d : collectAllDevices(ctx)) {
            if (d.getType() == type) return d;
        }
        return null;
    }

    protected void logActivity(SmartHomeContext ctx, String action, String target) {
        ctx.getActivityLog().add(new ActivityEntry(id, name, action, target, ctx.getCurrentTime()));
    }

    private boolean isInteractive(DeviceType type) {
        return switch (type) {
            case SMOKE_GAS_SENSOR, WATER_LEAK_SENSOR, MOTION_SENSOR,
                 DOOR_WINDOW_SENSOR, AIR_QUALITY_SENSOR, OUTDOOR_CAMERA -> false;
            default -> true;
        };
    }

    public void setDesires(List<DeviceType> desires) { this.desires = desires; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public Room getLocation() { return location; }
    public PermissionSet getPermissions() { return permissions; }
}