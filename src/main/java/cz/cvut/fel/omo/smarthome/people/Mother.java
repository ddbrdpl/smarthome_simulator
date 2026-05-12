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
    private int lastCookedDay = -1;         // day of year when last meal was cooked

    public Mother(String id, String name, Role role, Room location, PermissionSet permissions) {
        super(id, name, role, location, permissions);
    }

    @Override
    public void performStep(SmartHomeContext ctx) {
        // Cooking has priority over sport AND rest
        // but only if not critically tired (energy > 20)
        if (energy > 20) {
            int today = ctx.getCurrentTime().getDayOfYear();
            if (today != lastCookedDay) {
                lastCookedMeal = null;
                lastCookedDay  = today;
            }
            MealTime current = MealTime.current(ctx.getCurrentTime());
            if (current != MealTime.NONE && current != lastCookedMeal) {
                if (tryCook(ctx, current)) {
                    lastCookedMeal = current;
                    return;
                }
            }
        }
        super.performStep(ctx);
    }

    @Override
    public void performDeviceLogic(SmartHomeContext ctx) {
        // Sunny weather → water the plants
        if (ctx.getWeatherService().getCurrent() == cz.cvut.fel.omo.smarthome.simulation.Weather.SUNNY) {
            if (RANDOM.nextInt(100) < 25 && tryWaterPlants(ctx)) return;
        }

        super.performDeviceLogic(ctx);
    }

    private boolean tryWaterPlants(SmartHomeContext ctx) {
        for (Device d : ctx.getAllDevices()) {
            if (d.getType() == DeviceType.IRRIGATION_SYSTEM && d.isOff()) {
                moveTo(d.getLocation());
                d.turnOn();
                d.markUsedBy(this);
                logActivity(ctx, "WATERING_PLANTS",
                        d.getName() + " (sunny day)");
                System.out.println(" [" + name + "] Watering plants — sunny day!");
                return true;
            }
        }
        return false;
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