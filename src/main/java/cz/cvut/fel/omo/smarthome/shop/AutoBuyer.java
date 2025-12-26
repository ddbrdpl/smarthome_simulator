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

/**
 * Automatic purchasing system for smart home devices.
 *
 * <p>The {@code AutoBuyer} supports two different purchasing strategies:</p>
 * <ul>
 *   <li><b>Minimum-based purchase</b> — ensures a minimum number of devices
 *       of selected types exists in the home</li>
 *   <li><b>Demand-based purchase</b> — buys a device only when a resident
 *       attempts to use a device type that does not yet exist</li>
 * </ul>
 *
 * <p>In the final solution, demand-based purchase is preferred and triggered
 * from {@link cz.cvut.fel.omo.smarthome.people.Person#performStep(SmartHomeContext)}.</p>
 */
public class AutoBuyer {

    /**
     * Minimum required number of devices for selected device types.
     *
     * <p>Used only by {@link #checkAndBuyIfNeeded(SmartHomeContext)}.</p>
     */
    private final Map<DeviceType, Integer> minimum = new EnumMap<>(DeviceType.class);

    /**
     * Initializes default minimum device counts.
     */
    public AutoBuyer() {
        minimum.put(DeviceType.SMART_LIGHT, 3);
        minimum.put(DeviceType.SMART_LOCK, 1);
        minimum.put(DeviceType.OUTDOOR_CAMERA, 1);
        minimum.put(DeviceType.SMOKE_GAS_SENSOR, 1);
        minimum.put(DeviceType.WATER_LEAK_SENSOR, 1);
    }

    /**
     * Ensures that the minimum required number of selected devices exists.
     *
     * <p><b>Legacy behavior:</b> This method may purchase devices at simulation start
     * or periodically, regardless of actual demand.</p>
     *
     * <p><b>Note:</b> This method should not be called if only demand-based
     * purchasing is desired.</p>
     *
     * @param ctx smart home context
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

    /**
     * Counts how many devices of a given type currently exist in the home.
     *
     * @param ctx  smart home context
     * @param type device type to count
     * @return number of devices of that type
     */
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
     * Demand-based purchase method.
     *
     * <p>Buys exactly one device of the given type, but only if
     * no such device currently exists in the home.</p>
     *
     * <p>This method is typically triggered when a person attempts
     * to use a device type that is missing.</p>
     *
     * @param ctx  smart home context
     * @param type device type to purchase
     */
    public void buyDevice(SmartHomeContext ctx, DeviceType type) {
        if (countDevices(ctx, type) > 0) return;
        buyOne(ctx, type);
    }

    /**
     * Internal helper that creates, registers and logs a newly purchased device.
     *
     * @param ctx  smart home context
     * @param type device type to purchase
     */
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

    /**
     * Selects a suitable room for a given device type.
     *
     * <p>The selection is based on room name heuristics
     * (e.g., kitchen for smoke sensors, garden for outdoor cameras).</p>
     *
     * @param ctx  smart home context
     * @param type device type
     * @return selected room or {@code null} if none found
     */
    private Room pickRoomForType(SmartHomeContext ctx, DeviceType type) {
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                String rn = r.getName().toLowerCase();

                switch (type) {
                    case OUTDOOR_CAMERA -> { if (rn.contains("garden")) return r; }
                    case SMART_LOCK -> { if (rn.contains("living")) return r; }
                    case SMOKE_GAS_SENSOR -> { if (rn.contains("kitchen")) return r; }
                    case WATER_LEAK_SENSOR -> { if (rn.contains("bath")) return r; }
                    default -> { return r; } // fallback: first available room
                }
            }
        }
        return null;
    }
}
