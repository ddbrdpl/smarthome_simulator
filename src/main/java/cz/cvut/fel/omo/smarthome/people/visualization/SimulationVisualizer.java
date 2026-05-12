package cz.cvut.fel.omo.smarthome.people.visualization;

import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.house.Room;
import cz.cvut.fel.omo.smarthome.people.Animal;
import cz.cvut.fel.omo.smarthome.people.Person;
import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.simulation.SimulationEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SimulationVisualizer extends JFrame {

    // ── Layout ──────────────────────────────────────────────────────────
    private static final int CELL_W   = 180;
    private static final int CELL_H   = 140;
    private static final int COLS     = 3;
    private static final int ROWS     = 3;
    private static final int PANEL_W  = CELL_W * COLS;
    private static final int PANEL_H  = CELL_H * ROWS;
    private static final int LOG_W    = 300;
    private static final int STATUS_H = 40;

    // Grid positions: row, col (0-based)
    private static final Map<String, int[]> ROOM_GRID = new LinkedHashMap<>();
    static {
        ROOM_GRID.put("Garage",       new int[]{0, 0});
        ROOM_GRID.put("Garden",       new int[]{0, 1});
        ROOM_GRID.put("SonRoom",      new int[]{1, 0});
        ROOM_GRID.put("LivingRoom",   new int[]{1, 1});
        ROOM_GRID.put("Kitchen",      new int[]{1, 2});
        ROOM_GRID.put("DaughterRoom", new int[]{2, 0});
        ROOM_GRID.put("Bathroom",     new int[]{2, 1});
    }

    // Pixel-art palette
    private static final Color COL_BG        = new Color(0x1a1c2c);
    private static final Color COL_ROOM_FILL = new Color(0x29366f);
    private static final Color COL_ROOM_BORD = new Color(0x3b5dc9);
    private static final Color COL_ROOM_NAME = new Color(0x73eff7);
    private static final Color COL_GRID_LINE = new Color(0x3b5dc9);
    private static final Color COL_STATUS_BG = new Color(0x0f1425);
    private static final Color COL_LOG_BG    = new Color(0x0d1117);
    private static final Color COL_LOG_TEXT  = new Color(0x8be9fd);
    private static final Color COL_LOG_TIME  = new Color(0x6272a4);
    private static final Color COL_LOG_HDR   = new Color(0x50fa7b);

    // Role → colour
    private static final Map<String, Color> ROLE_COLOR = new HashMap<>();
    static {
        ROLE_COLOR.put("FATHER",      new Color(0xff5555));
        ROLE_COLOR.put("MOTHER",      new Color(0xff79c6));
        ROLE_COLOR.put("SON",         new Color(0x50fa7b));
        ROLE_COLOR.put("DAUGHTER",    new Color(0xffb86c));
        ROLE_COLOR.put("GRANDFATHER", new Color(0xbd93f9));
        ROLE_COLOR.put("CAT",         new Color(0xf1fa8c));
    }

    // Role → pixel emoji label
    private static final Map<String, String> ROLE_ICON = new HashMap<>();
    static {
        ROLE_ICON.put("FATHER",      "OT");
        ROLE_ICON.put("MOTHER",      "MA");
        ROLE_ICON.put("SON",         "SY");
        ROLE_ICON.put("DAUGHTER",    "DC");
        ROLE_ICON.put("GRANDFATHER", "DE");
        ROLE_ICON.put("CAT",         "KK");
    }

    // ── Simulation state ─────────────────────────────────────────────────
    private final SmartHomeContext ctx;
    private final SimulationEngine engine;
    private final List<ActivityEntry> activityHistory = new ArrayList<>();

    // ── Snapshot system for rewind ────────────────────────────────────────
    private static class Snapshot {
        final Map<String, String> personRooms;   // name → room name
        final Map<String, String> personActions; // name → last action text
        final String simTime;
        final int step;
        final List<String> logLines;

        Snapshot(Map<String, String> rooms, Map<String, String> actions,
                 String time, int step, List<String> log) {
            this.personRooms   = new LinkedHashMap<>(rooms);
            this.personActions = new LinkedHashMap<>(actions);
            this.simTime  = time;
            this.step     = step;
            this.logLines = new ArrayList<>(log);
        }
    }

    private final List<Snapshot>        snapshots    = new ArrayList<>();
    private final Map<String, String>   lastActions  = new LinkedHashMap<>();
    private final List<String>          logLines     = new ArrayList<>();

    // Per-person animated position (pixel coords, centre of sprite)
    private final Map<String, float[]> personPos   = new LinkedHashMap<>(); // name → [x, y]
    private final Map<String, float[]> personTarget = new LinkedHashMap<>();
    private final Map<String, String>  personBubble = new LinkedHashMap<>(); // name → action text
    private final Map<String, Integer> bubbleTimer  = new LinkedHashMap<>(); // name → ticks left

    private int  currentStep = 0;
    private int  totalSteps  = 50;
    private boolean running  = false;
    private Timer  animTimer;
    private Timer  stepTimer;

    // ── UI components ────────────────────────────────────────────────────
    private GamePanel gamePanel;
    private JTextArea logArea;
    private JLabel    stepLabel;
    private JLabel    timeLabel;
    private JButton   btnPlay;
    private JSlider   speedSlider;
    private JLabel    weatherLabel;

    private Runnable  onFinish; // called when simulation ends or Reports button clicked

    // ── Font ─────────────────────────────────────────────────────────────
    private Font pixelFont;
    private Font pixelFontSm;

    // ─────────────────────────────────────────────────────────────────────
    public SimulationVisualizer(SmartHomeContext ctx, int totalSteps, Runnable onFinish) {
        this.ctx        = ctx;
        this.engine     = new SimulationEngine(ctx);
        this.totalSteps = totalSteps;
        this.onFinish   = onFinish;

        loadFont();
        initPersonPositions();
        buildUI();
        setupFridgeClick();
        startAnimLoop();
    }

    // ── Font ─────────────────────────────────────────────────────────────
    private void loadFont() {
        try {
            // Use a monospaced font that looks retro
            pixelFont   = new Font("Monospaced", Font.BOLD, 13);
            pixelFontSm = new Font("Monospaced", Font.PLAIN, 11);
        } catch (Exception e) {
            pixelFont   = new Font(Font.MONOSPACED, Font.BOLD, 13);
            pixelFontSm = new Font(Font.MONOSPACED, Font.PLAIN, 11);
        }
    }

    // ── Init positions ────────────────────────────────────────────────────
    private void initPersonPositions() {
        for (Person p : ctx.getResidents()) {
            float[] pos = roomCentre(p.getLocation().getName());
            personPos.put(p.getName(),    pos.clone());
            personTarget.put(p.getName(), pos.clone());
            personBubble.put(p.getName(), "");
            bubbleTimer.put(p.getName(),  0);
        }
        // Animals
        for (var a : ctx.getAnimals()) {
            float[] pos = roomCentre(a.getLocation().getName());
            personPos.put(a.getName(),    pos.clone());
            personTarget.put(a.getName(), pos.clone());
            personBubble.put(a.getName(), "");
            bubbleTimer.put(a.getName(),  0);
        }
        spreadInRoom();
    }

    private void spreadInRoom() {
        // Group persons by initial room
        Map<String, List<String>> byRoom = new LinkedHashMap<>();
        for (Person p : ctx.getResidents()) {
            byRoom.computeIfAbsent(p.getLocation().getName(), k -> new ArrayList<>()).add(p.getName());
        }
        // Assign slot offsets within room
        int[] dx = {-30, 30, -30, 30, 0, -50};
        int[] dy = {-20, -20, 20, 20, 0, 0};
        for (List<String> names : byRoom.values()) {
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                float[] pos = personPos.get(name);
                pos[0] += i < dx.length ? dx[i] : 0;
                pos[1] += i < dy.length ? dy[i] : 0;
                personTarget.put(name, pos.clone());
            }
        }
    }

    private float[] roomCentre(String roomName) {
        int[] rc = ROOM_GRID.getOrDefault(roomName, new int[]{1, 1});
        return new float[]{
                rc[1] * CELL_W + CELL_W / 2f,
                rc[0] * CELL_H + CELL_H / 2f
        };
    }

    // ── UI ────────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("Smart Home Simulation — Replay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(COL_BG);
        setLayout(new BorderLayout(0, 0));

        // Game panel
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        add(gamePanel, BorderLayout.CENTER);

        // Right log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(COL_LOG_BG);
        logPanel.setPreferredSize(new Dimension(LOG_W, PANEL_H));

        JLabel logHeader = new JLabel("  Activity Log", JLabel.LEFT);
        logHeader.setFont(pixelFont);
        logHeader.setForeground(COL_LOG_HDR);
        logHeader.setBackground(new Color(0x0a0e1a));
        logHeader.setOpaque(true);
        logHeader.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 0));
        logPanel.add(logHeader, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(COL_LOG_BG);
        logArea.setForeground(COL_LOG_TEXT);
        logArea.setFont(pixelFontSm);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(logArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COL_LOG_BG);
        logPanel.add(scroll, BorderLayout.CENTER);
        add(logPanel, BorderLayout.EAST);

        // Bottom control bar
        JPanel controls = buildControls();
        add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildControls() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setBackground(COL_STATUS_BG);
        bar.setPreferredSize(new Dimension(PANEL_W + LOG_W, STATUS_H + 10));

        // Step / time labels
        stepLabel = new JLabel("Step: 0 / " + totalSteps);
        stepLabel.setFont(pixelFont);
        stepLabel.setForeground(COL_ROOM_NAME);

        timeLabel = new JLabel("Time: --:--");
        timeLabel.setFont(pixelFont);
        timeLabel.setForeground(new Color(0xf1fa8c));

        weatherLabel = new JLabel("Weather: --");
        weatherLabel.setFont(pixelFont);
        weatherLabel.setForeground(new Color(0x8be9fd));

        // Buttons
        JButton btnBack  = makeBtn("◀ Prev");
        btnPlay          = makeBtn("▶ Play");
        JButton btnNext  = makeBtn("Next ▶");
        JButton btnReset = makeBtn("↺ Reset");
        JButton btnRep   = makeBtn("📋 Reports");

        // Speed slider
        JLabel speedLbl = new JLabel("Speed:");
        speedLbl.setFont(pixelFontSm);
        speedLbl.setForeground(COL_LOG_TEXT);

        speedSlider = new JSlider(1, 10, 4);
        speedSlider.setBackground(COL_STATUS_BG);
        speedSlider.setForeground(COL_LOG_TEXT);
        speedSlider.setPreferredSize(new Dimension(100, 24));

        bar.add(stepLabel);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(timeLabel);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(weatherLabel);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(btnBack);
        bar.add(btnPlay);
        bar.add(btnNext);
        bar.add(btnReset);
        bar.add(btnRep);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(speedLbl);
        bar.add(speedSlider);

        // Legend
        for (Person p : ctx.getResidents()) {
            JLabel leg = new JLabel(" " + ROLE_ICON.get(p.getRole().name()) + "=" + p.getName());
            leg.setFont(pixelFontSm);
            leg.setForeground(ROLE_COLOR.getOrDefault(p.getRole().name(), Color.WHITE));
            bar.add(leg);
        }

        // Wire buttons
        btnPlay.addActionListener(e -> togglePlay());
        btnNext.addActionListener(e -> { stopPlay(); doStep(); });
        btnBack.addActionListener(e -> { stopPlay(); stepBack(); });
        btnReset.addActionListener(e -> { stopPlay(); resetSim(); });
        btnRep.addActionListener(e -> generateReports());

        return bar;
    }

    private JButton makeBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(pixelFontSm);
        b.setBackground(COL_ROOM_FILL);
        b.setForeground(COL_ROOM_NAME);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(COL_GRID_LINE, 1));
        return b;
    }

    // ── Animation loop ────────────────────────────────────────────────────
    private void startAnimLoop() {
        animTimer = new Timer(30, e -> {
            // Lerp all persons toward target
            for (String name : personPos.keySet()) {
                float[] pos = personPos.get(name);
                float[] tgt = personTarget.get(name);
                pos[0] += (tgt[0] - pos[0]) * 0.12f;
                pos[1] += (tgt[1] - pos[1]) * 0.12f;

                // Decrement bubble timers
                int bt = bubbleTimer.getOrDefault(name, 0);
                if (bt > 0) bubbleTimer.put(name, bt - 1);
                else        personBubble.put(name, "");
            }
            gamePanel.repaint();
        });
        animTimer.start();
    }

    // ── Simulation step ───────────────────────────────────────────────────
    private void doStep() {
        if (currentStep >= totalSteps) {
            stopPlay();
            generateReports(); // auto-generate when simulation finishes
            return;
        }

        // Save snapshot BEFORE running the step so we can go back
        saveSnapshot();

        int logBefore = ctx.getActivityLog().getEntries().size();
        engine.runStep();
        currentStep++;

        // Collect new log entries
        List<ActivityEntry> entries = ctx.getActivityLog().getEntries();
        List<ActivityEntry> newEntries = entries.subList(logBefore, entries.size());

        // Move persons (skip Father if shopping — location is null)
        for (Person p : ctx.getResidents()) {
            if (p.getLocation() == null) continue;
            String roomName = p.getLocation().getName();
            float[] centre  = roomCentre(roomName);
            centre[0] += (float)(Math.random() * 40 - 20);
            centre[1] += (float)(Math.random() * 30 - 15);
            personTarget.put(p.getName(), centre);
        }

        // Move animals
        for (var a : ctx.getAnimals()) {
            if (a.getLocation() == null) continue;
            float[] centre = roomCentre(a.getLocation().getName());
            centre[0] += (float)(Math.random() * 30 - 15);
            centre[1] += (float)(Math.random() * 20 - 10);
            personTarget.put(a.getName(), centre);
        }

        // Show action bubbles from log
        for (ActivityEntry e : newEntries) {
            String actionText = e.getAction() + ": " + e.getTargetName();
            personBubble.put(e.getPersonName(), actionText);
            bubbleTimer.put(e.getPersonName(), 60);
            lastActions.put(e.getPersonName(), actionText);
            String line = "[" + e.getTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "] "
                    + e.getPersonName() + " → " + e.getAction()
                    + (e.getTargetName() != null ? ": " + e.getTargetName() : "") + "\n";
            logLines.add(line);
            SwingUtilities.invokeLater(() -> {
                logArea.append(line);
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }

        // Update labels
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);
        timeLabel.setText("Time: " + ctx.getCurrentTime()
                .format(DateTimeFormatter.ofPattern("HH:mm")));

        // Update weather label with icon
        var weather = ctx.getWeatherService().getCurrent();
        String icon = switch (weather) {
            case SUNNY  -> "☀";
            case CLOUDY -> "☁";
            case RAINY  -> "☂";
            case COLD   -> "❄";
        };
        weatherLabel.setText(icon + " " + weather.getDisplayName());
        weatherLabel.setForeground(switch (weather) {
            case SUNNY  -> new Color(0xf1fa8c);
            case CLOUDY -> new Color(0xaaaaaa);
            case RAINY  -> new Color(0x8be9fd);
            case COLD   -> new Color(0xbd93f9);
        });
    }

    // ── Snapshot ──────────────────────────────────────────────────────────
    private void saveSnapshot() {
        Map<String, String> rooms = new LinkedHashMap<>();
        for (Person p : ctx.getResidents()) {
            // Father may be shopping — location is null
            String roomName = p.getLocation() != null ? p.getLocation().getName() : "OUTSIDE";
            rooms.put(p.getName(), roomName);
        }
        // Also save animals
        for (var a : ctx.getAnimals()) {
            String roomName = a.getLocation() != null ? a.getLocation().getName() : "OUTSIDE";
            rooms.put(a.getName(), roomName);
        }

        String time = ctx.getCurrentTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        snapshots.add(new Snapshot(rooms, new LinkedHashMap<>(lastActions),
                time, currentStep, new ArrayList<>(logLines)));
    }

    private void stepBack() {
        if (snapshots.isEmpty()) return;

        // Pop last snapshot
        Snapshot snap = snapshots.remove(snapshots.size() - 1);
        currentStep = snap.step;

        // Restore person targets to snapshot positions
        for (Map.Entry<String, String> e : snap.personRooms.entrySet()) {
            String personName = e.getKey();
            String roomName   = e.getValue();
            if ("OUTSIDE".equals(roomName)) continue; // skip — person was shopping
            float[] centre = roomCentre(roomName);
            centre[0] += (float)(Math.random() * 40 - 20);
            centre[1] += (float)(Math.random() * 30 - 15);
            personTarget.put(personName, centre);
            // Show last action bubble
            String action = snap.personActions.getOrDefault(personName, "");
            if (!action.isEmpty()) {
                personBubble.put(personName, action);
                bubbleTimer.put(personName, 60);
            }
        }

        // Restore log display
        lastActions.clear();
        lastActions.putAll(snap.personActions);
        logLines.clear();
        logLines.addAll(snap.logLines);
        SwingUtilities.invokeLater(() -> {
            logArea.setText(String.join("", logLines));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });

        // Update labels
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);
        timeLabel.setText("Time: " + snap.simTime);
    }

    // ── Play/Stop ─────────────────────────────────────────────────────────
    private void togglePlay() {
        if (running) stopPlay();
        else         startPlay();
    }

    private void startPlay() {
        running = true;
        btnPlay.setText("⏸ Pause");
        int delay = 1100 - speedSlider.getValue() * 100; // 100–1000 ms
        stepTimer = new Timer(delay, e -> doStep());
        stepTimer.start();
    }

    private void stopPlay() {
        running = false;
        btnPlay.setText("▶ Play");
        if (stepTimer != null) stepTimer.stop();
    }

    private void resetSim() {
        // Note: full reset requires re-initializing ctx — for now just notify
        JOptionPane.showMessageDialog(this,
                "Restart the application to reset the simulation.",
                "Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Game panel (renderer) ─────────────────────────────────────────────
    private class GamePanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // Background
            g2.setColor(COL_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw rooms
            for (Map.Entry<String, int[]> entry : ROOM_GRID.entrySet()) {
                drawRoom(g2, entry.getKey(), entry.getValue());
            }

            // Draw fridge icon in Kitchen
            drawFridgeIndicator(g2);

            // Draw persons (skip if location is null — e.g. Father shopping)
            int idx = 0;
            for (Person p : ctx.getResidents()) {
                if (p.getLocation() != null) {
                    drawPerson(g2, p, idx);
                } else {
                    drawShoppingIndicator(g2, p);
                }
                idx++;
            }

            // Draw animals
            for (var a : ctx.getAnimals()) {
                if (a.getLocation() != null) drawAnimal(g2, a);
            }
        }

        private void drawFridgeIndicator(Graphics2D g2) {
            int[] rc = ROOM_GRID.get("Kitchen");
            if (rc == null) return;
            int x = rc[1] * CELL_W + CELL_W - 50;
            int y = rc[0] * CELL_H + 24;

            // Find fridge
            cz.cvut.fel.omo.smarthome.devices.Fridge fridge = null;
            for (Device d : ctx.getAllDevices()) {
                if (d instanceof cz.cvut.fel.omo.smarthome.devices.Fridge f) { fridge = f; break; }
            }
            if (fridge == null) return;

            int total = fridge.getFoodCount();
            int max   = 8 + 6 + 4 + 4; // sum of all max stocks
            Color col = total > 10 ? new Color(0x50fa7b)
                    : total > 5  ? new Color(0xf1fa8c)
                    : new Color(0xff5555);

            g2.setColor(col);
            g2.setFont(pixelFontSm);
            g2.drawString("F:" + total, x, y);
        }

        private void drawShoppingIndicator(Graphics2D g2, Person p) {
            // Show "SHOPPING" text at top-right corner
            Color col = ROLE_COLOR.getOrDefault(p.getRole().name(), Color.WHITE);
            g2.setFont(pixelFontSm);
            g2.setColor(col);
            String text = p.getName() + ": SHOPPING";
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, PANEL_W - tw - 6, 14 + ctx.getResidents().indexOf(p) * 14);
        }

        private void drawRoom(Graphics2D g2, String name, int[] rc) {
            int x = rc[1] * CELL_W;
            int y = rc[0] * CELL_H;

            // Room fill
            g2.setColor(COL_ROOM_FILL);
            g2.fillRect(x + 2, y + 2, CELL_W - 4, CELL_H - 4);

            // Border — pixel style (no rounding)
            g2.setColor(COL_ROOM_BORD);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x + 2, y + 2, CELL_W - 4, CELL_H - 4);

            // Corner accents (pixel art style)
            g2.setColor(COL_ROOM_NAME);
            g2.fillRect(x + 2, y + 2, 4, 4);
            g2.fillRect(x + CELL_W - 6, y + 2, 4, 4);
            g2.fillRect(x + 2, y + CELL_H - 6, 4, 4);
            g2.fillRect(x + CELL_W - 6, y + CELL_H - 6, 4, 4);

            // Room name
            g2.setFont(pixelFont);
            g2.setColor(COL_ROOM_NAME);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(name);
            g2.drawString(name, x + (CELL_W - tw) / 2, y + 18);

            // Device count indicator
            Room room = findRoom(name);
            if (room != null) {
                long onCount = room.getDevices().stream().filter(Device::isOn).count();
                long totalD  = room.getDevices().size();
                if (totalD > 0) {
                    g2.setFont(pixelFontSm);
                    g2.setColor(onCount > 0 ? new Color(0x50fa7b) : new Color(0x6272a4));
                    g2.drawString("dev:" + onCount + "/" + totalD, x + 6, y + CELL_H - 8);
                }
            }
        }

        private void drawPerson(Graphics2D g2, Person p, int idx) {
            float[] pos = personPos.get(p.getName());
            if (pos == null) return;

            int px = (int) pos[0];
            int py = (int) pos[1];

            Color col = ROLE_COLOR.getOrDefault(p.getRole().name(), Color.WHITE);
            String icon = ROLE_ICON.getOrDefault(p.getRole().name(), "??");

            // Tired → draw semi-transparent
            float alpha = p.isTired() ? 0.45f : 1.0f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(px - 13, py - 11, 28, 28);

            // Body — pixel sprite
            g2.setColor(col);
            g2.fillRect(px - 12, py - 12, 26, 26);

            // Inner dark square
            g2.setColor(col.darker());
            g2.fillRect(px - 8, py - 8, 18, 18);

            // Resting indicator — small "Z" in corner
            if (p.isTired()) {
                g2.setFont(pixelFontSm);
                g2.setColor(new Color(0xf1fa8c));
                g2.drawString("z", px + 6, py - 6);
            }

            // Icon text
            g2.setFont(pixelFontSm);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, px - fm.stringWidth(icon) / 2, py + fm.getAscent() / 2 - 1);

            // Reset alpha
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Name below
            g2.setFont(pixelFontSm);
            g2.setColor(col);
            String name = p.getName();
            int nw = g2.getFontMetrics().stringWidth(name);
            g2.drawString(name, px - nw / 2, py + 24);

            // Energy bar (24px wide, 3px tall)
            int barW = 24;
            int barX = px - barW / 2;
            int barY = py + 28;
            float pct = (float) p.getEnergy() / p.getMaxEnergy();
            Color barCol = pct > 0.6f ? new Color(0x50fa7b)
                    : pct > 0.3f ? new Color(0xf1fa8c)
                    :               new Color(0xff5555);

            g2.setColor(new Color(0x0f1425));
            g2.fillRect(barX - 1, barY - 1, barW + 2, 5);
            g2.setColor(barCol);
            g2.fillRect(barX, barY, (int)(barW * pct), 3);

            // Speech bubble
            String bubble = personBubble.getOrDefault(p.getName(), "");
            int bt = bubbleTimer.getOrDefault(p.getName(), 0);
            if (bubble != null && !bubble.isEmpty() && bt > 0) {
                drawBubble(g2, px, py - 20, bubble, col);
            }
        }

        private void drawBubble(Graphics2D g2, int x, int y, String text, Color col) {
            // Truncate long text
            String display = text.length() > 22 ? text.substring(0, 20) + ".." : text;

            g2.setFont(pixelFontSm);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(display);
            int th = fm.getHeight();
            int bx = x - tw / 2 - 5;
            int by = y - th - 12;
            int bw = tw + 10;
            int bh = th + 6;

            // Keep bubble inside panel
            bx = Math.max(2, Math.min(bx, PANEL_W - bw - 2));
            by = Math.max(2, by);

            // Bubble background
            g2.setColor(new Color(0x0f1425));
            g2.fillRect(bx, by, bw, bh);
            g2.setColor(col);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(bx, by, bw, bh);

            // Tail — small triangle pointing down
            g2.setColor(new Color(0x0f1425));
            g2.fillRect(x - 2, by + bh, 5, 4);
            g2.setColor(col);
            g2.drawLine(x - 2, by + bh, x - 2, by + bh + 3);
            g2.drawLine(x + 2, by + bh, x + 2, by + bh + 3);

            // Text
            g2.setColor(Color.WHITE);
            g2.drawString(display, bx + 5, by + fm.getAscent() + 2);
        }

        private void drawAnimal(Graphics2D g2, Animal a) {
            if (a.getLocation() == null) return;
            float[] pos = personPos.get(a.getName());
            if (pos == null) return;

            int px = (int) pos[0];
            int py = (int) pos[1];

            Color col = new Color(0xf1fa8c);
            g2.setColor(col);
            g2.fillRect(px - 10, py - 10, 20, 20);
            g2.setColor(col.darker());
            g2.fillRect(px - 6, py - 6, 12, 12);
            g2.setFont(pixelFontSm);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("KK", px - fm.stringWidth("KK") / 2, py + fm.getAscent() / 2 - 1);
            g2.setColor(col);
            int nw = fm.stringWidth(a.getName());
            g2.drawString(a.getName(), px - nw / 2, py + 20);

            String bubble = personBubble.getOrDefault(a.getName(), "");
            int bt = bubbleTimer.getOrDefault(a.getName(), 0);
            if (bubble != null && !bubble.isEmpty() && bt > 0) {
                drawBubble(g2, px, py - 16, bubble, col);
            }
        }

        private Room findRoom(String name) {
            for (var floor : ctx.getFloors()) {
                for (Room r : floor.getRooms()) {
                    if (r.getName().equals(name)) return r;
                }
            }
            return null;
        }
    }

    // ── Fridge popup on Kitchen click ─────────────────────────────────────
    private void setupFridgeClick() {
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int[] rc = ROOM_GRID.get("Kitchen");
                if (rc == null) return;
                int rx = rc[1] * CELL_W;
                int ry = rc[0] * CELL_H;
                if (e.getX() >= rx && e.getX() <= rx + CELL_W
                        && e.getY() >= ry && e.getY() <= ry + CELL_H) {
                    showFridgePopup();
                }
            }
        });
    }

    private void showFridgePopup() {
        cz.cvut.fel.omo.smarthome.devices.Fridge fridge = null;
        for (Device d : ctx.getAllDevices()) {
            if (d instanceof cz.cvut.fel.omo.smarthome.devices.Fridge f) { fridge = f; break; }
        }
        if (fridge == null) {
            JOptionPane.showMessageDialog(this, "No fridge found!", "Fridge", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Kitchen Fridge\n");
        sb.append("──────────────────\n");
        for (var entry : fridge.getAllStock().entrySet()) {
            var max = cz.cvut.fel.omo.smarthome.devices.Fridge.MAX_STOCK.get(entry.getKey());
            sb.append(String.format("%-10s %d / %d\n",
                    entry.getKey().name().charAt(0)
                            + entry.getKey().name().substring(1).toLowerCase() + ":",
                    entry.getValue(), max));
        }
        sb.append("──────────────────\n");
        sb.append(String.format("Total: %d items", fridge.getFoodCount()));

        JOptionPane.showMessageDialog(this, sb.toString(), "Fridge Status",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Reports ───────────────────────────────────────────────────────────
    private void generateReports() {
        if (currentStep == 0) {
            JOptionPane.showMessageDialog(this,
                    "Run the simulation first before generating reports.\nPress ▶ Play or Next ▶ to start.",
                    "No data yet", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onFinish != null) {
            onFinish.run();
            JOptionPane.showMessageDialog(this,
                    "Reports generated in 'output/' folder.\nSteps completed: " + currentStep + "/" + totalSteps,
                    "Reports ready", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────
    public static void show(SmartHomeContext ctx, int steps, Runnable onFinish) {
        SwingUtilities.invokeLater(() -> {
            SimulationVisualizer viz = new SimulationVisualizer(ctx, steps, onFinish);
            viz.setVisible(true);
        });
    }
}