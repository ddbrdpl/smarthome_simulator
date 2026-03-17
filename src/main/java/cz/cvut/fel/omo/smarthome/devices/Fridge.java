package cz.cvut.fel.omo.smarthome.devices;

import cz.cvut.fel.omo.smarthome.house.Room;

public class Fridge extends Device {

    private int foodCount;


    public Fridge(String id, String name, Room location) {
        super(id, name, DeviceType.FRIDGE, location);
        this.foodCount = 0;
    }

    int addFood(int amount) {
        foodCount += amount;
        return amount;
    }

    public boolean takeFood(int amount) {
        if (foodCount >= amount) {
            foodCount -= amount;
            return true;
        }
        return false;
    }


}
