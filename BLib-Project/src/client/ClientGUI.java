package client;

import gui.client.IPController; // Importing the IPController class from the GUI client package.
import javafx.application.Application; // Importing the JavaFX Application class.
import javafx.stage.Stage; // Importing the Stage class from JavaFX.

/**
 * The {@code ClientGUI} class serves as the entry point for launching the graphical
 * user interface (GUI) of the client application. It uses JavaFX for creating
 * and managing the GUI.
 */
public class ClientGUI extends Application {

    /**
     * Default constructor for the {@code ClientGUI} class. 
     * JavaFX requires a no-argument constructor to launch the application.
     */
    public ClientGUI() {
        super(); // Call the superclass constructor.
    }

    /**
     * The main method, which serves as the entry point of the Java application.
     * It invokes the JavaFX `launch` method to initialize and start the GUI application.
     * 
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String args[]) {
        launch(args); // Start the JavaFX application lifecycle.
    }

    /**
     * The {@code start} method is overridden from the {@link Application} class. 
     * It sets up the primary stage (main window) of the JavaFX application and initializes the GUI.
     * 
     * @param primaryStage The primary stage (window) for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        IPController aFrame = new IPController(); // Create an instance of the IPController class.
        aFrame.start(primaryStage); // Start the GUI by passing the primary stage to the IPController.
    }
}
