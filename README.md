# Smart Home Simulator

A Java-based smart home simulation with a pixel-art desktop visualizer. The project models the life of a Czech family in a smart house - residents interact with devices, follow daily routines, react to weather, and manage food supplies. The entire simulation plays out in real time through a GUI inspired by turn-based strategy games.

---

## Background

This project started as a university assignment at CTU FEL Prague. The original scope was limited — a smart home model with a few design patterns and file-based report generation. No simulation, no visualizer.

The visual replay system, meal scheduling, weather behavior, energy system, and shopping logic were all added beyond the assignment requirements. The Swing visualizer was built in collaboration with AI — architecture decisions, feature design, and iteration were mine; AI assisted with implementation. This also became a practical exercise in prompt engineering — learning to decompose complex features and communicate requirements precisely.

This is v1 — a sandbox prototype with solid architectural foundations and a lot of room to grow. The codebase is designed to be extended: adding a new resident type requires one class and one line in the factory, adding a new weather effect requires one enum value. v2 will focus on making the house actually smart — sensors that trigger real automation, device schedules, and behavior patterns.

One known mistake worth mentioning: the entire project was developed in a single Git branch. v2 will follow a proper Git flow with feature branches and a stable main branch.

---

## Table of Contents

- [Background](#background)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Design Patterns](#design-patterns)
- [Class Hierarchy](#class-hierarchy)
- [Residents and Behavior](#residents-and-behavior)
- [Device System](#device-system)
- [Food and Shopping System](#food-and-shopping-system)
- [Weather System](#weather-system)
- [Energy System](#energy-system)
- [Visualizer](#visualizer)
- [Reports](#reports)
- [Running the Project](#running-the-project)
- [Configuration](#configuration)
- [Project Structure](#project-structure)

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 23 | Core language |
| Java Swing | built into JDK | Desktop GUI visualizer |
| Jackson Databind | 2.17.1 | JSON config parsing (`house.json`) |
| Maven | 3.x | Build system |

---

## System Architecture

### Diagram 1 - Startup and Initialization

![Diagram 1 - Startup and Initialization](docs/diagrams/diagram1_startup.png)

### Diagram 2 - Simulation Engine and Tick Cycle

![Diagram 2 - Simulation Engine and Tick Cycle](docs/diagrams/diagram2_simulation_engine.png)

### Diagram 3 - Event System (Observer + Chain of Responsibility)

![Diagram 3 - Event System](docs/diagrams/diagram3_event_system.png)

---

## Design Patterns

### State - device states

A device delegates all behavior to its current state object. When the state changes, the state object is replaced entirely.

```java
// Device.java
private DeviceState state = new OffState();

public void turnOn()  { state.turnOn(this);  }
public void turnOff() { state.turnOff(this); }
public void tick()    { state.tick(this);    }
```

| State | turnOn | turnOff | tick |
|---|---|---|---|
| `OffState` | transitions to OnState | nothing | nothing |
| `OnState` | nothing | transitions to OffState | random breakdown |
| `BrokenState` | blocked | blocked | waiting for repair |

### Observer - event bus

`EventBus` fully decouples devices from residents. A device does not know about the existence of specific resident classes.

```java
// On initialization
eventBus.subscribe(father);
eventBus.subscribe(mother);
eventBus.subscribe(new SystemEventListener(chain, ctx));

// On device breakdown
device.publishEvent(new Event(EventType.DEVICE_BROKEN, device, user));
// EventBus calls onEvent() on all subscribers
```

### Chain of Responsibility - event handling

Events pass through a chain of handlers. Each one either handles the event or passes it further.

```java
father.setNext(mother);
mother.setNext(daughter);
daughter.setNext(grandfather);
grandfather.setNext(fallback); // catch-all
```

### Singleton - shared simulation state

`SmartHomeContext` guarantees that all system components work with the same house state.

```java
public static synchronized SmartHomeContext getInstance() {
    if (instance == null) instance = new SmartHomeContext();
    return instance;
}
```

`synchronized` ensures thread safety - without it, two simultaneous calls could create two different context instances.

### Factory - object creation

Factories encapsulate creation logic. `SmartHomeContext` never calls `new Father()` directly.

```java
Person person = switch (def.role) {
    case FATHER      -> new Father(def.id, def.name, def.role, location, ps);
    case MOTHER      -> new Mother(def.id, def.name, def.role, location, ps);
    case SON         -> new Son(def.id, def.name, def.role, location, ps);
    case GRANDFATHER -> new Grandfather(def.id, def.name, def.role, location, ps);
    default          -> new Person(def.id, def.name, def.role, location, ps);
};
```

Adding a new resident type requires only creating a new class and one line in `PersonFactory`. `SmartHomeContext` and `SimulationEngine` remain unchanged.

### Template Method - report generation

The file-writing algorithm is defined once in `AbstractReportGenerator`. Each of the four generators implements only its own formatting logic.

```java
public abstract class AbstractReportGenerator {
    protected void writeToFile(String path, String content) { ... }
    public abstract void generate(String outputPath);
}
```

---

## Class Hierarchy

### Diagram 4 - People and Animal Class Hierarchy

![Diagram 4 - People Hierarchy](docs/diagrams/diagram4_people_hierarchy.png)

### Diagram 5 - Device Hierarchy and State Pattern

![Diagram 5 - Device Hierarchy](docs/diagrams/diagram5_devices_states.png)

### Diagram 6 - Reports, AutoBuyer and Event Handlers

![Diagram 6 - Reports and AutoBuyer](docs/diagrams/diagram6_reports_autobuyer.png)

---

## Residents and Behavior

| Resident | Role | maxEnergy | Special behavior |
|---|---|---|---|
| Otec | FATHER | 100 | Repairs devices, goes grocery shopping when ingredients are missing |
| Matka | MOTHER | 90 | Cooks on schedule, waters plants on sunny days |
| Syn | SON | 120 | Priority - watch TV in his own room |
| Dcera | DAUGHTER | 110 | Standard behavior |
| Ded | GRANDFATHER | 60 | Activates thermostat in cold weather |
| Kocka | CAT | - | Wanders independently around the house, logs WANDERED_TO / SLEEPING |

### Permission System (PermissionSet)

Each resident has a set of rules `PermissionRule` defining which actions they can perform on which device types. The cat has no permissions. Father has full access. Children can only control lights, TV, and audio.

---

## Device System

The house supports 21 device types (`DeviceType`):

```
SMART_LIGHT, GROUP_LIGHT, GARDEN_LIGHT, SMART_LOCK,
MOTION_SENSOR, DOOR_WINDOW_SENSOR, SMOKE_GAS_SENSOR,
WATER_LEAK_SENSOR, AIR_QUALITY_SENSOR, OUTDOOR_CAMERA,
THERMOSTAT, HUMIDIFIER_AC, SMART_WASHING_MACHINE,
MULTIROOM_AUDIO, SMART_TV, SMART_MIRROR, SMART_BLINDS,
IRRIGATION_SYSTEM, PET_FEEDER, SMART_COFFEE_MACHINE, FRIDGE
```

### Breakdown probabilities

Each tick a powered device (`OnState`) can randomly break down. The probability depends on the last user:

| User | Breakdown chance per tick |
|---|---|
| Son | 0.50% (50/10000) |
| Daughter | 0.35% (35/10000) |
| Others | 0.20% (20/10000) |

On breakdown, a `DEVICE_BROKEN` event is published via `EventBus`. `FatherHandler` intercepts it, and Father goes to repair in the next tick.

### AutoBuyer - automatic purchases

Residents have a wishlist (`desires`). If a desired device is missing from the house, `AutoBuyer` purchases it and places it in the appropriate room. Each type has an individual limit:

| Limit | Devices |
|---|---|
| 1 (unique) | Fridge, AC, Washing Machine, Thermostat, Lock, Irrigation, Coffee Machine, Pet Feeder |
| 2 | TV, Audio, Mirror, Camera, all sensors |
| 4-6 | Lights, blinds |

---

## Food and Shopping System

### Meal schedule

| Meal | Time | Ingredients |
|---|---|---|
| Breakfast | 08:00-10:00 | Eggs x2, Oatmeal x1 |
| Lunch | 12:00-14:00 | Soup x2 |
| Dinner | 18:00-20:00 | Steak x2 |

### Initial fridge stock

| Item | Start | Maximum |
|---|---|---|
| Eggs | 4 | 8 |
| Oatmeal | 2 | 6 |
| Soup | 2 | 4 |
| Steak | 1 | 4 |

Steak intentionally starts at `1` with a required amount of `2` - this guarantees Father makes a shopping trip before dinner on every run.

### Father's shopping logic

30 minutes before each meal (`07:30`, `11:30`, `16:30`) Father checks the fridge. If ingredients are insufficient:

1. `location = null` - disappears from the visualizer map
2. Log: `SHOPPING: Left for groceries (4 ticks ~1h)`
3. After 4 ticks returns and calls `fridge.restockFull()`
4. Log: `RESTOCK: Fridge fully restocked`

The check runs in `performStep()` before sport logic - shopping has the highest priority and interrupts any current activity.

---

## Weather System

Weather changes every 8 ticks (~2 hours) randomly with weighted probabilities:

| Weather | Probability | Effect |
|---|---|---|
| SUNNY | 50% | Mother waters plants (activates IRRIGATION_SYSTEM) |
| CLOUDY | 30% | No effect |
| RAINY | 15% | Blocks all activities in Garden |
| COLD | 5% | Grandfather activates thermostat |

In bad weather (`RAINY` / `COLD`), `tryFindAndUseSport()` and `interactWithDevice()` exclude the Garden room from candidate lists.

---

## Energy System

Each resident has an `energy` parameter (0 - maxEnergy). When energy is low the resident goes to their home room to rest.

| Event | Energy change |
|---|---|
| One sport tick | -15 |
| One rest tick | +10 |
| One idle tick | +3 |

| Role | maxEnergy |
|---|---|
| GRANDFATHER | 60 |
| MOTHER | 90 |
| FATHER | 100 |
| DAUGHTER | 110 |
| SON | 120 |

When `energy < 30` - the resident interrupts any activity and goes to rest. When `energy >= 70` - ready for sport again.

---

## Visualizer

Desktop Swing application. 3x3 house map in pixel-art style.

```
[Garage]       [Garden]      [  -  ]
[SonRoom]      [LivingRoom]  [Kitchen]
[DaughterRoom] [Bathroom]    [  -  ]
```

### Two-timer architecture

The visualizer runs on two independent `javax.swing.Timer` instances:

```
animTimer  (30ms)     - smoothly moves sprites toward target positions (lerp interpolation)
                        repaints the screen 33 times per second
                        knows nothing about the simulation

stepTimer  (configurable) - triggers one tick via engine.runStep()
                            updates target sprite positions
                            frequency controlled by speed slider
```

### GUI features

| Feature | Description |
|---|---|
| Pixel sprites | Each resident - colored sprite with role code (OT, MA, SY, DC, DE, KK) |
| Smooth animation | Lerp interpolation of sprite positions (30 fps) |
| Action bubbles | Last action from ActivityLog shown above sprite |
| Energy bar | Green - yellow - red bar below each sprite |
| Fatigue | Semi-transparent sprite + `z` symbol when resting |
| Father shopping | Sprite disappears from map, corner shows `Otec: SHOPPING` |
| Weather indicator | Status bar: `☀ Sunny / ☁ Cloudy / ☂ Rainy / ❄ Cold` |
| Device counter | In each room: `dev:on/total` |
| Clickable fridge | Click Kitchen - popup with current food inventory |
| Back button | Snapshot-based rewind - step back through saved state snapshots |

### Snapshot-based rewind

Before each tick `doStep()` saves a snapshot (`Snapshot`) - positions of all residents and animals, last actions, log text, simulation time. Pressing `◀ Prev` restores the last snapshot. This is a visual rollback - the state of Java objects (devices, inventory) is not rolled back.

---

## Reports

Press the `📋 Reports` button after running the simulation. Files are saved to the `output/` folder:

| File | Contents |
|---|---|
| `house_configuration_report.txt` | Full house structure - floors, rooms, devices, residents |
| `activity_report.txt` | Log of all actions by all residents with timestamps |
| `event_report.txt` | Device breakdowns, who handled them, through which Handler |
| `consumption_report.txt` | Electricity (kWh), water (L), gas (m³) consumption per device |

---

## Running the Project

**Requirements:** Java 23+, Maven 3.x

```bash
git clone https://github.com/ddbrdpl/smarthome_simulator.git
cd smarthome_simulator
mvn compile
mvn exec:java -Dexec.mainClass="cz.cvut.fel.omo.smarthome.Main"
```

### GUI controls

| Button | Action |
|---|---|
| `▶ Play` | Auto-run simulation |
| `Next ▶` | One tick forward (15 min game time) |
| `◀ Prev` | Step back (visual rollback) |
| `↺ Reset` | Restart |
| `📋 Reports` | Generate reports to `output/` |
| Speed slider | Control playback speed |
| Click Kitchen | View fridge inventory |

---

## Configuration

File `src/main/resources/house.json` - full house configuration:

```json
{
  "rooms":   [ { "name": "Kitchen", "type": "KITCHEN" } ],
  "persons": [ { "id": "p1", "name": "Otec", "role": "FATHER", "room": "LivingRoom" } ],
  "devices": [ { "id": "d1", "name": "Kitchen Fridge", "type": "FRIDGE", "room": "Kitchen" } ],
  "sports":  [ { "id": "s1", "type": "TREADMILL", "room": "Garage" } ]
}
```

To change the house layout, simply edit the JSON - no code changes needed.

---

## Project Structure

```
src/main/java/cz/cvut/fel/omo/smarthome/
├── config/
│   ├── Configuration.java              - house.json loading via Jackson
│   ├── DeviceFactory.java              - device factory (Factory pattern)
│   ├── PersonFactory.java              - resident factory (Factory pattern)
│   └── *Definition.java                - DTOs for JSON deserialization
├── consumption/
│   ├── ConsumptionLog.java
│   ├── ConsumptionProfile.java         - device consumption profile
│   └── ConsumptionRecord.java
├── devices/
│   ├── Device.java                     - abstract device class
│   ├── DeviceState.java                - state interface (State pattern)
│   ├── OnState.java                    - powered on (random breakdown)
│   ├── OffState.java                   - powered off
│   ├── BrokenState.java                - broken
│   ├── GenericDevice.java              - standard device
│   ├── Fridge.java                     - fridge with food system
│   ├── DeviceType.java                 - enum of 21 device types
│   └── FoodType.java                   - enum EGGS, OATMEAL, SOUP, STEAK
├── events/
│   ├── EventBus.java                   - event bus (Observer pattern)
│   ├── EventListener.java              - listener interface
│   ├── EventHandler.java               - handler interface (CoR pattern)
│   ├── AbstractEventHandler.java       - base chain handler
│   ├── FatherHandler.java              - handles DEVICE_BROKEN
│   ├── MotherHandler.java              - handles SMOKE_ALERT
│   ├── GrandfatherHandler.java         - handles TEMPERATURE_LOW
│   ├── FallbackHandler.java            - catch-all handler
│   ├── SystemEventListener.java        - triggers handler chain
│   ├── Event.java
│   └── EventType.java
├── house/
│   ├── SmartHomeContext.java           - Singleton, central context
│   ├── Floor.java
│   └── Room.java
├── logs/
│   ├── ActivityLog.java
│   ├── ActivityEntry.java
│   ├── EventLog.java
│   └── EventEntry.java
├── people/
│   ├── visualization/
│   │   └── SimulationVisualizer.java   - Swing GUI visualizer
│   ├── Animal.java                     - abstract animal class
│   ├── Cat.java                        - cat (wanders around)
│   ├── Person.java                     - base resident class
│   ├── Father.java                     - repairs devices, goes shopping
│   ├── Mother.java                     - cooks, waters plants
│   ├── Son.java                        - TV priority
│   ├── Grandfather.java                - reacts to cold weather
│   ├── PermissionSet.java              - resident permission set
│   ├── PermissionRule.java             - single permission rule
│   ├── Role.java                       - role enum
│   └── DeviceAction.java               - action enum TURN_ON / TURN_OFF
├── reports/
│   ├── ReportGenerator.java            - interface (Template Method)
│   ├── AbstractReportGenerator.java    - base generator
│   ├── HouseConfigurationReportGenerator.java
│   ├── ActivityReportGenerator.java
│   ├── EventReportGenerator.java
│   └── ConsumptionReportGenerator.java
├── shop/
│   ├── AutoBuyer.java                  - purchases devices with limits
│   └── ShopContext.java                - interface (ISP from SOLID)
├── simulation/
│   ├── SimulationEngine.java           - main simulation loop
│   ├── MealTime.java                   - meal schedule
│   ├── Weather.java                    - weather enum with weights
│   └── WeatherService.java             - weather change service
├── sports/
│   ├── SportEquipment.java             - sport equipment with timer
│   └── SportType.java                  - sport type enum
└── Main.java                           - entry point + report generation

src/main/resources/
└── house.json                          - house configuration

output/
├── house_configuration_report.txt
├── activity_report.txt
├── event_report.txt
└── consumption_report.txt
```

---

## v2 Roadmap

- Daily schedule - kitchen in the morning, living room in the evening
- Inter-resident interactions - competition for equipment and TV
- Night mode - after 22:00 lights turn off, everyone goes to their rooms
- Unit tests - coverage for `Fridge`, `MealTime`, `AutoBuyer`, `PermissionSet`
- More varied menu with extended food list<img width="997" height="848" alt="diagram1_Startup_and_Initialization" src="https://github.com/user-attachments/assets/2ff3b99d-81fd-4720-a67e-da73940c019c" />
<img width="997" height="848" alt="diagram1_Startup_and_Initialization" src="https://github.com/user-attachments/assets/59bee43f-8a79-406c-a295-fdc9edcc5722" />
