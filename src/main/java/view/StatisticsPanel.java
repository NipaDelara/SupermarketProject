package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Right-hand panel of the simulator: shows live key-performance indicators.
 *
 * Exposes update*() methods that are safe to call from the JavaFX thread
 * (SimulatorGUI wraps engine callbacks in Platform.runLater).
 */
public class StatisticsPanel extends VBox {

    private final Label avgWaitingValue;
    private final Label utilRegularValue;
    private final Label utilSelfValue;
    private final Label throughputValue;

    private final XYChart.Series<Number, Number> queueSeries;
    private int sampleIndex = 0;

    public StatisticsPanel() {
        setSpacing(10);
        setPadding(new Insets(18));
        setPrefWidth(240);
        setBackground(new Background(new BackgroundFill(
                Color.web("#e9f7ef"), new CornerRadii(10), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(
                Color.web("#b8e0ca"), BorderStrokeStyle.SOLID,
                new CornerRadii(10), new BorderWidths(1.5))));

        Label title = new Label("Live statistics");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#0f5132"));

        avgWaitingValue   = bigValue("--");
        utilRegularValue  = bigValue("--");
        utilSelfValue     = bigValue("--");
        throughputValue   = bigValue("--");

        VBox card1 = metricCard("Avg. waiting time", avgWaitingValue);
        VBox card2 = metricCard("Utilization regular", utilRegularValue);
        VBox card3 = metricCard("Utilization self",    utilSelfValue);
        VBox card4 = metricCard("Throughput",          throughputValue);

        // Mini chart for queue length trend
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFont(Font.font(9));
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Queue length trend");
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setPrefHeight(140);
        queueSeries = new XYChart.Series<>();
        chart.getData().add(queueSeries);

        getChildren().addAll(title, card1, card2, card3, card4, chart);
    }

    /* =================== Public API used by SimulatorGUI =================== */

    public void updateAvgWaiting(double minutes) {
        avgWaitingValue.setText(String.format("%.1f min", minutes));
    }

    public void updateUtilization(double regularPercent, double selfPercent) {
        utilRegularValue.setText(String.format("%.0f %%", regularPercent));
        utilSelfValue.setText(String.format("%.0f %%", selfPercent));
    }

    public void updateThroughput(double customersPerHour) {
        throughputValue.setText(String.format("%.0f cust/h", customersPerHour));
    }

    public void recordQueueLength(int totalQueueLength) {
        ObservableList<XYChart.Data<Number, Number>> pts = queueSeries.getData();
        pts.add(new XYChart.Data<>(sampleIndex++, totalQueueLength));
        // keep last 60 samples to avoid unbounded growth
        if (pts.size() > 60) pts.remove(0);
    }

    public void reset() {
        avgWaitingValue.setText("--");
        utilRegularValue.setText("--");
        utilSelfValue.setText("--");
        throughputValue.setText("--");
        queueSeries.getData().clear();
        sampleIndex = 0;
    }

    /* =================== Helpers =================== */

    private static Label bigValue(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 22));
        l.setTextFill(Color.web("#0f172a"));
        return l;
    }

    private static VBox metricCard(String title, Label valueLabel) {
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.NORMAL, 12));
        t.setTextFill(Color.web("#475569"));
        VBox card = new VBox(2, t, valueLabel);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
                Color.web("#dce7df"), BorderStrokeStyle.SOLID,
                new CornerRadii(8), new BorderWidths(1))));
        return card;
    }
}
