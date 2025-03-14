package gui.client;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * The ViewMessagesController class is responsible for handling the viewing and clearing of messages 
 * for the librarian. It loads the librarian's messages, displays them in a table, 
 * and provides functionality to clear all messages.
 */
public class ViewMessagesController {
	
	@FXML
	private Button btnBack = null; // Button for exiting the application.
	
	@FXML
	private Button btnClearAllMessages = null; // Button to clear all messages.
	
	@FXML
	private TableView<String> messageTable; // Table to display messages.
	
	@FXML
	private TableColumn<String, String> messageColumn; // Column for displaying the message content.

	/**
	 * Loads the librarian's messages and populates the messageTable.
	 * Retrieves the messages from the server and displays them in the table.
	 */
	public void loadMessages() {
		// Fetch the messages from the server.
		List<String> messages = IPController.client.getLibrarianMessages();
		ObservableList<String> data = FXCollections.observableArrayList(messages);

		// Set up the table to display the messages.
		messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
		messageTable.setItems(data);
	}

	/**
	 * Clears all messages for the librarian and reloads the messages table.
	 * This action removes all messages from the server and refreshes the table to show the updated data.
	 */
	public void clearAllMessages() {
		// Clear all messages from the server.
		IPController.client.clearLibrarianMessages();
		// Reload the messages after clearing.
		loadMessages();
	}

	/**
	 * Handles the "Back" button action. Navigates the user back to the Librarian Main Menu.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
	public void backBtn(ActionEvent event) {
		// Load the Librarian Main Menu page.
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "LibrarianClientGUIFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		
		// Update the message count in the main menu and load the librarian interface.
		LibrarianClientGUIController librarianClientGUIController = loader.getController();
		librarianClientGUIController.updateMessageCount();
		librarianClientGUIController.loadLibrarian();
		
		// Navigate to the Librarian Main Menu page.
		IPController.client.nextPage(loader, root, event, "Librarian Main Menu");
	}

}
