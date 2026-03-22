package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.people.DeviceAction;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventListener;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Person implements EventListener {

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

    protected void moveTo(Room target) {
        if (this.location == target) return;
        this.location.removePerson(this);
        this.location = target;
        this.location.addPerson(this);
    }

    protected void performDeviceLogic(SmartHomeContext ctx) {
        // 1. Check Desires (Wishlist)
        checkAndBuyDesires(ctx);

        // 2. Maybe random shop (10% chance)
        tryGeneralShop(ctx, 10);

        // 3. Interact with devices
        interactWithDevice(ctx);
    }

    private boolean tryFindAndUseSport(SmartHomeContext ctx) {
        List<SportEquipment> allSports = ctx.getAllSportEquipment();

        if (allSports.isEmpty()) return false;

        for (SportEquipment s : allSports) {
            if (s.isFree()) {
                if (this.location != s.getLocation()) {
                    moveTo(s.getLocation());
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
        // 70% — prefer devices in current room, 30% — go elsewhere
        List<Device> candidates = (!location.getDevices().isEmpty() && RANDOM.nextInt(100) < 70)
                ? new ArrayList<>(location.getDevices())
                : collectAllDevices(ctx);

        if (candidates.isEmpty()) candidates = collectAllDevices(ctx);
        if (candidates.isEmpty()) return;

        Device target = null;
        for (int i = 0; i < 5; i++) {
            Device candidate = candidates.get(RANDOM.nextInt(candidates.size()));
            if (isInteractive(candidate.getType())) {
                target = candidate;
                break;
            }
        }
        if (target == null) return;

        if (this.location != target.getLocation()) {
            moveTo(target.getLocation());
        }

        if (target.isBroken()) return;

        boolean turnOn = RANDOM.nextBoolean();
        if (turnOn && target.isOn()) return;
        if (!turnOn && target.isOff()) return;

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
            ctx.getAutoBuyer().buyImpulseDevice(ctx, name);
        }
    }

    // --- HELPERS ---

    @Override
    public void onEvent(Event e) {}

    protected List<Device> collectAllDevices(SmartHomeContext ctx) {
        return ctx.getAllDevices();
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