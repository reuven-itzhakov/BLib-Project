package gui.server;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import server.BLibDBC;
import server.BLibServer;
import server.ServerGUI;
import javafx.scene.input.KeyEvent;

public class SQLPassController {

	// FXML elements to be injected from the FXML file
	@FXML
	private Label lblMSG; // Label to display messages to the user (e.g., error or success)
	@FXML
	private TextField txtPass; // TextField for the user to input the password
	@FXML
	private TextField txtPort; // TextField for the user to input the password
	@FXML
	private Button btnClose; // Button to close the window
	@FXML
	private Button btnOK; // Button to confirm the password entry

	// Method to start the stage (window) and show the password prompt
	public void start(Stage primaryStage) throws Exception {
		// Handling close request to ensure the application exits when the window is
		// closed
		primaryStage.setOnCloseRequest((E) -> System.exit(0));
		// Load the FXML layout for this window and set the scene
		Parent root = FXMLLoader.load(getClass().getResource("/gui/server/SQLPass.fxml"));
		Scene scene = new Scene(root);
		
		
		
		
		// Adding the stylesheet to the scene to apply styles
		scene.getStylesheets().add(getClass().getResource("/gui/server/Server.css").toExternalForm());

		// Set the title for the window
		primaryStage.setTitle("Enter Password");
		primaryStage.setScene(scene); // Set the scene for the primary stage
		primaryStage.show(); // Show the window
	}

	// Method to handle the "Close" button click (exits the application)
	public void actionOnClose(ActionEvent e) {
		if(BLibDBC.getInstance() != null) {
			BLibDBC.getInstance().disconnect();
		}
		System.out.println("exiting..."); // Log message to indicate exit
		System.exit(0); // Exit the application
	}

	// Enables the enter key to activate the OK button
	public void handleKey(KeyEvent event) {
		if(event.getCode().equals(KeyCode.ENTER)) {
			actionOnOK(event);
		}
	}

	// Method to handle the "OK" button click
	public void actionOnOK(Event e) {
		
		// Retrieve the entered password and port
		String password = txtPass.getText();
		String portStr= txtPort.getText();
		int portNum;

		// Check if the password field is empty, if so, prompt the user for a password
		if (password.isBlank()) {
			txtPass.setText(""); // Clear the text field
			display("An SQL password must be entered"); // Show the message in lblMSG
			return; // Exit the method without proceeding further
		}

		// Try to establish a connection to the database using the entered password
		if (!BLibServer.connect(password)) {
			// If the connection fails, display an error message
			display("Can't establish connection with DB");
			return; // Exit the method
		}

		// Check if port field is empty, if so, prompt the user for a port
		if (portStr.isBlank()) {
			txtPass.setText(""); // Clear the text field
			display("the port must be entered"); // Show the message in lblMSG
			return; // Exit the method without proceeding further
		}
		
		// Check if the port is numeric and in valid range
		try {
			portNum = Integer.parseInt(portStr.strip()); // Try to parse the port into an integer
			if (portNum < 0) {
				display("the port can't be negative"); // Show the message in lblMSG
				return; // Exit the method without proceeding further
			}
			if (portNum > 65535) {
				display("the port can't be greater than " + 65535); // Show the message in lblMSG
				return; // Exit the method without proceeding further
			}
		}catch(NumberFormatException ex) {
			display("the port must be an integer"); // Show the message in lblMSG
			return; // Exit the method without proceeding further
		}
		
		// If the connection is successful, proceed to set up the next window
		try {
			// Load the server interface window
			FXMLLoader loader = new FXMLLoader();
			ServerGUI.server = new BLibServer(portNum); // Get the server instance

			// Create a new stage (window) for the server interface
			Stage primaryStage = new Stage();
			Pane root = loader.load(getClass().getResource("/gui/server/Server.fxml").openStream());
			Scene scene = new Scene(root);

			// Apply the stylesheet to the new scene
			scene.getStylesheets().add(getClass().getResource("/gui/server/Server.css").toExternalForm());

			// Handle close request for the server window
			primaryStage.setOnCloseRequest((E) -> System.exit(0));

			// Set the title for the new window
			primaryStage.setTitle("BLib Server");

			// Set the scene for the new window and show it
			primaryStage.setScene(scene);
			((Node) e.getSource()).getScene().getWindow().hide(); // Hide the current window
			primaryStage.show(); // Show the server interface window

		} catch (Exception ex) {
			// If there is an exception (e.g., port already in use), display an error
			// message
			display("make sure port " + portNum + " isn't used");
			return; // Exit the method
		}
		
	}
	

	// Method to update the message label with a specific string
	public void display(String str) {
		lblMSG.setText(str); // Set the message text on the label
	}


}