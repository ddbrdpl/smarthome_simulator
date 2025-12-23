package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.devices.Device;

public class FatherHandler implements EventHandler {

    private EventHandler next;

    @Override
    public boolean handle(Event event) {

        if (event.getType() == EventType.WATER_LEAK) {
            System.out.println("[FATHER] handled WATER_LEAK");
            event.setHandledBy("FATHER");
            return true;
        }

        if (event.getType() == EventType.DEVICE_BROKEN) {
            System.out.println("[FATHER] handled " + event.getType());

            if (event.getSource() instanceof Device d) {
                d.repair();
                System.out.println("[FATHER] repaired device: " + d.getName());

                // publish DEVICE_REPAIRED and mark who handled it
                Event repaired = new Event(EventType.DEVICE_REPAIRED, d, null);
                repaired.setHandledBy("FATHER");
                d.publishEvent(repaired);
            }

            event.setHandledBy("FATHER");
            return true;
        }

        return next != null && next.handle(event);
    }

    @Override
    public void setNext(EventHandler next) {
        this.next = next;
    }
}
