package view;

import controller.Controller;
import controller.IControllerVtoM;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import simu.framework.Trace;
import simu.framework.Trace.Level;

/**
 * Application entry point and view orchestrator.
 * <p>
 * Layout (BorderPane):
 *   Top    = title bar
 *   Left   = ConfigurationPanel (builds SimulationConfig, controls run)
 *   Center = SimulatorVisualisation + status bar
 *   Right  = StatisticsPanel
 * <p>
 * Implements ISimulatorUI: forwards Controller queries to ConfigurationPanel.
 */
public class SimulatorGUI extends Application implements ISimulatorUI {

    private IControllerVtoM controller;

    private ConfigurationPanel configPanel;
    private StatisticsPanel    statsPanel;
    private SimulatorVisualisation display;

    private Label clockLabel;
    private Label customersLabel;
    private double endingTime = 0.0;
    private long simulationStartedAt = 0L;
    private boolean running = false;

    @Override
    public void init() {
        Trace.setTraceLevel(Level.INFO);
        controller = new Controller(this);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(t -> { Platform.exit(); System.exit(0); });
        primaryStage.setTitle("Supermarket Simulator v1.0");

        // ---- Components ----
        configPanel = new ConfigurationPanel(controller);
        statsPanel  = new StatisticsPanel();
        display     = new SimulatorVisualisation(580, 360);

        clockLabel = new Label("00 min 00 sec");
        clockLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        customersLabel = new Label("0");
        customersLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        VBox statusBox = statusBar();

        VBox centerBox = new VBox(8, display, statusBox);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(10));

        // RESPONSIVE: let center area grow
        VBox.setVgrow(display, Priority.ALWAYS);
        centerBox.setFillWidth(true);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#f3eee3"), new CornerRadii(0), Insets.EMPTY)));

        Label titleBar = new Label("Supermarket Simulator v1.0  ·  MariaDB persistence active");
        titleBar.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        titleBar.setTextFill(Color.web("#374151"));
        titleBar.setPadding(new Insets(2, 0, 10, 6));

        root.setTop(titleBar);
        root.setLeft(configPanel);
        root.setCenter(centerBox);
        root.setRight(statsPanel);
        BorderPane.setMargin(configPanel, new Insets(0, 12, 0, 0));
        BorderPane.setMargin(statsPanel,  new Insets(0, 0, 0, 12));

        // RESPONSIVE: side panels keep reasonable widths
        configPanel.setMinWidth(230);
        configPanel.setPrefWidth(260);
        configPanel.setMaxWidth(320);

        statsPanel.setMinWidth(220);
        statsPanel.setPrefWidth(240);
        statsPanel.setMaxWidth(300);

        // RESPONSIVE: canvas follows center space
        display.widthProperty().bind(centerBox.widthProperty().subtract(20));
        display.heightProperty().bind(centerBox.heightProperty().subtract(statusBox.heightProperty()).subtract(30));

        // RESPONSIVE: status card follows center width
        statusBox.maxWidthProperty().bind(centerBox.widthProperty().subtract(20));

        Scene scene = new Scene(root, 1180, 660);

        // RESPONSIVE: prevent layout from breaking too small
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(600);

        primaryStage.setScene(scene);
        primaryStage.show();

        startUiLoop();
    }

    private VBox statusBar() {
        VBox clockBox = new VBox(2, smallLabel("Simulation clock"), clockLabel);
        VBox custBox  = new VBox(2, smallLabel("Customers"),        customersLabel);
        HBox row = new HBox(40, clockBox, custBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        VBox card = new VBox(row);
        card.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
                Color.web("#e5e7eb"), BorderStrokeStyle.SOLID,
                new CornerRadii(8), new BorderWidths(1))));
        ((Region) card).setMaxWidth(580);
        return card;
    }

    private Label smallLabel(String txt) {
        Label l = new Label(txt);
        l.setFont(Font.font("System", FontWeight.NORMAL, 11));
        l.setTextFill(Color.web("#6b7280"));
        return l;
    }

    private void startUiLoop() {
        new AnimationTimer() {
            long lastSample = 0;
            @Override public void handle(long now) {
                if (now - lastSample < 200_000_000L) return;  // 5 Hz
                lastSample = now;

                if (endingTime > 0.0) {
                    clockLabel.setText(formatMinutes(endingTime));
                } else if (running) {
                    double elapsedSec = (System.currentTimeMillis() - simulationStartedAt) / 1000.0;
                    clockLabel.setText(formatMinutes(elapsedSec));
                }

                int processed = display.getCustomerCount();
                customersLabel.setText(processed + " / " + (int) configPanel.getSimulationTime());

                int regularQueue = display.getRegularQueueCount();
                int selfQueue = display.getSelfQueueCount();
                int totalQueue = regularQueue + selfQueue;

                statsPanel.recordQueueLength(totalQueue);

                // UPDATED: live average waiting time estimate
                statsPanel.updateAvgWaiting(totalQueue * 1.5);

                // UPDATED: live utilization estimate
                int regularCheckouts = getRegularCheckoutCountSafe();
                int selfCheckouts = getSelfCheckoutCountSafe();

                double regularUtil = regularCheckouts > 0
                        ? Math.min(100.0, (regularQueue * 100.0) / regularCheckouts)
                        : 0.0;

                double selfUtil = selfCheckouts > 0
                        ? Math.min(100.0, (selfQueue * 100.0) / selfCheckouts)
                        : 0.0;

                statsPanel.updateUtilization(regularUtil, selfUtil);

                // UPDATED: live throughput
                if (running) {
                    double elapsedHours = Math.max(
                            (System.currentTimeMillis() - simulationStartedAt) / 1000.0 / 3600.0,
                            0.0001
                    );

                    statsPanel.updateThroughput(processed / elapsedHours);
                }
            }
        }.start();
    }
    private int getRegularCheckoutCountSafe() {
        try {
            return configPanel.getRegularCheckouts();
        } catch (Exception e) {
            return 1;
        }
    }
    private int getSelfCheckoutCountSafe() {
        try {
            return configPanel.getSelfCheckouts();
        } catch (Exception e) {
            return 1;
        }
    }

    private String formatMinutes(double minutes) {
        int m = (int) minutes;
        int s = (int) ((minutes - m) * 60);
        return m + " min " + (s < 10 ? "0" + s : s) + " sec";
    }

    /* =================== ISimulatorUI =================== */

    @Override public double getTime()  { return configPanel.getSimulationTime(); }
    @Override public long   getDelay() { return configPanel.getDelay(); }

    @Override
    public void setEndingTime(double time) {
        endingTime = time;
        running = false;
        configPanel.onSimulationFinished();
        // Throughput estimate from the visualisation counter
        double hours = Math.max(time / 60.0, 0.0001);
        statsPanel.updateThroughput(display.getCustomerCount() / hours);
    }

    @Override public IVisualisation getVisualisation() {
        if (!running) {
            running = true;
            endingTime = 0.0;
            simulationStartedAt = System.currentTimeMillis();
            statsPanel.reset();
        }
        return display;
    }

    /* =================== Entry point =================== */

    public static void main(String[] args) {
        launch(args);
    }
}
