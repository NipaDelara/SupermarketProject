# Supermarket Simulator

A discrete-event simulation of customer flow and checkout systems
in a supermarket. Built with Java 21 and JavaFX 20 as the final project
for the Object-Oriented Programming course (TX00EY21-3013) at
Metropolia University of Applied Sciences.

## Group Members
- Dornaraj Kharal — simulation engine (`simu.framework`, `simu.model`)
- Delara Nipa — controller and model–view bridge (`controller`)
- Noel Marenco — JavaFX user interface (`view`)

## What it does
The simulator reproduces the daily dynamics of a supermarket: customers
arrive at random intervals, move through shopping departments, decide
between regular and self-checkout, queue and get served, then exit.
The user can change the simulation parameters and watch queues form,
service rates change, and KPIs evolve in real time.

The user interface is split into three areas (matching the project
mockup):

- **Configuration** (left): simulation time, arrival rate, number of
  regular and self-checkout counters, animation speed, plus
  Start / Pause / Reset buttons.
- **Live animation** (center): visual representation of the supermarket
  with entrance, shopping area, checkout decision point, two checkout
  queues and the exit. Each customer is a coloured dot that flows
  through the system.
- **Live statistics** (right): average waiting time, utilization of
  each checkout type, throughput, and a queue-length trend chart.

## Tech stack
- Java 21 (toolchain target; Java 17+ should also work for runtime)
- JavaFX 20 (controls, graphics, fxml)
- Maven (build, dependency management, run plugin)

## How to Run

### Prerequisites
- JDK 21 (or 17+). Eclipse Temurin recommended.
- Maven 3.8+ (or use IntelliJ's bundled Maven).

### Option A — IntelliJ IDEA (recommended)
1. `File → Open…` → pick the `SuperMarketProject` folder.
2. Wait for IntelliJ to import the Maven project (it will download
   JavaFX automatically).
3. Open the Maven tool window (`View → Tool Windows → Maven`),
   expand `GUI → Plugins → javafx`, double-click **`javafx:run`**.

### Option B — Run `Main` directly
1. In the Project view, open `src/main/java/Main.java`.
2. Click the green ▶ next to the `Main` class.
3. `Main` is a launcher class that does **not** extend
   `javafx.application.Application`, which lets the JVM start without
   requiring `--module-path` flags.

### Option C — Command line (Maven)
```bash
mvn javafx:run
```

## Architecture

The project follows a Model–View–Controller separation:

```
view  ──── ISimulatorUI ────►  controller  ◄──── IControllerMtoV ──── simu.model (engine)
   ▲                                                                       │
   └──────── IVisualisation ◄────────────────────────────────────────────────┘
                  (Controller dispatches engine events to JavaFX thread
                   via Platform.runLater)
```

- **simu.framework / simu.model** — discrete-event engine. Owns the
  event list and runs as its own `Thread`.
- **controller** — the bridge. Calls into the View on the JavaFX thread
  using `Platform.runLater` so the UI never gets touched from the
  engine thread.
- **view** — JavaFX UI orchestrated by `SimulatorGUI` (a `BorderPane`
  composed of `ConfigurationPanel`, `SimulatorVisualisation` and
  `StatisticsPanel`).

## Project structure
```
src/main/java
├── Main.java                      # launcher (no Application subclass)
├── controller/
│   ├── Controller.java
│   ├── IControllerVtoM.java       # View → Engine commands
│   └── IControllerMtoV.java       # Engine → View notifications
├── simu/
│   ├── framework/                 # generic engine (Engine, Event, Clock, …)
│   └── model/                     # supermarket specifics (MyEngine, Customer, …)
├── eduni/distributions/           # statistical distributions library
└── view/
    ├── SimulatorGUI.java          # main window (BorderPane orchestrator)
    ├── ConfigurationPanel.java    # left panel
    ├── SimulatorVisualisation.java# center canvas
    ├── StatisticsPanel.java       # right panel
    ├── ISimulatorUI.java          # contract View ⇆ Controller
    └── IVisualisation.java        # contract Visualisation ⇆ Controller
```

## Suggested parameters to see the simulation in action
| Goal | Sim time | Arrival rate | Regular | Self |
|---|---|---|---|---|
| Stable / under-loaded system | 480 | 15 | 3 | 4 |
| Saturated system (queues form) | 120 | 3  | 2 | 2 |
| Stress test (very long queues) | 60  | 1  | 1 | 1 |

## Status / TODO
- [ ] Engine: support N regular and N self checkout counters from the
      `Configuration` panel (currently hard-coded to 1 each).
- [ ] Controller: implement real `pauseSimulation` / `resumeSimulation`
      / `stepSimulation` / `resetSimulation`.
- [ ] Controller: expose live statistics (avg waiting time, utilization,
      throughput) so the `StatisticsPanel` can show real numbers
      instead of placeholders.
- [ ] Persist results between runs (CSV/JSON export).
