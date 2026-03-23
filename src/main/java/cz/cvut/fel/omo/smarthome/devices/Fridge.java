package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Fridge extends Device {

    // Maximum stock per food type
    public static final Map<FoodType, Integer> MAX_STOCK;
    // Initial stock (~1.5 days) — enough to trigger Father's shopping trip
    public static final Map<FoodType, Integer> INITIAL_STOCK;

    static {
        Map<FoodType, Integer> max = new EnumMap<>(FoodType.class);
        max.put(FoodType.EGGS,    8);
        max.put(FoodType.OATMEAL, 6);
        max.put(FoodType.SOUP,    4);
        max.put(FoodType.STEAK,   4);
        MAX_STOCK = Collections.unmodifiableMap(max);

        // ~1 day minus one ingredient — Father will shop before dinner
        Map<FoodType, Integer> init = new EnumMap<>(FoodType.class);
        init.put(FoodType.EGGS,    4); // enough for breakfast
        init.put(FoodType.OATMEAL, 2); // enough for breakfast
        init.put(FoodType.SOUP,    2); // enough for lunch
        init.put(FoodType.STEAK,   1); // NOT enough for dinner → Father shops at 17:30
        INITIAL_STOCK = Collections.unmodifiableMap(init);
    }

    private final Map<FoodType, Integer> stock = new EnumMap<>(FoodType.class);

    public Fridge(String id, String name, Room location) {
        super(id, name, DeviceType.FRIDGE, location);
        stock.putAll(INITIAL_STOCK); // start with limited supply
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