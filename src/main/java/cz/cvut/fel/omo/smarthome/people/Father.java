package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.FoodType;
import cz.cvut.fel.omo.smarthome.devices.Fridge;
import cz.cvut.fel.omo.smarthome.events.Event;
import cz.cvut.fel.omo.smarthome.events.EventType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.simulation.MealTime;

import java.util.List;

public class Father extends Person {

    private static final int SHOPPING_DURATION = 4;

    private boolean isShopping    = false;
    private int     shoppingTicks = 0;
    private Room    homeRoom;

    public Father(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
        this.homeRoom = location;
    }

    @Override
    public void performStep(SmartHomeContext ctx) {
        if (isShopping) {
            shoppingTicks--;
            if (shoppingTicks <= 0) returnFromShopping(ctx);
            return;
        }

        // Check fridge BEFORE sport — shopping has highest priority
        MealTime upcoming = MealTime.checkTime(ctx.getCurrentTime());
        if (upcoming != MealTime.NONE) {
            Fridge fridge = findFridge(ctx);
            if (fridge != null && !fridge.hasEnoughFor(upcoming.getCost())) {
                goShopping(ctx);
                return;
            }
        }

        super.performStep(ctx);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        // 1. Repair broken devices
        if (tryRepair(ctx)) return;

        // 2. Standard behaviour
        super.performDeviceLogic(ctx);
    }

    private void goShopping(SmartHomeContext ctx) {
        isShopping    = true;
        shoppingTicks = SHOPPING_DURATION;
        if (this.location != null) {
            homeRoom = this.location;
            this.location.removePerson(this);
            this.location = null;
        }
        logActivity(ctx, "SHOPPING", "Left for groceries (" + SHOPPING_DURATION + " ticks ~1h)");
        System.out.println(" [" + name + "] Going shopping!");
    }

    private void returnFromShopping(SmartHomeContext ctx) {
        isShopping = false;
        Fridge fridge = findFridge(ctx);
        if (fridge != null) {
            fridge.restockFull();
            logActivity(ctx, "RESTOCK",
                    "Fridge restocked — eggs:" + fridge.getStock(FoodType.EGGS)
                            + " oatmeal:" + fridge.getStock(FoodType.OATMEAL)
                            + " soup:" + fridge.getStock(FoodType.SOUP)
                            + " steak:" + fridge.getStock(FoodType.STEAK));
        }
        this.location = homeRoom;
        this.location.addPerson(this);
        logActivity(ctx, "RETURNED", "Back home → " + homeRoom.getName());
        System.out.println(" [" + name + "] Back from shopping! Fridge restocked.");
    }

    private boolean tryRepair(SmartHomeContext ctx) {
        for (Device d : collectAllDevices(ctx)) {
            if (d.isBroken()) {
                if (this.location != d.getLocation()) moveTo(d.getLocation());
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

    private Fridge findFridge(SmartHomeContext ctx) {
        for (Device d : ctx.getAllDevices()) {
            if (d instanceof Fridge f) return f;
        }
        return null;
    }

    public boolean isShopping() { return isShopping; }
}