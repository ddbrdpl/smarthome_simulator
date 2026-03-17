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

    public void buyDevice(ShopContext ctx, DeviceType type, String requester) {
        int count = countDevices(ctx, type);

        if (count >= MAX_DEVICES_PER_TYPE) {
            System.out.println(" [SHOP] Wanted " + type + " but we have " + count + ". Skipped.");
            return;
        }

        Room targetRoom = findBestRoom(ctx, type);
        if (targetRoom == null) targetRoom = ctx.getFloors().get(0).getRooms().get(0);

        String name = type.toString() + " #" + (count + 1) + " (NEW)";

        Device device = new GenericDevice("buy-" + UUID.randomUUID(), name, type, targetRoom);
        device.connectEventBus(ctx.getEventBus());
        targetRoom.addDevice(device);

        String logTarget = device.getName() + " (for " + requester + ")";

        ctx.getActivityLog().add(new ActivityEntry(
                "SYSTEM",
                "AutoBuyer",
                "BOUGHT_DEVICE",
                logTarget,
                ctx.getCurrentTime()
        ));

        System.out.println(" [SHOP] Bought " + name + " for " + requester);
    }

    private int countDevices(ShopContext ctx, DeviceType type) {
        int count = 0;
        for (Device d : ctx.getAllDevices()) {
            if (d.getType() == type) count++;
        }
        return count;
    }

    private Room findBestRoom(ShopContext ctx, DeviceType type) {
        for (Floor f : ctx.getFloors()) {
            for (Room r : f.getRooms()) {
                String rn = r.getName().toLowerCase();

                if (type == DeviceType.OUTDOOR_CAMERA && rn.contains("garden")) return r;
                if (type == DeviceType.SMART_LOCK && rn.contains("hall")) return r;
                if (type == DeviceType.SMOKE_GAS_SENSOR && rn.contains("kitchen")) return r;
                if (type == DeviceType.WATER_LEAK_SENSOR && rn.contains("bath")) return r;

                if (rn.contains("living")) return r;
            }
        }
        if (!ctx.getFloors().isEmpty() && !ctx.getFloors().get(0).getRooms().isEmpty()) {
            return ctx.getFloors().get(0).getRooms().get(0);
        }
        return null;
    }
}