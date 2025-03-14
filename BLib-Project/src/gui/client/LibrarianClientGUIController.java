package gui.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * LibrarianClientGUIController is responsible for handling the interactions
 * of the librarian interface. It includes methods for navigating through 
 * different sections of the application, updating message counts, and initializing the GUI.
 */
public class LibrarianClientGUIController implements Initializable{
	boolean flag = true; // Flag for managing background thread.

	// FXML UI elements
	@FXML
	private Label lblTitle; // Label to display librarian's welcome message.
	@FXML
	private Button btnBack = null; 
	@FXML
	private Button btnUpdateDetails = null; 
	@FXML
	private Button btnViewHistory = null; 
	@FXML
	private Button btnSearch = null; 
	@FXML
	private Button btnSignUp = null;
	@FXML
	private Button btnBookActions = null;
	@FXML
	private Button btnReports = null;
	@FXML
	private Button btnBorrowRep = null;
//	@FXML
//	private Button messagesBtn = null; // Button for navigating to the messages section.
	@FXML
	private Label lblNumMessages; // Label to display the number of unread messages.

	/**
	 * loadLibrarian method loads the librarian's information and sets the welcome message.
	 */
	public void loadLibrarian() {
		this.lblTitle.setText("Welcome, "+AuthenticationController.librarianName+"!");
	}

	/**
	 * updateMessageCount method updates the number of unread messages for the librarian.
	 * This method retrieves the messages and updates the message count on the UI.
	 */
	public void updateMessageCount() {
		List<String> messages = IPController.client.getLibrarianMessages(); // Retrieve messages.
		int totalCount = messages.size(); // Get the total number of messages.
		Platform.runLater(() -> lblNumMessages.setText(String.valueOf(totalCount))); // Update message count in the UI thread.
	}

	/**
	 * Handles the action when the "Messages" button is clicked. It loads the 
	 * "ViewMessagesFrame" to display the librarian's messages.
	 * 
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void messagesBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "ViewMessagesFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		ViewMessagesController viewMessagesController = loader.getController();
		viewMessagesController.loadMessages(); // Load messages.
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "View Messages");
	}

	/**
	 * Handles the "Back" button action. It returns the user to the authentication page.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void backBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "AuthenticationFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		AuthenticationController authenticationController = loader.getController();
		authenticationController.loadImage(); // Load authentication screen.
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "Authentication");
	}

	/**
	 * Handles the "Sign Up" button action. It opens the sign-up page for subscribers.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void signUpBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SignUpFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "Sign Up Subscriber");
	}

	/**
	 * Handles the "Subscribers" button action. It transitions the user to the subscriber list page.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void subscribersBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SubscriberListFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "List of Subscribers");
	}

	/**
	 * Handles the "Search" button action. It opens the search page for books or titles.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void searchBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SearchFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "Librarian - Search");
	}

	/**
	 * Handles the "Book Actions" button action. It transitions the user to the book actions page.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void bookActionsBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "BookActionsFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "Book Actions");
	}

	/**
	 * Handles the "Reports" button action. It opens the report selection page.
	 *
	 * @param event The ActionEvent triggered by clicking the button.
	 */
	public void ReportsBtn(ActionEvent event){
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "ReportsFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		ReportsController reportsController = loader.getController();
		reportsController.loadChoiceBoxs(); // Load available reports.
		cleanUp(); // Clean up before transitioning to the next page.
		IPController.client.nextPage(loader, root, event, "Choose Report");
	}

	/**
	 * initialize method initializes the librarian client GUI. It starts a background thread
	 * to periodically update the message count every minute.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		flag = true;
		try {
			// Initially populate the client information by calling refresh method.
			updateMessageCount();

			// Create a new thread that refreshes the client list every minute.
			Thread refresher = new Thread(() -> {
				while (flag) {
					try {
						Thread.sleep(60*1000); // Pause for 1 minute before refreshing.
						updateMessageCount();
					} catch (Exception e) {
						// If there's an error in refreshing, log a simple message.
						System.out.println("failed to refresh");
					}
				}
			});
			// Mark the refresher thread as a daemon so it will stop when the main thread is stopped.
			refresher.setDaemon(true);
			refresher.start(); // Start the thread.
		} catch (Exception e) {
			// Log exception or handle as needed.
			System.out.println("Initialization failed: " + e.getMessage());
		}
	}

	/**
	 * cleanUp method cleans up the GUI. It stops the background thread and prevents
	 * further operations.
	 */
	public void cleanUp() {
		this.flag = false; // Stop the background thread from running.
	}
}
