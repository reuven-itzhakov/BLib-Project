package gui.client;

import java.io.IOException;

import client.BLibClient;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The IPController class handles the initial connection to the server, 
 * including validating the IP address and port input, and navigating 
 * to the authentication screen once the connection is successful.
 */
public class IPController {

	// Static client instance for handling server communication.
	public static BLibClient client;

	// UI elements defined in the FXML file.
	@FXML
	private Label lblError; // Label for displaying error messages.
	@FXML
	private TextField txtIp; // TextField for entering the server IP address.
	@FXML
	private TextField txtPort; // TextField for entering the server port.
	@FXML
	private Button btnExit = null; // Button for exiting the application.
	@FXML
	private Button btnSend = null; // Button to initiate the connection to the server.

	/**
	 * Default constructor. Required for JavaFX to initialize the controller.
	 */
	public IPController() {
		super();
	}

	/**
	 * Handles the Send button click event. Attempts to connect to the server using
	 * the provided IP address and port.
	 * 
	 * @param event The ActionEvent triggered by clicking the Send button.
	 */
	public void sendBtn(Event event) {
		String ip; // Holds the entered IP address.
		String port; // Holds the entered Port.
		int digit_port;
		ip = txtIp.getText(); // Retrieves the entered IP address.
		port = txtPort.getText(); // Retrieves the entered Port.

		// Validates the IP address input.
		if (ip.trim().isEmpty()) {
			IPController.client.display(lblError, "You must enter an IP Address", Color.RED); // Displays an error message if the input is empty.
		}
		else if (port.trim().isEmpty()) {
			IPController.client.display(lblError,"You must enter Port", Color.RED); // Displays an error message if the input is empty.
		}
		else {
			try {
				digit_port = Integer.parseInt(txtPort.getText()); // Validates that the Port contains only digits.
			} catch (Exception e) {
				IPController.client.display(lblError,"Port must have only digits", Color.RED); // Displays an error message for invalid Port.
				return;
			}
			try {
				// Attempts to create a client instance and connect to the server.
				client = new BLibClient(ip, digit_port);
				System.out.println("IP Entered Successfully");

				// Load the Authentication screen if the connection is successful.
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "AuthenticationFrame" +".fxml"));
				Parent root = loader.load();
				AuthenticationController authenticationController = loader.getController();
				authenticationController.loadImage();
				IPController.client.nextPage(loader, root, event, "Authentication");
				
			} catch (Exception e) {
				// Handles connection errors.
				System.out.println("Error: Can't setup connection! Terminating client.");
				display(lblError,"Can't setup connection", Color.RED); // Displays an error message.
			}
		}
	}

	/**
	 * Starts the IPController application. Initializes and displays the primary
	 * stage.
	 * 
	 * @param primaryStage The primary stage of the application.
	 */
	public void start(Stage primaryStage) {
		primaryStage.setOnCloseRequest((E) -> System.exit(0)); // Ensures the application exits on close.
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/gui/client/IPFrame.fxml"));
		} catch (IOException e) {e.printStackTrace();} // Loads the FXML file.
		Scene scene = new Scene(root); // Creates the scene with the loaded FXML.
		scene.getStylesheets().add(getClass().getResource("/gui/client/stylesheet.css").toExternalForm()); // Adds the stylesheet
		primaryStage.setTitle("IP"); // Sets the window title.
		primaryStage.setScene(scene);
		primaryStage.show(); // Displays the primary stage.
	}

	/**
	 * Handles the Exit button click event. Closes the application.
	 * 
	 * @param event The ActionEvent triggered by clicking the Exit button.
	 */
	public void exitBtn(ActionEvent event) {
		System.out.println("Exit Successfully"); // Logs the exit event.
		System.exit(0); // Exits the application.
	}
	
	/**
	 * Enables the enter key to trigger the send button functionality.
	 *
	 * @param event The KeyEvent triggered by pressing a key.
	 */
	public void handleKey(KeyEvent event) {
		if(event.getCode().equals(KeyCode.ENTER)) {
			sendBtn(event); // Calls sendBtn when ENTER key is pressed.
		}
	}

	/**
	 * Displays an error message with the specified color in the error label.
	 *
	 * @param lblError The Label to display the error message in.
	 * @param message The error message to display.
	 * @param color The color to display the error message in.
	 */
	public void display(Label lblError, String message, Color color) {
		lblError.setText(message); // Sets the error message in the label.
		lblError.setTextFill(color); // Sets the text color for the error message.
	}
}
