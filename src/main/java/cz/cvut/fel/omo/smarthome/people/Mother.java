package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.devices.DeviceType;
import cz.cvut.fel.omo.smarthome.devices.Fridge;
import cz.cvut.fel.omo.smarthome.devices.FoodType;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.simulation.MealTime;

import java.util.Map;

public class Mother extends Person {

    private MealTime lastCookedMeal = null; // avoid cooking same meal twice

    public Mother(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        MealTime current = MealTime.current(ctx.getCurrentTime());

        // Only cook during mealtime and only once per meal
        if (current != MealTime.NONE && current != lastCookedMeal) {
            if (tryCook(ctx, current)) {
                lastCookedMeal = current;
                return;
            }
        }

        super.performDeviceLogic(ctx);
    }

    private boolean tryCook(SmartHomeContext ctx, MealTime meal) {
        Fridge fridge = findFridge(ctx);
        if (fridge == null) {
            System.out.println(" [" + name + "] No fridge found!");
            return false;
        }

        Map<FoodType, Integer> cost = meal.getCost();

        if (this.location != fridge.getLocation()) {
            moveTo(fridge.getLocation());
        }

        if (fridge.takeFood(cost)) {
            String ingredients = formatCost(cost);
            logActivity(ctx, "COOKING",
                    meal.getDisplayName() + " (" + ingredients + ") — fridge: " + fridge.getFoodCount() + " total");
            System.out.println(" [" + name + "] Cooking " + meal.getDisplayName()
                    + "! Fridge total left: " + fridge.getFoodCount());
            turnOnKitchenLight(ctx);
            return true;
        } else {
            logActivity(ctx, "NO_INGREDIENTS", meal.getDisplayName() + " — waiting for restock");
            System.out.println(" [" + name + "] Not enough ingredients for " + meal.getDisplayName());
            return false;
        }
    }

    private String formatCost(Map<FoodType, Integer> cost) {
        StringBuilder sb = new StringBuilder();
        cost.forEach((type, amt) -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append(type.name().toLowerCase()).append(" x").append(amt);
        });
        return sb.toString();
    }

    public Fridge findFridge(SmartHomeContext ctx) {
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