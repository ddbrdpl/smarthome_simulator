package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.Fridge;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

public class Mother extends Person {

    private static final int FOOD_PER_MEAL    = 3;
    private static final int COOKING_CHANCE   = 20;  // 20% per tick when cooldown is 0
    private static final int COOKING_COOLDOWN = 8;   // ticks between meals (~2 hours)

    private int cookingCooldown = 0;

    public Mother(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        // Tick down cooldown
        if (cookingCooldown > 0) {
            cookingCooldown--;
        }

        // Try to cook only when cooldown is up
        if (cookingCooldown == 0 && RANDOM.nextInt(100) < COOKING_CHANCE) {
            if (tryCook(ctx)) {
                cookingCooldown = COOKING_COOLDOWN;
                return;
            }
        }

        // Otherwise standard behaviour
        super.performDeviceLogic(ctx);
    }

    private boolean tryCook(SmartHomeContext ctx) {
        // Find fridge anywhere in the house
        Fridge fridge = findFridge(ctx);
        if (fridge == null) {
            System.out.println(" [" + name + "] Wanted to cook but no fridge found!");
            return false;
        }

        // Go to fridge location
        if (this.location != fridge.getLocation()) {
            moveTo(fridge.getLocation());
        }

        // Try to take food
        if (fridge.takeFood(FOOD_PER_MEAL)) {
            logActivity(ctx, "COOKING", "Used " + FOOD_PER_MEAL + " food from " + fridge.getName()
                    + " (left: " + fridge.getFoodCount() + ")");
            System.out.println(" [" + name + "] Cooking! Food left in fridge: " + fridge.getFoodCount());

            // Also turn on kitchen light if it's off
            turnOnKitchenLight(ctx);
            return true;
        } else {
            logActivity(ctx, "FRIDGE_EMPTY", fridge.getName());
            System.out.println(" [" + name + "] Fridge is empty! Need to restock.");
            return false;
        }
    }

    private Fridge findFridge(SmartHomeContext ctx) {
        for (Device d : ctx.getAllDevices()) {
            if (d instanceof Fridge f) return f;
        }
        return null;
    }

    private void turnOnKitchenLight(SmartHomeContext ctx) {
        for (Device d : ctx.getAllDevices()) {
            if (d.getType() == DeviceType.SMART_LIGHT
                    && d.getLocation().getType().name().equals("KITCHEN")
                    && d.isOff()) {
                d.turnOn();
                d.markUsedBy(this);
                logActivity(ctx, "TURN_ON", d.getName());
                break;
            }
        }
    }
}