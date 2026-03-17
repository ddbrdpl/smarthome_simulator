package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

public class Son extends Person {

    public Son(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        // 1. EXTRA CAPABILITY: Watch TV Priority
        // He tries to find TV before doing standard stuff
        Device tv = findDeviceByType(ctx, DeviceType.SMART_TV);

        if (tv != null && RANDOM.nextInt(100) < 50) {
            // If TV exists, 50% chance he ignores everything else and watches it
            if (this.location != tv.getLocation()) {
                moveTo(tv.getLocation());
            }
            if (tv.isOff()) {
                tv.turnOn();
                tv.markUsedBy(this);
                logActivity(ctx, "TURN_ON", tv.getName());
            }
            return; // Skip standard behavior
        }

        // 2. If not watching TV -> Standard Behavior
        // (This includes checking desires, so if TV is missing, he will ask to buy it here)
        super.performDeviceLogic(ctx);
    }
}