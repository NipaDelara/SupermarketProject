package view;

import controller.IControllerVtoM;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import simu.model.SimulationConfig;

/**
 * Left-hand panel of the simulator: lets the user configure simulation
 * parameters and control the run (Start / Pause / Resume / Step / Reset).
 *
 * Builds a {@link SimulationConfig} from its fields when Start is pressed
 * and forwards it to the Controller. Fields that the simple UI does not
 * expose keep the SimulationConfig default values, so we still get a valid
 * run with a clean, focused UI.
 */
public class ConfigurationPanel extends VBox {

    private final TextField simulationTimeField;
    private final TextField arrivalMeanField;
    private final TextField arrivalStdField;
    private final TextField regularCheckoutsField;
    private final TextField selfCheckoutsField;
    private final TextField selfMaxItemsField;
    private final Slider    speedSlider;

    private final Button startButton;
    private final Button pauseButton;
    private final Button resumeButton;
    private final Button stepButton;
    private final Button resetButton;

    public ConfigurationPanel(IControllerVtoM controller) {
        // ---- Visual chrome ----
        setSpacing(10);
        setPadding(new Insets(18));
        setPrefWidth(260);
        setBackground(new Background(new BackgroundFill(
                Color.web("#eaf2fb"), new CornerRadii(10), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(
                Color.web("#b6d4fe"), BorderStrokeStyle.SOLID,
                new CornerRadii(10), new BorderWidths(1.5))));

        Label title = new Label("Configuration");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#0b3d91"));

        // ---- Input fields (defaults match SimulationConfig) ----
        simulationTimeField   = numericField("480");
        arrivalMeanField      = numericField("15");
        arrivalStdField       = numericField("5");
        regularCheckoutsField = numericField("3");
        selfCheckoutsField    = numericField("4");
        selfMaxItemsField     = numericField("10");

        // Speed slider (delay in ms; lower = faster)
        Label speedLabel = sectionLabel("Animation speed (delay ms)");
        speedSlider = new Slider(10, 1000, 200);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(false);
        speedSlider.setMajorTickUnit(250);
        Tooltip.install(speedSlider, new Tooltip("Lower = faster animation"));

        // ---- Action buttons ----
        startButton  = primaryButton("Start",  "#4ade80", "#14532d");
        pauseButton  = primaryButton("Pause",  "#fbbf24", "#78350f");
        resumeButton = primaryButton("Resume", "#60a5fa", "#1e3a8a");
        stepButton   = primaryButton("Step",   "#a78bfa", "#4c1d95");
        resetButton  = primaryButton("Reset",  "#f87171", "#7f1d1d");

        // Initial button states: only Start is enabled
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
        resetButton.setDisable(true);

        startButton.setOnAction(e -> {
            controller.startSimulation(buildSimulationConfig());
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            resumeButton.setDisable(true);
            stepButton.setDisable(true);
            resetButton.setDisable(false);
        });

        pauseButton.setOnAction(e -> {
            controller.pauseSimulation();
            pauseButton.setDisable(true);
            resumeButton.setDisable(false);
            stepButton.setDisable(false);
            resetButton.setDisable(false);
        });

        resumeButton.setOnAction(e -> {
            controller.resumeSimulation();
            pauseButton.setDisable(false);
            resumeButton.setDisable(true);
            stepButton.setDisable(true);
            resetButton.setDisable(false);
        });

        stepButton.setOnAction(e -> {
            controller.stepSimulation();
            pauseButton.setDisable(true);
            resumeButton.setDisable(false);
            stepButton.setDisable(false);
            resetButton.setDisable(false);
        });

        resetButton.setOnAction(e -> {
            controller.resetSimulation();
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            resumeButton.setDisable(true);
            stepButton.setDisable(true);
            resetButton.setDisable(true);
        });

        startButton .setMaxWidth(Double.MAX_VALUE);
        pauseButton .setMaxWidth(Double.MAX_VALUE);
        resumeButton.setMaxWidth(Double.MAX_VALUE);
        stepButton  .setMaxWidth(Double.MAX_VALUE);
        resetButton .setMaxWidth(Double.MAX_VALUE);

        HBox row1 = new HBox(8, startButton, pauseButton);
        HBox row2 = new HBox(8, resumeButton, stepButton);
        row1.setAlignment(Pos.CENTER);
        row2.setAlignment(Pos.CENTER);
        HBox.setHgrow(startButton,  Priority.ALWAYS);
        HBox.setHgrow(pauseButton,  Priority.ALWAYS);
        HBox.setHgrow(resumeButton, Priority.ALWAYS);
        HBox.setHgrow(stepButton,   Priority.ALWAYS);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                title,
                fieldGroup("Simulation time (min)",     simulationTimeField),
                fieldGroup("Arrival mean (min)",        arrivalMeanField),
                fieldGroup("Arrival std",               arrivalStdField),
                fieldGroup("Regular checkouts",         regularCheckoutsField),
                fieldGroup("Self-checkouts",            selfCheckoutsField),
                fieldGroup("Self max items (route)",    selfMaxItemsField),
                speedLabel, speedSlider,
                spacer,
                row1, row2, resetButton
        );
    }

    /* =================== Public API for SimulatorGUI =================== */

    /** Build a SimulationConfig populated with current UI values. */
    public SimulationConfig buildSimulationConfig() {
        SimulationConfig c = new SimulationConfig();           // defaults baked in
        c.arrivalMean   = parseDoubleSafe(arrivalMeanField,   c.arrivalMean);
        c.arrivalStd    = parseDoubleSafe(arrivalStdField,    c.arrivalStd);
        c.selfMaxItems  = (int) parseDoubleSafe(selfMaxItemsField, c.selfMaxItems);
        // Note: regular/self checkout COUNT fields are read by buildSimulationConfig
        // but the engine only uses 1 instance of each. When Dornaraj enables N
        // checkouts in MyEngine, he can pull these from c. (Stored in notes for now.)
        return c;
    }

    public double getSimulationTime() { return parseDoubleSafe(simulationTimeField, 480); }
    public long   getDelay()          { return (long) speedSlider.getValue(); }
    public int    getRegularCheckouts() { return (int) parseDoubleSafe(regularCheckoutsField, 3); }
    public int    getSelfCheckouts()    { return (int) parseDoubleSafe(selfCheckoutsField,    4); }

    /** Re-enable Start and reset all buttons (called from SimulatorGUI when the run finishes). */
    public void onSimulationFinished() {
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
        resetButton.setDisable(true);
    }

    /* =================== Helpers =================== */

    private static TextField numericField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#1e3a8a"));
        return l;
    }

    private static VBox fieldGroup(String labelText, TextField field) {
        return new VBox(3, sectionLabel(labelText), field);
    }

    private static Button primaryButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 14 8 14;"
        );
        return b;
    }

    private static double parseDoubleSafe(TextField tf, double fallback) {
        try { return Double.parseDouble(tf.getText().trim()); }
        catch (NumberFormatException ex) { return fallback; }
    }
}
