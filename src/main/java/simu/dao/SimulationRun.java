package simu.dao;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain Java object that mirrors a row of the {@code simulation_run} table
 * plus the related metric rows.
 *
 * Used as the carrier between {@link SimulationRunDAO} and the rest of
 * the application (Controller, future "history" UI screen, …).
 */
public class SimulationRun {

    /* identity */
    private Long id;                    // null until inserted
    private LocalDateTime runAt;        // set by DB default

    /* inputs */
    private double simulationTimeMinutes;
    private long   delayMs;
    private String arrivalDistType;
    private double arrivalMean;
    private double arrivalStd;
    private int    selfMaxItems;

    /* outputs */
    private double endTimeMinutes;
    private int    customersProcessed;

    /* free-form metrics: name → (value, unit) */
    private final Map<String, Metric> metrics = new LinkedHashMap<>();

    /* notes */
    private String notes;

    /* ===================== Convenience builders ===================== */

    public SimulationRun addMetric(String name, double value, String unit) {
        metrics.put(name, new Metric(value, unit));
        return this;
    }

    /* ===================== Getters / setters ===================== */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getRunAt() { return runAt; }
    public void setRunAt(LocalDateTime runAt) { this.runAt = runAt; }

    public double getSimulationTimeMinutes() { return simulationTimeMinutes; }
    public void setSimulationTimeMinutes(double v) { this.simulationTimeMinutes = v; }

    public long getDelayMs() { return delayMs; }
    public void setDelayMs(long v) { this.delayMs = v; }

    public String getArrivalDistType() { return arrivalDistType; }
    public void setArrivalDistType(String v) { this.arrivalDistType = v; }

    public double getArrivalMean() { return arrivalMean; }
    public void setArrivalMean(double v) { this.arrivalMean = v; }

    public double getArrivalStd() { return arrivalStd; }
    public void setArrivalStd(double v) { this.arrivalStd = v; }

    public int getSelfMaxItems() { return selfMaxItems; }
    public void setSelfMaxItems(int v) { this.selfMaxItems = v; }

    public double getEndTimeMinutes() { return endTimeMinutes; }
    public void setEndTimeMinutes(double v) { this.endTimeMinutes = v; }

    public int getCustomersProcessed() { return customersProcessed; }
    public void setCustomersProcessed(int v) { this.customersProcessed = v; }

    public Map<String, Metric> getMetrics() { return metrics; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /* ===================== Inner POJO ===================== */

    public static final class Metric {
        public final double value;
        public final String unit;
        public Metric(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    @Override
    public String toString() {
        return "SimulationRun{id=" + id +
                ", runAt=" + runAt +
                ", endTime=" + endTimeMinutes +
                ", customers=" + customersProcessed +
                ", metrics=" + metrics +
                '}';
    }
}
