-- ============================================================================
--  Supermarket Simulator - MariaDB schema
--  Run once on a fresh database:
--      mariadb -u simulator -p supermarket_sim < schema.sql
-- ============================================================================

-- Make the script idempotent: if you re-run, drop in dependency order first.
DROP TABLE IF EXISTS simulation_run_metric;
DROP TABLE IF EXISTS simulation_run;

-- ----------------------------------------------------------------------------
--  simulation_run
--  One row per finished simulation run. Stores the input parameters
--  (so a run can be reproduced) and the headline result.
-- ----------------------------------------------------------------------------
CREATE TABLE simulation_run (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    run_at                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Input parameters (subset of SimulationConfig + Engine.simulationTime)
    simulation_time_minutes  DOUBLE       NOT NULL,
    delay_ms                 BIGINT       NOT NULL,
    arrival_dist_type        VARCHAR(32)  NOT NULL,
    arrival_mean             DOUBLE       NOT NULL,
    arrival_std              DOUBLE       NOT NULL,
    self_max_items           INT          NOT NULL,

    -- Headline result
    end_time_minutes         DOUBLE       NOT NULL,
    customers_processed      INT          NOT NULL,

    -- Free-text notes (optional)
    notes                    VARCHAR(500) DEFAULT NULL,

    PRIMARY KEY (id),
    INDEX idx_simulation_run_run_at (run_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
--  simulation_run_metric
--  Many rows per run: one per metric (avg waiting time, utilization, etc.).
--  Modeled as key/value to be future-proof: adding a new metric does NOT
--  require a schema migration.
-- ----------------------------------------------------------------------------
CREATE TABLE simulation_run_metric (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    run_id       BIGINT       NOT NULL,
    metric_name  VARCHAR(64)  NOT NULL,
    metric_value DOUBLE       NOT NULL,
    metric_unit  VARCHAR(16)  DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_metric_run
        FOREIGN KEY (run_id) REFERENCES simulation_run(id)
        ON DELETE CASCADE,
    INDEX idx_metric_run (run_id, metric_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
