package cz.cvut.fel.omo.smarthome.shop;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AutoBuyer {

    // Per-type purchase limits — how many of each can exist in the house
    private static final Map<DeviceType, Integer> LIMITS = new EnumMap<>(DeviceType.class);
    static {
        // Unique — only one per house
        LIMITS.put(DeviceType.FRIDGE,                 1);
        LIMITS.put(DeviceType.HUMIDIFIER_AC,           1);
        LIMITS.put(DeviceType.SMART_WASHING_MACHINE,   1);
        LIMITS.put(DeviceType.THERMOSTAT,              1);
        LIMITS.put(DeviceType.IRRIGATION_SYSTEM,       1);
        LIMITS.put(DeviceType.SMART_LOCK,              1);
        LIMITS.put(DeviceType.SMART_COFFEE_MACHINE,    1);
        LIMITS.put(DeviceType.PET_FEEDER,              1);

        // Small duplicates allowed — max 2
        LIMITS.put(DeviceType.MULTIROOM_AUDIO,         2);
        LIMITS.put(DeviceType.SMART_TV,                2);
        LIMITS.put(DeviceType.SMART_MIRROR,            2);
        LIMITS.put(DeviceType.OUTDOOR_CAMERA,          2);
        LIMITS.put(DeviceType.MOTION_SENSOR,           2);
        LIMITS.put(DeviceType.DOOR_WINDOW_SENSOR,      2);
        LIMITS.put(DeviceType.SMOKE_GAS_SENSOR,        2);
        LIMITS.put(DeviceType.WATER_LEAK_SENSOR,       2);
        LIMITS.put(DeviceType.AIR_QUALITY_SENSOR,      2);

        // Lights — more allowed
        LIMITS.put(DeviceType.SMART_LIGHT,             6);
        LIMITS.put(DeviceType.GROUP_LIGHT,             4);
        LIMITS.put(DeviceType.GARDEN_LIGHT,            2);
        LIMITS.put(DeviceType.SMART_BLINDS,            3);
    }

    private static final int DEFAULT_LIMIT = 3;

    // Only these types can be bought on impulse
    private static final DeviceType[] IMPULSE_TYPES = {
            DeviceType.SMART_LIGHT,
            DeviceType.SMART_TV,
            DeviceType.MULTIROOM_AUDIO,
            DeviceType.SMART_MIRROR,
            DeviceType.OUTDOOR_CAMERA,
            DeviceType.MOTION_SENSOR,
            DeviceType.DOOR_WINDOW_SENSOR,
            DeviceType.SMOKE_GAS_SENSOR,
            DeviceType.WATER_LEAK_SENSOR,
            DeviceType.AIR_QUALITY_SENSOR
    };

    public void buyDevice(ShopContext ctx, DeviceType type, String requester) {
        int count = countDevices(ctx, type);
        int limit = LIMITS.getOrDefault(type, DEFAULT_LIMIT);

        if (count >= limit) {
            System.out.println(" [SHOP] Wanted " + type
                    + " but limit reached (" + count + "/" + limit + "). Skipped.");
            return;
        }

        Room targetRoom = findBestRoom(ctx, type);
        if (targetRoom == null) targetRoom = ctx.getFloors().get(0).getRooms().get(0);

        String typeName = type.toString().replace("_", " ").toLowerCase();
        typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
        String name = count == 0 ? typeName : typeName + " #" + (count + 1);

        Device device = new GenericDevice("buy-" + UUID.randomUUID(), name, type, targetRoom);
        device.connectEventBus(ctx.getEventBus());
        targetRoom.addDevice(device);

        String logTarget = name + " → " + targetRoom.getName() + " (for " + requester + ")";
        ctx.getActivityLog().add(new ActivityEntry(
                "SYSTEM", "AutoBuyer", "BOUGHT_DEVICE", logTarget, ctx.getCurrentTime()
        ));
        System.out.println(" [SHOP] Bought " + name + " for " + requester);
    }

    public void buyImpulseDevice(ShopContext ctx, String requester) {
        // Try up to 5 times to find a type that hasn't hit its limit
        Random rnd = new Random();
        for (int i = 0; i < 5; i++) {
            DeviceType type = IMPULSE_TYPES[rnd.nextInt(IMPULSE_TYPES.length)];
            int count = countDevices(ctx, type);
            int limit = LIMITS.getOrDefault(type, DEFAULT_LIMIT);
            if (count < limit) {
                buyDevice(ctx, type, "Impulse by " + requester);
                return;
            }
        }
        System.out.println(" [SHOP] Impulse buy skipped — all limits reached.");
    }

    private int countDevices(ShopContext ctx, DeviceType type) {
        int count = 0;
        for (Device d : ctx.getAllDevices()) {
            if (d.getType() == type) count++;
        }
        return count;
    }

    private Room findBestRoom(ShopContext ctx, DeviceType type) {
        String target = switch (type) {
            case SMART_LIGHT, GROUP_LIGHT, SMART_LOCK,
                 DOOR_WINDOW_SENSOR, MULTIROOM_AUDIO,
                 SMART_TV, SMART_BLINDS, THERMOSTAT,
                 HUMIDIFIER_AC, AIR_QUALITY_SENSOR    -> "living";
            case SMOKE_GAS_SENSOR, SMART_COFFEE_MACHINE,
                 PET_FEEDER, FRIDGE                   -> "kitchen";
            case WATER_LEAK_SENSOR, SMART_WASHING_MACHINE,
                 SMART_MIRROR                         -> "bath";
            case OUTDOOR_CAMERA, IRRIGATION_SYSTEM,
                 GARDEN_LIGHT                         -> "garden";
            case MOTION_SENSOR                        -> "garage";
            default                                   -> "living";
        };

        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                if (r.getName().toLowerCase().contains(target)) return r;
            }
        }
        if (!ctx.getFloors().isEmpty() && !ctx.getFloors().get(0).getRooms().isEmpty()) {
            return ctx.getFloors().get(0).getRooms().get(0);
        }
        return null;
    }
}