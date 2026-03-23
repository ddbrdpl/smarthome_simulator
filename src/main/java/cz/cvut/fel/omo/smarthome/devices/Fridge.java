package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Fridge extends Device {

    // Maximum stock per food type
    public static final Map<FoodType, Integer> MAX_STOCK;
    static {
        Map<FoodType, Integer> m = new EnumMap<>(FoodType.class);
        m.put(FoodType.EGGS,    8);
        m.put(FoodType.OATMEAL, 6);
        m.put(FoodType.SOUP,    4);
        m.put(FoodType.STEAK,   4);
        MAX_STOCK = Collections.unmodifiableMap(m);
    }

    private final Map<FoodType, Integer> stock = new EnumMap<>(FoodType.class);

    public Fridge(String id, String name, Room location) {
        super(id, name, DeviceType.FRIDGE, location);
        restockFull();
    }

    // Fill everything to maximum
    public void restockFull() {
        stock.putAll(MAX_STOCK);
    }

    // Try to take food for a meal — returns true if all ingredients available
    public boolean takeFood(Map<FoodType, Integer> cost) {
        // Check first
        for (Map.Entry<FoodType, Integer> e : cost.entrySet()) {
            if (stock.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
        }
        // Then consume
        for (Map.Entry<FoodType, Integer> e : cost.entrySet()) {
            stock.merge(e.getKey(), -e.getValue(), Integer::sum);
        }
        return true;
    }

    // Check if fridge has enough for a given meal cost
    public boolean hasEnoughFor(Map<FoodType, Integer> cost) {
        for (Map.Entry<FoodType, Integer> e : cost.entrySet()) {
            if (stock.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
        }
        return true;
    }

    public int getStock(FoodType type) {
        return stock.getOrDefault(type, 0);
    }

    public Map<FoodType, Integer> getAllStock() {
        return Collections.unmodifiableMap(stock);
    }

    // Legacy support — total food count
    public int getFoodCount() {
        return stock.values().stream().mapToInt(Integer::intValue).sum();
    }
}