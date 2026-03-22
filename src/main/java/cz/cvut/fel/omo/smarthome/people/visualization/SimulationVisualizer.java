package cz.cvut.fel.omo.smarthome.people.visualization;

import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.house.Room;
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

    // ── Font ─────────────────────────────────────────────────────────────
    private Font pixelFont;
    private Font pixelFontSm;

    // ─────────────────────────────────────────────────────────────────────
    public SimulationVisualizer(SmartHomeContext ctx, int totalSteps) {
        this.ctx        = ctx;
        this.engine     = new SimulationEngine(ctx);
        this.totalSteps = totalSteps;

        loadFont();
        initPersonPositions();
        buildUI();
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
            // Offset slightly so people in the same room don't overlap
            personPos.put(p.getName(),    pos.clone());
            personTarget.put(p.getName(), pos.clone());
            personBubble.put(p.getName(), "");
            bubbleTimer.put(p.getName(),  0);
        }
        // Spread people in the same room
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

        // Buttons
        JButton btnBack = makeBtn("◀ Prev");
        btnPlay         = makeBtn("▶ Play");
        JButton btnNext = makeBtn("Next ▶");
        JButton btnReset= makeBtn("↺ Reset");

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
        bar.add(Box.createHorizontalStrut(20));
        bar.add(btnBack);
        bar.add(btnPlay);
        bar.add(btnNext);
        bar.add(btnReset);
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
        btnBack.addActionListener(e -> { /* replay not supported, just notify */ });
        btnReset.addActionListener(e -> { stopPlay(); resetSim(); });

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
            return;
        }

        int logBefore = ctx.getActivityLog().getEntries().size();
        engine.runStep(); // ← we call a single-step method (see below)
        currentStep++;

        // Collect new log entries
        List<ActivityEntry> entries = ctx.getActivityLog().getEntries();
        List<ActivityEntry> newEntries = entries.subList(logBefore, entries.size());

        // Move persons & show bubbles
        for (Person p : ctx.getResidents()) {
            String roomName = p.getLocation().getName();
            float[] centre  = roomCentre(roomName);
            // Add small random offset so people in same room spread
            centre[0] += (float)(Math.random() * 40 - 20);
            centre[1] += (float)(Math.random() * 30 - 15);
            personTarget.put(p.getName(), centre);
        }

        // Show action bubbles from log
        for (ActivityEntry e : newEntries) {
            personBubble.put(e.getPersonName(), e.getAction() + ": " + e.getTargetName());
            bubbleTimer.put(e.getPersonName(), 60); // ~1.8s at 30fps
            appendLog(e);
        }

        // Update labels
        stepLabel.setText("Step: " + currentStep + " / " + totalSteps);
        timeLabel.setText("Time: " + ctx.getCurrentTime()
                .format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void appendLog(ActivityEntry e) {
        String time = e.getTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String line = "[" + time + "] " + e.getPersonName()
                + " → " + e.getAction()
                + (e.getTargetName() != null ? ": " + e.getTargetName() : "") + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
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

            // Draw persons
            int idx = 0;
            for (Person p : ctx.getResidents()) {
                drawPerson(g2, p, idx++);
            }
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

            // Shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(px - 13, py - 11, 28, 28);

            // Body — pixel sprite (16×16)
            g2.setColor(col);
            g2.fillRect(px - 12, py - 12, 26, 26);

            // Inner dark square
            g2.setColor(col.darker());
            g2.fillRect(px - 8, py - 8, 18, 18);

            // Icon text
            g2.setFont(pixelFontSm);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, px - fm.stringWidth(icon) / 2, py + fm.getAscent() / 2 - 1);

            // Name below
            g2.setFont(pixelFontSm);
            g2.setColor(col);
            String name = p.getName();
            int nw = g2.getFontMetrics().stringWidth(name);
            g2.drawString(name, px - nw / 2, py + 24);

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

        private Room findRoom(String name) {
            for (var floor : ctx.getFloors()) {
                for (Room r : floor.getRooms()) {
                    if (r.getName().equals(name)) return r;
                }
            }
            return null;
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────
    public static void show(SmartHomeContext ctx, int steps) {
        SwingUtilities.invokeLater(() -> {
            SimulationVisualizer viz = new SimulationVisualizer(ctx, steps);
            viz.setVisible(true);
        });
    }
}