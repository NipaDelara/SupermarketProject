package simu.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data access object for {@link SimulationRun} entities.
 * <p>
 * Two tables involved:
 *   - simulation_run         : one row per run (input + headline output)
 *   - simulation_run_metric  : N rows per run (avg waiting, throughput, …)
 * <p>
 * Inserts are wrapped in a single transaction so a partial save can never
 * leave the run without its metrics or vice-versa.
 */
public class SimulationRunDAO {

    private static final String INSERT_RUN_SQL =
            "INSERT INTO simulation_run (" +
            "  simulation_time_minutes, delay_ms, arrival_dist_type, " +
            "  arrival_mean, arrival_std, self_max_items, " +
            "  end_time_minutes, customers_processed, notes" +
            ") VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_METRIC_SQL =
            "INSERT INTO simulation_run_metric (run_id, metric_name, metric_value, metric_unit) " +
            "VALUES (?,?,?,?)";

    private static final String SELECT_ALL_SQL =
            "SELECT id, run_at, simulation_time_minutes, delay_ms, arrival_dist_type, " +
            "       arrival_mean, arrival_std, self_max_items, " +
            "       end_time_minutes, customers_processed, notes " +
            "FROM simulation_run ORDER BY run_at DESC";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, run_at, simulation_time_minutes, delay_ms, arrival_dist_type, " +
            "       arrival_mean, arrival_std, self_max_items, " +
            "       end_time_minutes, customers_processed, notes " +
            "FROM simulation_run WHERE id = ?";

    private static final String SELECT_METRICS_SQL =
            "SELECT metric_name, metric_value, metric_unit " +
            "FROM simulation_run_metric WHERE run_id = ?";

    /* =================== INSERT =================== */

    /**
     * Persist a finished run. After return the {@code run.id} field is populated.
     * Throws SQLException on any DB failure — caller decides whether to swallow.
     */
    public long save(SimulationRun run) throws SQLException {
        try (Connection c = DatabaseConnection.get()) {
            c.setAutoCommit(false);
            try {
                long runId;
                try (PreparedStatement ps = c.prepareStatement(
                        INSERT_RUN_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDouble(1, run.getSimulationTimeMinutes());
                    ps.setLong  (2, run.getDelayMs());
                    ps.setString(3, run.getArrivalDistType());
                    ps.setDouble(4, run.getArrivalMean());
                    ps.setDouble(5, run.getArrivalStd());
                    ps.setInt   (6, run.getSelfMaxItems());
                    ps.setDouble(7, run.getEndTimeMinutes());
                    ps.setInt   (8, run.getCustomersProcessed());
                    ps.setString(9, run.getNotes());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("INSERT did not return an id");
                        }
                        runId = keys.getLong(1);
                    }
                }

                if (!run.getMetrics().isEmpty()) {
                    try (PreparedStatement ps = c.prepareStatement(INSERT_METRIC_SQL)) {
                        for (Map.Entry<String, SimulationRun.Metric> e : run.getMetrics().entrySet()) {
                            ps.setLong  (1, runId);
                            ps.setString(2, e.getKey());
                            ps.setDouble(3, e.getValue().value);
                            ps.setString(4, e.getValue().unit);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                c.commit();
                run.setId(runId);
                return runId;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /* =================== READS =================== */

    public List<SimulationRun> findAll() throws SQLException {
        List<SimulationRun> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRun(rs));
            }
        }
        return result;
    }

    /** Returns the run with metrics loaded, or null if not found. */
    public SimulationRun findById(long id) throws SQLException {
        try (Connection c = DatabaseConnection.get()) {
            SimulationRun run;
            try (PreparedStatement ps = c.prepareStatement(SELECT_BY_ID_SQL)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    run = mapRun(rs);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(SELECT_METRICS_SQL)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        run.addMetric(rs.getString("metric_name"),
                                      rs.getDouble("metric_value"),
                                      rs.getString("metric_unit"));
                    }
                }
            }
            return run;
        }
    }

    /* =================== Helpers =================== */

    private static SimulationRun mapRun(ResultSet rs) throws SQLException {
        SimulationRun r = new SimulationRun();
        r.setId(rs.getLong("id"));
        Timestamp ts = rs.getTimestamp("run_at");
        if (ts != null) r.setRunAt(ts.toLocalDateTime());
        r.setSimulationTimeMinutes(rs.getDouble("simulation_time_minutes"));
        r.setDelayMs(rs.getLong("delay_ms"));
        r.setArrivalDistType(rs.getString("arrival_dist_type"));
        r.setArrivalMean(rs.getDouble("arrival_mean"));
        r.setArrivalStd(rs.getDouble("arrival_std"));
        r.setSelfMaxItems(rs.getInt("self_max_items"));
        r.setEndTimeMinutes(rs.getDouble("end_time_minutes"));
        r.setCustomersProcessed(rs.getInt("customers_processed"));
        r.setNotes(rs.getString("notes"));
        return r;
    }
}
