package server;

import gui.server.SQLPassController;
import javafx.application.Application;
import javafx.stage.Stage;
public class ServerGUI extends Application {

	// Static reference to the SQLPassController frame for managing SQL connection
	public static SQLPassController aFrame;
	public static Stage primaryStage;
	// Static reference to the BLibServer for managing the server
	public static BLibServer server;

	/**
	 * Constructor for ServerGUI. Inherited from Application class.
	 */
	public ServerGUI() {
		super(); // Call the superclass constructor
	}

	/**
	 * Main method to launch the JavaFX application.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		launch(args); // Launch the JavaFX application
	}

	/**
	 * Start method to initialize and set up the user interface. This method is
	 * called automatically when the application is launched.
	 * 
	 * @param primaryStage the primary stage for the JavaFX application
	 * @throws Exception if an error occurs during startup
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// Initialize the SQLPassController frame, which manages the SQL password
		aFrame = new SQLPassController();
		ServerGUI.primaryStage=primaryStage;
		// Start the SQLPassController frame and display it in the primary stage
		aFrame.start(primaryStage);
	}
}
