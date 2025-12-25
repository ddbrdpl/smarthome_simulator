package cz.cvut.fel.omo.smarthome.shop;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class AutoBuyer {

    private final Map<DeviceType, Integer> minimum = new EnumMap<>(DeviceType.class);

    public AutoBuyer() {
        minimum.put(DeviceType.SMART_LIGHT, 3);
        minimum.put(DeviceType.SMART_LOCK, 1);
        minimum.put(DeviceType.OUTDOOR_CAMERA, 1);
        minimum.put(DeviceType.SMOKE_GAS_SENSOR, 1);
        minimum.put(DeviceType.WATER_LEAK_SENSOR, 1);
    }

    /**
     * Old behavior (min counts). Keep it, but DON'T call it from Main if you want "buy only on demand".
     */
    public void checkAndBuyIfNeeded(SmartHomeContext ctx) {
        for (Map.Entry<DeviceType, Integer> rule : minimum.entrySet()) {
            DeviceType type = rule.getKey();
            int minCount = rule.getValue();

            int current = countDevices(ctx, type);
            if (current >= minCount) continue;

            int toBuy = minCount - current;
            for (int i = 0; i < toBuy; i++) {
                buyOne(ctx, type);
            }
        }
    }

    private int countDevices(SmartHomeContext ctx, DeviceType type) {
        int count = 0;
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                for (Device d : r.getDevices()) {
                    if (d.getType() == type) count++;
                }
            }
        }
        return count;
    }

    /**
     * NEW behavior: buy one device only when needed (called from Person).
     * Uses the same logic as buyOne() to avoid inconsistent rooms/names/ids.
     */
    public void buyDevice(SmartHomeContext ctx, DeviceType type) {
        // if already exists -> do nothing (prevents spam buys)
        if (countDevices(ctx, type) > 0) return;
        buyOne(ctx, type);
    }

    private void buyOne(SmartHomeContext ctx, DeviceType type) {
        Room targetRoom = pickRoomForType(ctx, type);
        if (targetRoom == null) return;

        String id = "buy-" + UUID.randomUUID();
        String name = switch (type) {
            case OUTDOOR_CAMERA -> "Outdoor Camera (NEW)";
            case SMART_LIGHT -> "Smart Light (NEW)";
            case SMART_LOCK -> "Smart Lock (NEW)";
            case SMOKE_GAS_SENSOR -> "Smoke/Gas Sensor (NEW)";
            case WATER_LEAK_SENSOR -> "Water Leak Sensor (NEW)";
            default -> "New " + type;
        };

        Device device = new GenericDevice(id, name, type, targetRoom);
        device.connectEventBus(ctx.getEventBus());
        targetRoom.addDevice(device);

        ctx.getActivityLog().add(new ActivityEntry(
                "SYSTEM",
                "SYSTEM",
                "BUY_DEVICE",
                device.getName(),
                LocalDateTime.now()
        ));
    }

    private Room pickRoomForType(SmartHomeContext ctx, DeviceType type) {
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                String rn = r.getName().toLowerCase();

                switch (type) {
                    case OUTDOOR_CAMERA -> { if (rn.contains("garden")) return r; }
                    case SMART_LOCK -> { if (rn.contains("living")) return r; }
                    case SMOKE_GAS_SENSOR -> { if (rn.contains("kitchen")) return r; }
                    case WATER_LEAK_SENSOR -> { if (rn.contains("bath")) return r; }
                    default -> { return r; } // fallback: first room
                }
            }
        }
        return null;
    }
}
