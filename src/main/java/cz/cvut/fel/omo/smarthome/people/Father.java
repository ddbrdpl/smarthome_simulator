package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import java.util.List;

public class Father extends StandardPerson {

    public Father(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        // 1. EXTRA CAPABILITY: Repair
        if (tryRepair(ctx)) {
            return; // If repaired, he is busy, skip standard stuff
        }

        // 2. Standard Behavior (Desires, Shopping, Interaction)
        super.performStep(ctx);
    }

    private boolean tryRepair(SmartHomeContext ctx) {
        List<Device> allDevices = collectAllDevices(ctx);
        for (Device d : allDevices) {
            if ("BROKEN".equals(d.getStateName())) {
                if (this.location != d.getLocation()) this.location = d.getLocation();
                d.repair();

                Event fixed = new Event(EventType.DEVICE_REPAIRED, d, this);
                fixed.setHandledBy(this.name);
                d.publishEvent(fixed);

                logActivity(ctx, "REPAIRED", d.getName());
                return true;
            }
        }
        return false;
    }
}