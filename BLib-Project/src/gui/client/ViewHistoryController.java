package gui.client;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import logic.Activity;
import logic.Subscriber; 

/**
 * The ViewHistoryController class is responsible for displaying the history of activities 
 * performed by a subscriber. It loads the subscriber's activity history from the server 
 * and presents it in a table with details such as the type, description, and date of the activity.
 */
public class ViewHistoryController {
	
	private Subscriber subscriber; // Subscriber object to hold the current subscriber's information.
	
	@FXML
	private Button btnBack = null; // Button to navigate back to the previous screen.
	
	@FXML
	private TableView<Activity> historyTable; // TableView to display the subscriber's activity history.
	
	@FXML
	private TableColumn<Activity, String> typeColumn; // Column to display the type of activity.
	
	@FXML
	private TableColumn<Activity, String> descriptionColumn; // Column to display the description of the activity.
	
	@FXML
	private TableColumn<Activity, LocalDate> dateColumn; // Column to display the date of the activity.

	/**
	 * Loads the activity history of the subscriber and displays it in the historyTable.
	 * 
	 * @param subscriber The subscriber whose activity history is to be loaded.
	 */
	public void loadHistory(Subscriber subscriber) {
		this.subscriber = subscriber;
		// Retrieve the subscriber's activity history from the server.
		List<Activity> activities = IPController.client.getSubscriberHistory(subscriber.getId());
		ObservableList<Activity> data = FXCollections.observableArrayList();
		
		// Populate the table with the retrieved activity data.
		for (Activity ac : activities) {
			data.add(ac);
		}
		
		// Set up the columns to display activity details.
		typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
		
		// Bind the data to the table and enable sorting by date.
		historyTable.setItems(data);
		historyTable.getSortOrder().add(dateColumn);
	}
	
	/**
	 * Handles the "Back" button click event. Navigates the user back to the previous screen.
	 * The navigation depends on the current screen the user is on.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
	public void backBtn(ActionEvent event) {
		// Get the current window stage and title.
		Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    String currentTitle = currentStage.getTitle();
	    
	    // If the current screen is the subscriber's main menu, navigate to it.
	    if (currentTitle.split(" ")[0].equals("Subscriber")) {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/" + "SubscriberClientGUIFrame" + ".fxml"));
	    	Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
	    	SubscriberClientGUIController subscriberClientGUIController = loader.getController();
	    	subscriberClientGUIController.loadSubscriber();
	    	IPController.client.nextPage(loader, root, event, "Subscriber Main Menu");
	    }
	    // Otherwise, navigate to the subscriber's reader card screen.
	    else {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/" + "SubscriberReaderCardFrame" + ".fxml"));
	    	Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {e.printStackTrace();}
			SubscriberReaderCardController subscriberReaderCardController = loader.getController();
			subscriberReaderCardController.loadSubscriber(subscriber);
			subscriberReaderCardController.loadBorrows(subscriber);
			IPController.client.nextPage(loader, root, event, "Subscriber's Reader Card");
	    }
	}
}
