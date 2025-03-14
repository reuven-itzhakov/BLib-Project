package gui.client;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import logic.Message;

/**
 * The ReportsController handles the interaction for generating reports,
 * including the creation of graphs based on user selection for the month,
 * year, and type of report.
 */
public class ReportsController {
	@FXML
	private Button btnBack = null; // Button to navigate back to the previous screen.
	@FXML
	private Button btnGenerateGraph = null; // Button to generate the graph based on selected parameters.
	@FXML
	private ChoiceBox<Integer> choiceBoxMonth; // ChoiceBox for selecting the month for the report.
	@FXML
	private ChoiceBox<Integer> choiceBoxYear; // ChoiceBox for selecting the year for the report.
	@FXML
	private ChoiceBox<String> choiceBoxGraph; // ChoiceBox for selecting the type of report (e.g., borrowing report, subscriber status).
	@FXML
	private Label lblError; // Label to display error messages, if any.

	/**
	 * This method loads the default values into the ChoiceBox elements for the month,
	 * year, and graph type when the report page is opened.
	 */
	public void loadChoiceBoxs() {
		choiceBoxMonth.setValue(1); // Set the default month to January.
		choiceBoxYear.setValue(2025); // Set the default year to 2025.
		choiceBoxGraph.setValue("borrowing report"); // Set the default report type to borrowing report.

		// Add months 1-12 to the month choice box.
		ObservableList<Integer> dataMonth = FXCollections.observableArrayList();
		for (Integer number = 1; number < 13; number++) {
			dataMonth.add(number);
		}
		choiceBoxMonth.setItems(dataMonth); // Set available months.

		// Add year options 2024 and 2025 to the year choice box.
		choiceBoxYear.getItems().addAll(2024, 2025);
		
		// Add the report types ("borrowing report", "subscriber status") to the graph choice box.
		choiceBoxGraph.getItems().addAll("borrowing report", "subscriber status");
	}

	/**
	 * Handles the "Back" button click event. Navigates the user back to the librarian main menu.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void backBtn(ActionEvent event) {
		// Load the librarian main menu page.
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/" + "LibrarianClientGUIFrame" + ".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		LibrarianClientGUIController librarianClientGUIController = loader.getController();
		librarianClientGUIController.loadLibrarian(); // Load librarian's personal data.
		IPController.client.nextPage(loader, root, event, "Librarian Main Menu"); // Transition to librarian menu.
	}

	/**
	 * Handles the "Generate Graph" button click event. This method generates and displays the selected report
	 * (graph) for the chosen month, year, and type of report.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void GenerateGraphBtn(ActionEvent event) {
		// Fetch the graph data using the selected parameters (month, year, report type).
		Message msg = IPController.client.getGraph(choiceBoxYear.getValue(), choiceBoxMonth.getValue(), choiceBoxGraph.getValue());
		
		// If the server returns a failure message (no graph data), display an error.
		if (msg.getCommand().equals("failed")) {
			IPController.client.display(lblError, "No graph in this date", Color.RED); // Display error message.
			return;
		} else {
			// If the graph generation is successful, clear the error and display the graph.
			IPController.client.display(lblError, "", Color.RED);

			// Load the report viewing page to display the generated graph.
			FXMLLoader loader = new FXMLLoader();
			Pane root = null;
			try {
				root = loader.load(getClass().getResource("/gui/client/" + "ShowReportsFrame" + ".fxml").openStream());
			} catch (IOException e) {e.printStackTrace();}
			ShowReportsController showReportsController = loader.getController();
			showReportsController.loadGraphDetails(choiceBoxGraph.getValue(), choiceBoxYear.getValue(), choiceBoxMonth.getValue());
			showReportsController.loadGraph((byte[]) msg.getArguments().get(0)); // Load the graph data.

			// Create a new window to display the graph.
			Stage primaryStage = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/gui/client/stylesheet.css").toExternalForm());
			primaryStage.setTitle("Show Reports"); // Set the window title.
			primaryStage.setScene(scene); // Set the scene for the stage.
			primaryStage.show(); // Show the graph window.
		}
	}
}
