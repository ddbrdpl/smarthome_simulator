package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.EventEntry;

import java.time.LocalDateTime;

public class SystemEventListener implements EventListener {

    private final EventHandler chain;

    public SystemEventListener(EventHandler chain) {
        this.chain = chain;
    }

    @Override
    public void onEvent(Event e) {
        boolean handled = chain.handle(e);

        SmartHomeContext ctx = SmartHomeContext.getInstance();

        if (handled) {
            ctx.getEventLog().add(
                    new EventEntry(e.getType(), e.getHandledBy(), LocalDateTime.now())
            );
        } else {
            ctx.getEventLog().add(
                    new EventEntry(e.getType(), "SYSTEM (unhandled)", LocalDateTime.now())
            );
        }
    }
}
