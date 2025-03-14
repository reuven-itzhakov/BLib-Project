package gui.client;

import java.io.IOException;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import logic.BookTitle; 

/**
 * The SearchController class is responsible for handling the book search functionality for both 
 * subscribers and librarians. It manages the search input, retrieves search results, 
 * and displays them in a table. It also handles navigation to book details.
 */
public class SearchController {
	
	@FXML
	private TextField txtSearch; // Field for user input for search query.
	
	@FXML
	private Label lblError; // Label to display error messages.
	
	@FXML
	private Button btnBack = null; // Button to navigate back.
	
	@FXML
	private Button btnSearch = null; // Button to perform search.
	
	@FXML
	private TableView<BookTitle> bookTable; // Table to display search results.
	
	@FXML
	private TableColumn<BookTitle, String> authorColumn; // Column for author.
	
	@FXML
	private TableColumn<BookTitle, String> titleColumn; // Column for book title.

	/**
	 * Handles the search button click and updates the TableView with the search results.
	 * It fetches the books matching the search keyword and displays them in the table.
	 * Additionally, it enables double-click functionality to view more details about the selected book.
	 * 
	 * @param event The action event triggered by clicking the search button.
	 */
	public void searchBtn(Event event) {
		ObservableList<BookTitle> data;
		String keyword = txtSearch.getText();
		
		// Fetch books that match the search keyword.
		Set<BookTitle> bookTitle = IPController.client.getTitlesByKeyword(keyword);
		data = FXCollections.observableArrayList();
		
		// Populate the TableView with the search results.
		for(BookTitle bt : bookTitle) {
			data.add(bt);
		}
		
		// Set the cell value factory for both author and title columns.
		authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("titleName"));
		
		// Bind the data to the table and enable sorting.
		bookTable.setItems(data);
		bookTable.getSortOrder().addAll(authorColumn, titleColumn);
		
		// Allow double-click on row to view book details.
		bookTable.setRowFactory(tv -> {
		    TableRow<BookTitle> row = new TableRow<>();
		    row.setOnMouseClicked(eventa -> {
		        if (eventa.getClickCount() == 2 && !row.isEmpty()) {
		        	BookTitle rowData = row.getItem();
		    		Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		    	    String currentTitle = currentStage.getTitle();
		    	    String[] title = currentTitle.split(" ");
		    	    
			    	// Load the book details page.
			    	FXMLLoader loader = new FXMLLoader();
			    	Pane root = null;
					try {
						root = loader.load(getClass().getResource("/gui/client/"+ "BookTitleFrame" +".fxml").openStream());
					} catch (IOException e) { e.printStackTrace(); }
		    		BookTitleController bookTitleController = loader.getController();
		    		bookTitleController.loadBookTitle(rowData); // Load book details into the controller.
		    		bookTitleController.loadOrderButton(title[0]); // Set the order button based on current user.
					IPController.client.nextPage(loader, root, event, title[0] + " - " + rowData.getTitleName());
		        }
		    });
		    return row;
		});
	}

	/**
	 * Handles the back button click event, navigating to the appropriate page based on the current window's title.
	 * It checks if the current page is for a subscriber or librarian and navigates accordingly, 
	 * or returns to the authentication page if neither is the case.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
	public void backBtn(ActionEvent event){
		Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    String currentTitle = currentStage.getTitle();
	    
	    if(currentTitle.equals("Subscriber - Search")) {
	    	// Navigate to Subscriber Main Menu.
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SubscriberClientGUIFrame" +".fxml"));
	    	Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
	    	SubscriberClientGUIController subscriberClientGUIController = loader.getController();
	    	subscriberClientGUIController.loadSubscriber();
	    	IPController.client.nextPage(loader, root, event, "Subscriber Main Menu");
	    }
	    else if(currentTitle.equals("Librarian - Search")) {
	    	// Navigate to Librarian Main Menu.
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "LibrarianClientGUIFrame" +".fxml"));
	    	Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
			LibrarianClientGUIController librarianClientGUIController = loader.getController();
			librarianClientGUIController.loadLibrarian();
			IPController.client.nextPage(loader, root, event, "Librarian Main Menu");
	    }
	    else {
	    	// Navigate to Authentication page.
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "AuthenticationFrame" +".fxml"));
	    	Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
			AuthenticationController authenticationController = loader.getController();
			authenticationController.loadImage();
			IPController.client.nextPage(loader, root, event, "Authentication");
	    }
	}
	
	/**
	 * Displays an error message in the lblError label with the specified color.
	 * 
	 * @param message The error message to be displayed.
	 * @param color The color to display the message in.
	 */
	public void display(String message, Color color) {
		lblError.setText(message);
		lblError.setTextFill(color);
	}
}
