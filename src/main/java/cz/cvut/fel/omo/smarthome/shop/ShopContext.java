package cz.cvut.fel.omo.smarthome.shop;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.events.EventBus;
import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ShopContext {
    List<Device> getAllDevices();
    List<Floor> getFloors();
    EventBus getEventBus();
    ActivityLog getActivityLog();
    LocalDateTime getCurrentTime();
}