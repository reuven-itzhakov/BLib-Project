package gui.server;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;
import server.BLibDBC;
import server.ServerGUI;

public class ServerController implements Initializable {

	// FXML elements for the GUI
	@FXML
	private TextArea text; // TextArea to display connected clients
	@FXML
	private Button exitBtn; // Button to exit the application
	@FXML
	private Button refreshBtn; // Button to manually refresh the client list

	// This method is called when the FXML is loaded
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			// Initially populate the client information by calling refresh method
			refresh(null);

			// Create a new thread that refreshes the client list every 2 seconds
			Thread refresher = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(2000); // Pause for 2 seconds before refreshing
						refresh(null); // Refresh the client list
					} catch (Exception e) {
						// If there's an error in refreshing, log a simple message
						System.out.println("failed to refresh");
					}
				}
			});
			// Mark the refresher thread as a daemon so it will stop when the main thread is
			// stopped
			refresher.setDaemon(true);
			refresher.start(); // Start the thread

		} catch (Exception e) {
			// Log exception or handle as needed
			System.out.println("Initialization failed: " + e.getMessage());
		}
	}

	// Method to refresh the list of connected clients
	public void refresh(ActionEvent e) throws Exception {
		// StringBuilder is used to efficiently construct the output string
		StringBuilder sb = new StringBuilder("");
		boolean isEmpty = true; // Flag to check if there are no clients connected
		int i = 1; // Client index for displaying number

		// Iterate through the list of connected clients from the server
		for (Map.Entry<ConnectionToClient, String[]> c : ServerGUI.server.getConnectedClients().entrySet()) {
			// Append client information (IP, Host, and Connection Status)
			sb.append(i + "#IP    : " + c.getValue()[0] + "\n");
			sb.append(i + "#HOST  : " + c.getValue()[1] + "\n");
			sb.append(i + "#STATUS: " + (c.getKey().isAlive() ? "Connected" : "Disconnected") + "\n\n");
			isEmpty = false; // Set to false since there's at least one client connected
			i++; // Increment the client index
		}

		// If there are no clients connected, display a message
		if (isEmpty) {
			sb.append("no clients connected");
		}

		// Set the TextArea's content to the constructed string
		text.setText(sb.toString());
	}

	// Method to handle the Exit button action
	public void exit(ActionEvent e) throws Exception {
		if(BLibDBC.getInstance() != null) {
			BLibDBC.getInstance().disconnect();
		}
		System.out.println("exiting..."); // Print message for debugging
		System.exit(0); // Exit the application
	}

}