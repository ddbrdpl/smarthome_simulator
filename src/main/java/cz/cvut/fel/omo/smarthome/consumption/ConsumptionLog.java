package cz.cvut.fel.omo.smarthome.consumption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects aggregated consumption data for all devices in the smart home.
 *
 * <p>The log stores {@link ConsumptionRecord} instances indexed by device ID.
 * Each record accumulates electricity, water, and gas usage over time.</p>
 *
 * <p>This class is updated during each simulation step
 * by {@code Device.accumulateConsumption()}.</p>
 */
public class ConsumptionLog {

    /** Consumption records indexed by device identifier. */
    private final Map<String, ConsumptionRecord> recordsByDeviceId = new HashMap<>();

    /**
     * Adds resource usage for a specific device.
     *
     * <p>If the device does not yet have a consumption record,
     * a new one is created automatically.</p>
     *
     * @param deviceId      unique identifier of the device
     * @param deviceName    human-readable device name
     * @param addPowerKWh   electricity consumption to add (kWh)
     * @param addWaterL     water consumption to add (liters)
     * @param addGasM3      gas consumption to add (cubic meters)
     */
    public void addUsage(
            String deviceId,
            String deviceName,
            double addPowerKWh,
            double addWaterL,
            double addGasM3
    ) {
        ConsumptionRecord r = recordsByDeviceId.computeIfAbsent(
                deviceId,
                id -> new ConsumptionRecord(deviceId, deviceName)
        );
        r.add(addPowerKWh, addWaterL, addGasM3);
    }

    /**
     * Returns an unmodifiable map of consumption records indexed by device ID.
     *
     * @return read-only map of device consumption records
     */
    public Map<String, ConsumptionRecord> getRecordsByDeviceId() {
        return Collections.unmodifiableMap(recordsByDeviceId);
    }

    /**
     * Clears all recorded consumption data.
     *
     * <p>Used when reinitializing the simulation context.</p>
     */
    public void clear() {
        recordsByDeviceId.clear();
    }
}
