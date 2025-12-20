package cz.cvut.fel.omo.smarthome.consumption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConsumptionLog {

    private final Map<String, ConsumptionRecord> recordsByDeviceId = new HashMap<>();

    public void addUsage(String deviceId, String deviceName, double addPowerKWh, double addWaterL, double addGasM3) {
        ConsumptionRecord r = recordsByDeviceId.computeIfAbsent(deviceId, id -> new ConsumptionRecord(deviceId, deviceName));
        r.add(addPowerKWh, addWaterL, addGasM3);
    }

    public Map<String, ConsumptionRecord> getRecordsByDeviceId() {
        return Collections.unmodifiableMap(recordsByDeviceId);
    }

    public void clear() { recordsByDeviceId.clear(); }
}
