package cz.cvut.fel.omo.smarthome.consumption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConsumptionLog {
    private final Map<String, ConsumptionRecord> records = new HashMap<>();

    public void addUsage(String deviceId, String deviceName, double kwh, double water, double gas) {
        records.computeIfAbsent(deviceId, id -> new ConsumptionRecord(id, deviceName))
                .add(kwh, water, gas);
    }

    public Map<String, ConsumptionRecord> getRecordsByDeviceId() {
        return Collections.unmodifiableMap(records);
    }

    public void clear() {
        records.clear();
    }
}