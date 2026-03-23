package cz.cvut.fel.omo.smarthome.shop;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.util.UUID;

public class AutoBuyer {

    private static final int MAX_DEVICES_PER_TYPE = 10;

    // These are large/expensive devices — impulse buying is blocked for them,
    // and even desire-based buying is capped at 1 per house.
    private static final java.util.Set<DeviceType> UNIQUE_DEVICES = java.util.EnumSet.of(
            DeviceType.FRIDGE,
            DeviceType.HUMIDIFIER_AC,
            DeviceType.SMART_WASHING_MACHINE,
            DeviceType.THERMOSTAT,
            DeviceType.IRRIGATION_SYSTEM,
            DeviceType.SMART_LOCK
    );

    public void buyDevice(ShopContext ctx, DeviceType type, String requester) {
        int count = countDevices(ctx, type);

        // Unique devices can only exist once in the house
        int limit = UNIQUE_DEVICES.contains(type) ? 1 : MAX_DEVICES_PER_TYPE;

        if (count >= limit) {
            System.out.println(" [SHOP] Wanted " + type + " but limit reached (" + count + "). Skipped.");
            return;
        }

        Room targetRoom = findBestRoom(ctx, type);
        if (targetRoom == null) targetRoom = ctx.getFloors().get(0).getRooms().get(0);

        // Clean name: "Smart TV #2" instead of "SMART_TV #2 (NEW)"
        String typeName = type.toString()
                .replace("_", " ")
                .toLowerCase()
                .substring(0, 1).toUpperCase()
                + type.toString().replace("_", " ").toLowerCase().substring(1);
        String name = count == 0 ? typeName : typeName + " #" + (count + 1);

        Device device = new GenericDevice("buy-" + UUID.randomUUID(), name, type, targetRoom);
        device.connectEventBus(ctx.getEventBus());
        targetRoom.addDevice(device);

        String logTarget = name + " → " + targetRoom.getName() + " (for " + requester + ")";

        ctx.getActivityLog().add(new ActivityEntry(
                "SYSTEM",
                "AutoBuyer",
                "BOUGHT_DEVICE",
                logTarget,
                ctx.getCurrentTime()
        ));

        System.out.println(" [SHOP] Bought " + name + " for " + requester);
    }

    // Only these types can be bought on impulse — cheap, small, non-critical
    private static final DeviceType[] IMPULSE_TYPES = {
            DeviceType.SMART_LIGHT,
            DeviceType.GROUP_LIGHT,
            DeviceType.SMART_TV,
            DeviceType.MULTIROOM_AUDIO,
            DeviceType.SMART_MIRROR,
            DeviceType.OUTDOOR_CAMERA,
            DeviceType.MOTION_SENSOR,
            DeviceType.DOOR_WINDOW_SENSOR,
            DeviceType.SMOKE_GAS_SENSOR,
            DeviceType.WATER_LEAK_SENSOR,
            DeviceType.PET_FEEDER,
            DeviceType.AIR_QUALITY_SENSOR
    };

    public void buyImpulseDevice(ShopContext ctx, String requester) {
        DeviceType type = IMPULSE_TYPES[new java.util.Random().nextInt(IMPULSE_TYPES.length)];
        buyDevice(ctx, type, "Impulse by " + requester);
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

        // First pass — exact match
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                if (r.getName().toLowerCase().contains(target)) return r;
            }
        }

        // Fallback — first room
        if (!ctx.getFloors().isEmpty() && !ctx.getFloors().get(0).getRooms().isEmpty()) {
            return ctx.getFloors().get(0).getRooms().get(0);
        }
        return null;
    }
}