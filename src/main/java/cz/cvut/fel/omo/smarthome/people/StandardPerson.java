package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

public class StandardPerson extends Person {

    public StandardPerson(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        if (role == Role.CAT) return;

        // 1. Check Desires (Wishlist)
        checkAndBuyDesires(ctx);

        // 2. Maybe random shop (10% chance)
        tryGeneralShop(ctx, 10);

        // 3. Interact with devices
        interactWithDevice(ctx);
    }
}