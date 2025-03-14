package gui.client;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import logic.BookCopy;
import logic.BookTitle;
import logic.Message;

/**
 * The BookTitleController class handles the display and interaction with a specific book title.
 * It manages the UI for viewing the book details, ordering a book, and navigating to other screens.
 */
public class BookTitleController {
	private BookTitle bt; // The current BookTitle object being displayed

	// UI elements defined in the FXML file.
	@FXML
	private Label lblTitle; // Label for displaying the book's title.
	@FXML
	private Label lblError; // Label for displaying error messages.
	@FXML
	private Label lblDueDate; // Label for displaying the closest return date of the book copies.
	@FXML
	private Text txtAuthorName; // Text for displaying the author's name.
	@FXML
	private Text txtGenre; // Text for displaying the book's genre.
	@FXML
	private Text txtDescription; // Text for displaying the book's description.
	@FXML
	private Button btnBack = null; // Button to navigate back to the previous screen.
	@FXML
	private Button btnOrder = null; // Button to order the book.
	@FXML
	private TableView<BookCopy> bookTable; // Table for displaying available copies of the book.
	@FXML
	private TableColumn<BookCopy, Integer> columnBookId; // Column for displaying the book copy ID.
	@FXML
	private TableColumn<BookCopy, String> columnShelf; // Column for displaying the shelf location of the book copy.

	/**
	 * Handles the Exit button click event. Navigates back to the previous screen.
	 * 
	 * @param event The ActionEvent triggered by clicking the Exit button.
	 */
	public void backBtn(ActionEvent event) {
		// Get the current stage and title
		Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    String currentTitle = currentStage.getTitle();
	    String title;
	    if(currentTitle.split(" ")[0].equals("Subscriber")) {
	    	title = "Subscriber - Search";
	    }
	    else if(currentTitle.split(" ")[0].equals("Librarian")) {
	    	title = "Librarian - Search";
	    }
	    else {
	    	title = "Guest - Search";
	    }
		// Load the search frame and navigate back to it
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SearchFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		IPController.client.nextPage(loader, root, event, title);
	}

	/**
	 * Handles the Order button click event. Allows the user to order the book.
	 *
	 * @param event The ActionEvent triggered by clicking the Order button.
	 */
	public void orderBtn(ActionEvent event){
		// Try to order the book
		Message msg = IPController.client.orderTitle(AuthenticationController.subscriber, bt);
		if(msg.getCommand().equals("success")) {
			// On success, navigate back to the search screen with a success message
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SearchFrame" +".fxml"));
			Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
    		SearchController searchController = loader.getController();
    		searchController.display("Order succeed.", Color.GREEN);
    		IPController.client.nextPage(loader, root, event, "Subscriber - Search");
		} else {
			// On failure, display the error message
			IPController.client.display(lblError,(String)msg.getArguments().get(0), Color.RED);
		}
	}

	/**
	 * Loads the details of the book into the UI.
	 *
	 * @param bt1 The book title to load into the UI.
	 */
	public void loadBookTitle(BookTitle bt1) {
		// Create an observable list to store the available book copies
		ObservableList<BookCopy> data;
		Set<BookCopy> bookCopy = IPController.client.getCopiesByTitle(bt1);
		data = FXCollections.observableArrayList();
		for(BookCopy bc : bookCopy) {
			if(!bc.isBorrowed()) // Only add non-borrowed copies
				data.add(bc);
		}
		// If no available copies, show the closest return date
		if(data.isEmpty()) {
			LocalDate dueDate = IPController.client.getTitleClosestReturnDate(bt1);
			lblDueDate.setText("Closest date of return is: " + dueDate.toString());
		} else {
			// Otherwise, display available copies in the table
			columnBookId.setCellValueFactory(new PropertyValueFactory<>("copyID"));
			columnShelf.setCellValueFactory(new PropertyValueFactory<>("shelf"));
			bookTable.setItems(data);
			bookTable.getSortOrder().add(columnBookId);
		}
		// Set book details
		this.bt = bt1;
		this.lblTitle.setText(String.valueOf(bt.getTitleName()));
		this.txtAuthorName.setText(bt.getAuthorName());
		this.txtDescription.setText(bt.getDescription());
		this.txtGenre.setText(bt.getGenre());
	}

	/**
	 * Sets the visibility of the "Order" button based on the user's role.
	 *
	 * @param title The role or title of the current user (e.g., Subscriber, Librarian, Guest).
	 */
	public void loadOrderButton(String title) {
		// If the user is a guest or librarian, hide the order button
		if(title.equals("Guest") || title.equals("Librarian")) {
			btnOrder.setVisible(false);
		} else {
			btnOrder.setVisible(true); // Show the order button for subscribers
		}
	}
}
