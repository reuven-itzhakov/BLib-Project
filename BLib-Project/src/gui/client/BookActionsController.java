package gui.client;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import logic.BookCopy;
import logic.Message; 

/**
 * The BookActionsController class is responsible for handling the book-related actions 
 * such as borrowing and returning books in the library system. It validates the user input, 
 * communicates with the backend, and displays appropriate messages to the librarian.
 */
public class BookActionsController {

	@FXML
	private Label lblError; // Label for displaying error messages to the user.
	@FXML
	private TextField txtSubscriberId; // TextField for the librarian to enter the subscriber's ID.
	@FXML
	private TextField txtBookId; // TextField for the librarian to enter the book ID.
	@FXML
	private Button btnBack = null; // Button to navigate back to the librarian's main menu.
	@FXML
	private Button btnBorrow = null; // Button to trigger the borrowing process.
	@FXML
	private Button btnReturn = null; // Button to trigger the return process.

	/**
	 * Handles the borrowing process when the librarian clicks the "Borrow" button.
	 *
	 * @param event The ActionEvent triggered by clicking the Borrow button.
	 */
	public void borrowBtn(ActionEvent event) {
		Integer bookID;
		// Validate the input fields for the book ID
	    try {
	        // Attempt to parse the Book ID from the TextField
	    	bookID = Integer.valueOf(txtBookId.getText());
	    } catch (NumberFormatException e) {
	        // Handle invalid Book ID input
	        IPController.client.display(lblError,"Invalid Book ID", Color.RED);
	        return;
	    }
	    
	    Integer subID;
		// Validate the input fields for the subscriber ID
	    try {
	        // Attempt to parse the Subscriber ID from the TextField
	        subID = Integer.valueOf(txtSubscriberId.getText());
	    } catch (NumberFormatException e) {
	        // Handle invalid Subscriber ID input
	        IPController.client.display(lblError,"Invalid subscriber ID", Color.RED);
	        return;
	    }

		// Attempt to create a borrow transaction
	    Message msg = IPController.client.createBorrow(subID, bookID);
	    if(msg.getCommand().equals("failed")) {
	    	// If the borrow operation fails, display the error message
	    	IPController.client.display(lblError,(String)msg.getArguments().get(0), Color.RED);
	    	return;
	    }
	    // If the borrow operation succeeds, display the success message
	    IPController.client.display(lblError,"Borrow succeeded", Color.GREEN);
	}


	/**
	 * Handles the returning process when the librarian clicks the "Return" button.
	 *
	 * @param event The ActionEvent triggered by clicking the Return button.
	 */
	public void returnBtn(ActionEvent event){
		Integer bookID;
		// Validate the input fields for the book ID
	    try {
	        // Attempt to parse the Book ID from the TextField
	    	bookID = Integer.valueOf(txtBookId.getText());
	    } catch (NumberFormatException e) {
	        // Handle invalid Book ID input
	        IPController.client.display(lblError,"Invalid Book ID", Color.RED);
	        return;
	    }

	    // Search for the book copy using the provided Book ID
	    BookCopy searchedBook = IPController.client.getCopyByID(bookID);
	    
	    // Attempt to return the book
	    Message msg = IPController.client.returnBook(searchedBook);
	    
	    if(msg.getCommand().equals("failed")) {
	    	// If the return operation fails, display the error message
	    	IPController.client.display(lblError,(String)msg.getArguments().get(0), Color.RED);
	    	return;
	    }
	    // If the return operation succeeds, display the success message
	    IPController.client.display(lblError,(String)msg.getArguments().get(0), Color.GREEN);
	}
	

	/**
	 * Handles the Exit button click event. Navigates the user back to the librarian's main menu.
	 *
	 * @param event The ActionEvent triggered by clicking the Exit button.
	 */
	public void backBtn(ActionEvent event) {
		// Load the Librarian's main menu interface
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "LibrarianClientGUIFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		LibrarianClientGUIController librarianClientGUIController = loader.getController();
		librarianClientGUIController.loadLibrarian();
		// Navigate to the Librarian's main menu
		IPController.client.nextPage(loader, root, event, "Librarian Main Menu");
	}
}
