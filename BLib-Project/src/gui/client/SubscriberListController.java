package gui.client;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import logic.Subscriber; 

/**
 * The SubscriberListController handles the management and display of subscribers.
 * It allows searching for subscribers by various fields and viewing detailed subscriber information.
 */
public class SubscriberListController{
	
	private List<Subscriber> allSubscribers = IPController.client.getAllSubscribers();
	
	@FXML
	private TextField txtSearch; // Text field for entering search queries.
	@FXML
	private Label lblError; // Label for displaying error messages to the user.
	@FXML
	private Button btnBack = null; // Button to exit the application.
	@FXML
	private Button btnSearch = null; // Button to trigger the search functionality.
	@FXML
	private TableView<Subscriber> subTable; // Table view displaying the list of subscribers.
	@FXML
	private TableColumn<Subscriber, Integer> idColumn; // Table column for subscriber ID.
	@FXML
	private TableColumn<Subscriber, String> nameColumn; // Table column for subscriber name.
	@FXML
	private TableColumn<Subscriber, String> phoneColumn; // Table column for subscriber phone number.
	@FXML
	private TableColumn<Subscriber, String> emailColumn; // Table column for subscriber email address.
	@FXML
	private TableColumn<Subscriber, String> statusColumn; // Table column for subscriber status.

	
	/**
	 * Handles the search functionality when the "Search" button is clicked.
	 * It filters the subscribers based on the search query and updates the table.
	 * 
	 * @param event The action event triggered by clicking the search button.
	 */
	public void searchBtn(Event event) {
		String id, phone, name, email;
	    ObservableList<Subscriber> data = FXCollections.observableArrayList();
        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        String text = txtSearch.getText().toLowerCase();
        
        // If the search field is empty, display all subscribers.
        if (text.isEmpty()){
        	data.addAll(allSubscribers);
        }
        else {
        	// Filter the list of subscribers based on the search query.
        	for(Subscriber subscriber : allSubscribers) {
	    		id = String.valueOf(subscriber.getId()).toLowerCase();
	    		phone = subscriber.getPhone().toLowerCase();
	    		name = subscriber.getName().toLowerCase();
	    		email = subscriber.getEmail().toLowerCase();
	    		if(id.contains(String.valueOf(text)) ||
	    		   phone.contains(String.valueOf(text)) ||
	    		   name.contains(String.valueOf(text)) ||
	    		   email.contains(String.valueOf(text)))
	    		{
	    			data.add(subscriber); // Add the matching subscriber to the displayed list.
	    		}
	    	}
        }
        subTable.setItems(data);
        
        // Allows double-clicking a row to view detailed subscriber information.
        subTable.setRowFactory(tv -> {
	 		    TableRow<Subscriber> rowa = new TableRow<>();
	 		    rowa.setOnMouseClicked(eventa -> {
	 		        if (eventa.getClickCount() == 2 && !rowa.isEmpty()) {
	 		        	Subscriber rowData = rowa.getItem();
	 		    	    
	 		    		// Load the SubscriberReaderCard page to show detailed information.
	 		    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SubscriberReaderCardFrame" +".fxml"));
	 		    		Parent root = null;
						try {
							root = loader.load();
						} catch (IOException e) {e.printStackTrace();}
	 		    		SubscriberReaderCardController subscriberReaderCardController = loader.getController();
	 		    		subscriberReaderCardController.loadChoiceBox();
	 		    		subscriberReaderCardController.loadSubscriber(rowData);
	 		    		subscriberReaderCardController.loadBorrows(rowData);
	 		    		IPController.client.nextPage(loader, root, event, "Subscriber's Reader Card");
	 		        }
	 		    });
	 	    return rowa ;
	 		});
	}
	
	/**
	 * Handles the "Exit" button action. Navigates back to the librarian's main menu screen.
	 * 
	 * @param event The action event triggered by clicking the "Back" button.
	 */
	public void backBtn(ActionEvent event) {
		// Load the LibrarianClientGUIFrame to return to the librarian's main menu.
		FXMLLoader loader = new FXMLLoader();
		Pane root = null;
		try {
			root = loader.load(getClass().getResource("/gui/client/"+ "LibrarianClientGUIFrame" +".fxml").openStream());
		} catch (IOException e) {e.printStackTrace();}
		LibrarianClientGUIController librarianClientGUIController = loader.getController();
		librarianClientGUIController.loadLibrarian();
		IPController.client.nextPage(loader, root, event, "Librarian Main Menu");
	}
}
