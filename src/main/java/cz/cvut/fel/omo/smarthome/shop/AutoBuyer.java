package cz.cvut.fel.omo.smarthome.shop;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.GenericDevice;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;

import java.time.LocalDateTime;
import java.util.UUID;

public class AutoBuyer {

    // Buy a device only if it's completely missing from the house
    public void buyDevice(SmartHomeContext ctx, DeviceType type) {
        int count = countDevices(ctx, type);

        if (count >= 10) {
            System.out.println(" [SHOP] Wanted " + type + " but we have " + count + ". Skipped.");
            return;
        }

        Room targetRoom = findBestRoom(ctx, type);
        if (targetRoom == null) return;

        String name = type.toString() + " #" + (count + 1) + " (NEW)";
        Device device = new GenericDevice("buy-" + UUID.randomUUID(), name, type, targetRoom);

        device.connectEventBus(ctx.getEventBus());
        targetRoom.addDevice(device);

        // LOG
        ctx.getActivityLog().add(new ActivityEntry(
                "SYSTEM", "AutoBuyer", "BOUGHT_DEVICE", device.getName(),
                ctx.getCurrentTime()
        ));

        System.out.println(" [SHOP] $$$ BOUGHT: " + name);
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

    private Room findBestRoom(SmartHomeContext ctx, DeviceType type) {
        // Simple heuristic: try to find a room that matches the device context
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                String rn = r.getName().toLowerCase();

                if (type == DeviceType.OUTDOOR_CAMERA && rn.contains("garden")) return r;
                if (type == DeviceType.SMART_LOCK && rn.contains("hall")) return r;
                if (type == DeviceType.SMOKE_GAS_SENSOR && rn.contains("kitchen")) return r;
                if (type == DeviceType.WATER_LEAK_SENSOR && rn.contains("bath")) return r;

                // For others, return the first found room (e.g. Living Room)
                if (rn.contains("living")) return r;
            }
        }
        // Fallback: first room of first floor
        if (!ctx.getFloors().isEmpty() && !ctx.getFloors().get(0).getRooms().isEmpty()) {
            return ctx.getFloors().get(0).getRooms().get(0);
        }
        return null;
    }
}