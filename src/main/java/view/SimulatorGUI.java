package view;

import java.text.DecimalFormat;
import controller.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import simu.framework.Trace;
import simu.framework.Trace.Level;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import simu.model.SimulationConfig;

public class SimulatorGUI extends Application implements ISimulatorUI {

	private IControllerVtoM controller;

	// Existing UI components
	private TextField time;
	private TextField delay;
	private Label results;
	private Label timeLabel;
	private Label delayLabel;
	private Label resultLabel;

	private Button startButton;
	private Button slowButton;
	private Button speedUpButton;
	private Button pauseButton;
	private Button resumeButton;
	private Button stepButton;

	private IVisualisation display;

	// New configuration input fields
	// Arrival
	private TextField arrivalMeanField;
	private TextField arrivalStdField;
	private ComboBox<String> arrivalDistCombo;

	// Shopping areas (all share same distribution type)
	private ComboBox<String> shoppingDistCombo;
	private TextField produceMeanField, produceStdField;
	private TextField dairyMeanField, dairyStdField;
	private TextField groceryMeanField, groceryStdField;
	private TextField beveragesMeanField, beveragesStdField;

	// Checkout
	private TextField regularMeanField, regularStdField;
	private TextField selfMeanField, selfStdField;

	// Routing probabilities (shopping area)
	private TextField probProduceField, probDairyField, probGroceryField, probBeveragesField;
	// Self-checkout item threshold
	private TextField selfMaxItemsField;

	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller(this);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setOnCloseRequest(t -> {
				Platform.exit();
				System.exit(0);
			});
			primaryStage.setTitle("Supermarket Simulator");

			// Create all UI controls (existing + new)

			// --- Existing buttons ---
			startButton = new Button("Start simulation");
			startButton.setOnAction(event -> {
				// Build config from UI fields
				SimulationConfig config = new SimulationConfig();

				// Arrival
				config.arrivalDistType = arrivalDistCombo.getValue();
				config.arrivalMean = Double.parseDouble(arrivalMeanField.getText());
				config.arrivalStd = Double.parseDouble(arrivalStdField.getText());

				// Shopping areas distribution type
				config.shoppingDistType = shoppingDistCombo.getValue();

				// Shopping areas means / stds
				config.produceMean = Double.parseDouble(produceMeanField.getText());
				config.produceStd = Double.parseDouble(produceStdField.getText());
				config.dairyMean = Double.parseDouble(dairyMeanField.getText());
				config.dairyStd = Double.parseDouble(dairyStdField.getText());
				config.groceryMean = Double.parseDouble(groceryMeanField.getText());
				config.groceryStd = Double.parseDouble(groceryStdField.getText());
				config.beveragesMean = Double.parseDouble(beveragesMeanField.getText());
				config.beveragesStd = Double.parseDouble(beveragesStdField.getText());

				// Checkout
				config.regularCheckoutMean = Double.parseDouble(regularMeanField.getText());
				config.regularCheckoutStd = Double.parseDouble(regularStdField.getText());
				config.selfCheckoutMean = Double.parseDouble(selfMeanField.getText());
				config.selfCheckoutStd = Double.parseDouble(selfStdField.getText());

				// Routing probabilities
				config.probProduce = Double.parseDouble(probProduceField.getText());
				config.probDairy = Double.parseDouble(probDairyField.getText());
				config.probGrocery = Double.parseDouble(probGroceryField.getText());
				config.probBeverages = Double.parseDouble(probBeveragesField.getText());

				// Checkout item threshold
				config.selfMaxItems = Integer.parseInt(selfMaxItemsField.getText());

				controller.startSimulation(config);
				startButton.setDisable(true);
			});

			slowButton = new Button("Slow down");
			slowButton.setOnAction(e -> controller.decreaseSpeed());

			speedUpButton = new Button("Speed up");
			speedUpButton.setOnAction(e -> controller.increaseSpeed());

			pauseButton = new Button("Pause");
			pauseButton.setOnAction(e -> controller.pauseSimulation());

			resumeButton = new Button("Resume");
			resumeButton.setOnAction(e -> controller.resumeSimulation());

			stepButton = new Button("Step");
			stepButton.setOnAction(e -> controller.stepSimulation());

			// --- Labels and text fields for simulation time/delay/results (existing) ---
			timeLabel = new Label("Simulation time:");
			timeLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			time = new TextField("Give time");
			time.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			time.setPrefWidth(150);

			delayLabel = new Label("Delay:");
			delayLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			delay = new TextField("Give delay");
			delay.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			delay.setPrefWidth(150);

			resultLabel = new Label("Total time:");
			resultLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			results = new Label();
			results.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			results.setPrefWidth(150);

			// --- New configuration input fields ---
			// Arrival
			arrivalDistCombo = new ComboBox<>();
			arrivalDistCombo.getItems().addAll("negexp", "normal", "uniform");
			arrivalDistCombo.setValue("negexp");
			arrivalMeanField = new TextField("15");
			arrivalStdField = new TextField("5");

			// Shopping area distribution type (shared)
			shoppingDistCombo = new ComboBox<>();
			shoppingDistCombo.getItems().addAll("normal", "negexp", "uniform");
			shoppingDistCombo.setValue("normal");

			// Shopping area means / stds
			produceMeanField = new TextField("8");
			produceStdField = new TextField("2");
			dairyMeanField = new TextField("6");
			dairyStdField = new TextField("2");
			groceryMeanField = new TextField("10");
			groceryStdField = new TextField("3");
			beveragesMeanField = new TextField("7");
			beveragesStdField = new TextField("2");

			// Checkout
			regularMeanField = new TextField("6");
			regularStdField = new TextField("2");
			selfMeanField = new TextField("4");
			selfStdField = new TextField("1");

			// Routing probabilities (sum should be 1, but user can input any)
			probProduceField = new TextField("0.25");
			probDairyField = new TextField("0.25");
			probGroceryField = new TextField("0.25");
			probBeveragesField = new TextField("0.25");

			// Self-checkout item threshold
			selfMaxItemsField = new TextField("10");

			// --- Layout: use GridPane with scrolling if needed (add many rows) ---
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.TOP_LEFT);
			grid.setVgap(8);
			grid.setHgap(10);
			grid.setPadding(new Insets(15, 15, 15, 15));

			int row = 0;
			// Row 0: Simulation time
			grid.add(timeLabel, 0, row);
			grid.add(time, 1, row++);
			// Row 1: Delay
			grid.add(delayLabel, 0, row);
			grid.add(delay, 1, row++);
			// Row 2: Result label
			grid.add(resultLabel, 0, row);
			grid.add(results, 1, row++);
			// Row 3: Start button
			grid.add(startButton, 0, row);
			// Row 4: Speed up / Slow down
			grid.add(speedUpButton, 0, row+1);
			grid.add(slowButton, 1, row+1);
			// Row 5: Pause / Resume / Step
			grid.add(pauseButton, 0, row+2);
			grid.add(resumeButton, 1, row+2);
			grid.add(stepButton, 2, row+2);
			row += 3;

			// Separator
			grid.add(new Separator(), 0, row++, 2, 1);

			// Arrival parameters
			grid.add(new Label("Arrival distribution:"), 0, row);
			grid.add(arrivalDistCombo, 1, row++);
			grid.add(new Label("Arrival mean (negexp) / mean (normal/uniform):"), 0, row);
			grid.add(arrivalMeanField, 1, row++);
			grid.add(new Label("Arrival std (only for normal):"), 0, row);
			grid.add(arrivalStdField, 1, row++);

			grid.add(new Separator(), 0, row++, 2, 1);

			// Shopping area distribution type
			grid.add(new Label("Shopping area distribution type:"), 0, row);
			grid.add(shoppingDistCombo, 1, row++);

			// Produce
			grid.add(new Label("Produce (mean, std):"), 0, row);
			grid.add(new HBox(5, produceMeanField, new Label(","), produceStdField), 1, row++);
			// Dairy
			grid.add(new Label("Dairy (mean, std):"), 0, row);
			grid.add(new HBox(5, dairyMeanField, new Label(","), dairyStdField), 1, row++);
			// Grocery
			grid.add(new Label("Grocery (mean, std):"), 0, row);
			grid.add(new HBox(5, groceryMeanField, new Label(","), groceryStdField), 1, row++);
			// Beverages
			grid.add(new Label("Beverages (mean, std):"), 0, row);
			grid.add(new HBox(5, beveragesMeanField, new Label(","), beveragesStdField), 1, row++);

			grid.add(new Separator(), 0, row++, 2, 1);

			// Checkout
			grid.add(new Label("Regular checkout (mean, std):"), 0, row);
			grid.add(new HBox(5, regularMeanField, new Label(","), regularStdField), 1, row++);
			grid.add(new Label("Self-checkout (mean, std):"), 0, row);
			grid.add(new HBox(5, selfMeanField, new Label(","), selfStdField), 1, row++);

			grid.add(new Separator(), 0, row++, 2, 1);

			// Routing probabilities (shopping area)
			grid.add(new Label("Routing probabilities (sum to 1):"), 0, row);
			grid.add(new Label(""), 1, row++);
			grid.add(new Label("Produce:"), 0, row);
			grid.add(probProduceField, 1, row++);
			grid.add(new Label("Dairy:"), 0, row);
			grid.add(probDairyField, 1, row++);
			grid.add(new Label("Grocery:"), 0, row);
			grid.add(probGroceryField, 1, row++);
			grid.add(new Label("Beverages:"), 0, row);
			grid.add(probBeveragesField, 1, row++);

			grid.add(new Separator(), 0, row++, 2, 1);

			// Checkout item threshold
			grid.add(new Label("Self-checkout max items:"), 0, row);
			grid.add(selfMaxItemsField, 1, row++);

			// Wrap the grid in a ScrollPane to make the window resizable and scrollable
			ScrollPane scrollPane = new ScrollPane(grid);
			scrollPane.setFitToWidth(true);
			scrollPane.setFitToHeight(true);

			// Visualisation area (Canvas) on the right
			display = new Visualisation2(400, 200);

			HBox mainLayout = new HBox(10, scrollPane, (Canvas) display);
			mainLayout.setPadding(new Insets(10));
			Scene scene = new Scene(mainLayout, 900, 700);
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* ISimulatorUI methods (controller calls) */
	@Override
	public double getTime() {
		return Double.parseDouble(time.getText());
	}

	@Override
	public long getDelay() {
		return Long.parseLong(delay.getText());
	}

	@Override
	public void setEndingTime(double time) {
		DecimalFormat formatter = new DecimalFormat("#0.00");
		this.results.setText(formatter.format(time));
	}

	@Override
	public IVisualisation getVisualisation() {
		return display;
	}

	public static void main(String[] args) {
		launch(args);
	}
}