package simu.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads connection settings from {@code src/main/resources/db.properties}
 * and provides JDBC {@link Connection}s on demand.
 *
 * Lightweight by design — no pooling. Each {@link #get()} opens a fresh
 * connection. For an academic simulator this is more than enough; the
 * application only persists once per run, at the end.
 *
 * If the properties file is missing or the database is unreachable,
 * {@link #get()} throws a {@link SQLException} so callers can decide
 * whether to surface or swallow the error (we swallow it in the
 * Controller so a DB outage does not crash the simulation).
 */
public final class DatabaseConnection {

    private static final String PROPERTIES_FILE = "/db.properties";

    private static final Properties PROPS = new Properties();
    private static volatile boolean loaded = false;

    private DatabaseConnection() { /* utility class */ }

    /** Open a brand-new connection. The caller is responsible for closing it. */
    public static Connection get() throws SQLException {
        ensureLoaded();
        try {
            return DriverManager.getConnection(
                    PROPS.getProperty("db.url"),
                    PROPS.getProperty("db.user"),
                    PROPS.getProperty("db.password"));
        } catch (SQLException e) {
            // wrap with the URL we tried, so debugging is fast
            throw new SQLException(
                    "Failed to connect to " + PROPS.getProperty("db.url") +
                    " as user '" + PROPS.getProperty("db.user") + "'", e);
        }
    }

    /** Attempt a connection right now and close it. Useful for a startup health check. */
    public static boolean ping() {
        try (Connection c = get()) {
            return c != null && c.isValid(2);
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] ping failed: " + e.getMessage());
            return false;
        }
    }

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Cannot find " + PROPERTIES_FILE + " on classpath. " +
                        "Make sure src/main/resources/db.properties exists.");
            }
            PROPS.load(in);
            loaded = true;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + PROPERTIES_FILE, e);
        }
    }
}
