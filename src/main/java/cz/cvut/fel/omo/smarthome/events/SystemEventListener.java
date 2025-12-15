package cz.cvut.fel.omo.smarthome.events;

public class SystemEventListener implements EventListener {
    private final EventHandler root;

    public SystemEventListener(EventHandler root) {
        this.root = root;
    }

    @Override
    public void onEvent(Event e) {
        root.handle(e);
    }
}
