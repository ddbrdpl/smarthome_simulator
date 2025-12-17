package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.house.Floor;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.sports.SportEquipment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HouseConfigurationReportGenerator {
    private final SmartHomeContext ctx;

    public HouseConfigurationReportGenerator(SmartHomeContext ctx) {
        this.ctx = ctx;
    }

    public void generate(String outputPath) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== HOUSE CONFIGURATION REPORT ===\n\n");

        for (Floor f : ctx.getFloors()) {
            sb.append("Floor: ").append(f.getName()).append(" (level ").append(f.getLevel()).append(")\n");
            for (Room r : f.getRooms()) {
                sb.append("  Room: ").append(r.getName()).append(" [").append(r.getType()).append("]\n");

                sb.append("    Devices:\n");
                for (Device d : r.getDevices()) {
                    sb.append("      - ").append(d.getName())
                            .append(" (").append(d.getType()).append(")\n");
                }

                sb.append("    Sports:\n");
                for (SportEquipment s : r.getSportEquipment()) {
                    sb.append("      - ").append(s.getType()).append("\n");
                }

                sb.append("    Persons:\n");
                for (Person p : r.getPersonsPresent()) {
                    sb.append("      - ").append(p.getName())
                            .append(" (").append(p.getRole()).append(")\n");
                }
            }
            sb.append("\n");
        }

        try {
            Path p = Path.of(outputPath);
            Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report: " + outputPath, e);
        }
    }
}
