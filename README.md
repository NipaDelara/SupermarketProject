# Supermarket Simulator

Discrete-event simulation of customer flow and checkout systems
in a supermarket. Built with Java 21, JavaFX 20 and MariaDB
as the project for the Object-Oriented Programming course
(TX00EY21-3013) at Metropolia University of Applied Sciences.

## Group Members
- Dornaraj Kharal — simulation engine (`simu.framework`, `simu.model`)
- Delara Nipa — controller and model–view bridge (`controller`)
- Noel Marenco — JavaFX user interface (`view`) + MariaDB persistence (`simu.dao`)

## Course requirements coverage
| # | Requirement | Status |
|---|---|---|
| 1 | ≥ 4 service points, non-linear network | ✅ |
| 2 | Distributions / parameters changeable from UI | ✅ |
| 3 | JavaFX user interface | ✅ |
| 4 | Visualisation / animation of the run | ✅ |
| 5 | External data repository (file or database) | ✅ MariaDB |
| 6 | Slow / speed up / pause / step at runtime | ✅ |
| 7 | Pleasant, easy-to-use UI | ✅ |

## Tech stack
- Java 21 (toolchain target)
- JavaFX 20 (controls, fxml, graphics)
- MariaDB 11 + `mariadb-java-client` 3.3.x (JDBC)
- Maven (build, dependency management, run plugin)

---

## How to set up

### 1. Install MariaDB locally (one-time)

macOS (Homebrew):
```bash
brew install mariadb
brew services start mariadb
```

Then create the database and a dedicated user:
```bash
mariadb -u root <<'SQL'
CREATE DATABASE IF NOT EXISTS supermarket_sim;
CREATE USER IF NOT EXISTS 'simulator'@'localhost' IDENTIFIED BY 'simpass2026';
GRANT ALL PRIVILEGES ON supermarket_sim.* TO 'simulator'@'localhost';
FLUSH PRIVILEGES;
SQL
```

Apply the schema:
```bash
mariadb -u simulator -p'simpass2026' supermarket_sim < src/main/resources/db/schema.sql
```

### 2. Provide your DB credentials to the app
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# edit db.properties if your password differs
```
> `db.properties` is in `.gitignore`. Each developer keeps their own copy.

### 3. Run the app

#### From IntelliJ IDEA (recommended)
Open `pom.xml` as a project, wait for Maven to import, then either:
- Run `Main` (the launcher class), **or**
- Open the Maven tool window → `GUI → Plugins → javafx → javafx:run`.

#### From the command line
```bash
mvn javafx:run
```

---

## Architecture

```
view  ──── ISimulatorUI ────►  controller  ◄──── IControllerMtoV ──── simu.model (engine)
   ▲                                  │
   │                                  ▼
   └──── IVisualisation ◄──    simu.dao (MariaDB persistence)
```

- **simu.framework / simu.model** — discrete-event engine. Owns the
  event list and runs in its own `Thread`.
- **controller** — the bridge. Marshals UI input into a `SimulationConfig`
  and dispatches engine events back onto the JavaFX thread via
  `Platform.runLater`. Also persists the finished run.
- **view** — JavaFX UI with simulation controls and animation.
- **simu.dao** — JDBC persistence layer. Saves a row per run plus a
  flexible key/value table for metrics.

## Project structure
```
src/main/java
├── Main.java                              # launcher (no Application subclass)
├── controller/
│   ├── Controller.java                    # bridges UI + Engine + DAO
│   ├── IControllerVtoM.java
│   └── IControllerMtoV.java
├── simu/
│   ├── framework/                         # generic engine (Engine, Event, Clock, …)
│   ├── model/                             # supermarket specifics
│   │   ├── MyEngine.java
│   │   ├── Customer.java
│   │   ├── ServicePoint.java
│   │   ├── EventType.java
│   │   ├── Statistics.java
│   │   └── SimulationConfig.java
│   └── dao/                               # ⬅ NEW (Noel)
│       ├── DatabaseConnection.java
│       ├── SimulationRun.java
│       └── SimulationRunDAO.java
├── eduni/distributions/                   # statistical distributions library
└── view/
    ├── SimulatorGUI.java
    ├── ISimulatorUI.java
    ├── IVisualisation.java
    ├── Visualisation.java
    └── Visualisation2.java

src/main/resources
├── db.properties.example                  # template (committed)
├── db.properties                          # local secrets (gitignored)
└── db/
    └── schema.sql                         # MariaDB DDL
```

## Inspecting the saved runs

After running the simulator a few times, you can list the runs:
```bash
mariadb -u simulator -p'simpass2026' supermarket_sim -e \
  "SELECT id, run_at, customers_processed, end_time_minutes FROM simulation_run ORDER BY run_at DESC;"
```

Or look at metrics for a particular run:
```bash
mariadb -u simulator -p'simpass2026' supermarket_sim -e \
  "SELECT metric_name, metric_value, metric_unit FROM simulation_run_metric WHERE run_id = 1;"
```

## Suggested simulation parameters
| Goal | Sim time | Arrival mean | Self max items |
|---|---|---|---|
| Stable / under-loaded system | 480 | 15 | 10 |
| Saturated system (queues form) | 120 | 3  | 10 |
| Stress test (very long queues) | 60  | 1  | 5 |

## A note about the database

Each developer runs their **own local MariaDB**. The application connects to
`localhost:3306`, so the `simulation_run` table on Noel's laptop is a
different table from the one on Delara's or Dornaraj's laptop. **No data is
shared between developers**, and that is intentional for this academic
project: the requirement is that the program *uses* an external data
repository, not that the team shares one.

If a teammate runs the app **without** installing MariaDB, the simulator
still works — every other feature (UI, animation, statistics) keeps
running. The persistence step fails silently in the console with a message
like:

```
[Controller] Could not persist run: Failed to connect to jdbc:mariadb://...
```

This is intentional graceful degradation: a broken database must never
block the rest of the product.

## Roadmap (still open)
- [ ] UI screen to browse historical runs from MariaDB.
- [ ] Multi-checkout (N regular + N self) in the engine.
- [ ] Live statistics in UI (avg waiting time, utilisation).
- [ ] JUnit tests on DAO + ServicePoint.
- [ ] Javadoc generation (`Tools → Generate JavaDoc` in IntelliJ).
