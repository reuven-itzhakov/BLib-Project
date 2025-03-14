package gui.client;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import logic.Borrow;
import logic.CheckBoxExtension;
import logic.Message;
import logic.Subscriber;

/**
 * The ExtendTimeController class handles the process of extending borrow durations for subscribers.
 * It manages the display of borrows, allows the user to select borrows for extension, and provides feedback
 * on the success or failure of the operation.
 */
public class ExtendTimeController {

	@FXML
	private Label lblError; // Label for displaying error messages to the user.
	@FXML
	private Button btnBack = null; // Button to navigate back to the main menu.
	@FXML
	private Button btnExtend = null; // Button to extend the borrow period for selected books.
	@FXML
	private TableView<Entry<CheckBoxExtension, Borrow>> tableBook; // TableView for displaying borrow information.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, CheckBox> columnCheckBox; // Checkbox column for selecting borrows.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnAuthor; // Column for displaying book authors.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnTitle; // Column for displaying book titles.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnDueDate; // Column for displaying due dates.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnErrorMessage; // Column for displaying error messages.
	@FXML
	private CheckBox checkBoxSelectAll; // Checkbox to select or deselect all rows in the table.

	/**
	 * Handles the "Exit" button action. Navigates the user back to the subscriber's main menu.
	 * 
	 * @param event The action event triggered by clicking the button.
	 */
	public void backBtn(ActionEvent event){
    	// Load the subscriber's main menu interface
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SubscriberClientGUIFrame" +".fxml"));
    	Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
    	SubscriberClientGUIController subscriberClientGUIController = loader.getController();
    	subscriberClientGUIController.loadSubscriber();
    	// Navigate to the main menu
    	IPController.client.nextPage(loader, root, event, "Subscriber Main Menu");
	}

	/**
	 * Handles the "Extend" button action. Extends the selected borrows by 7 days.
	 *
	 * @param event The action event triggered by clicking the button.
	 */
	public void extendBtn(ActionEvent event) {
		// Extend the selected borrows by 7 days
	    for(Entry<CheckBoxExtension, Borrow> entry : tableBook.getItems()) {
	    	Borrow borrow = entry.getValue();
	    	CheckBoxExtension borrowPlus = entry.getKey();
	        if (borrowPlus.getCheckBox().isSelected()) {
	            // Attempt to extend the borrow duration
	            Message msg = IPController.client.extendDuration(borrow, 7, "subscriber");
	            if (msg.getCommand().equals("failed")) {
                    // If the extension fails, display the error message
                    if(((String) msg.getArguments().get(0)).equals("the subscriber is frozen")) {
                        IPController.client.display(lblError,"Your account is suspended", Color.RED);
                        break;
                    }
                    borrowPlus.setErrorMessage((String) msg.getArguments().get(0)); // Set error message for failed extension
                } else {
	            	borrowPlus.setErrorMessage("Extend succeed"); // Success message for extension
	            	borrow.setDueDate(borrow.getDueDate().plusDays(7)); // Update the due date for the borrow
	            }
            }
        }
		// Refresh the table after processing the extension
	    checkBoxSelectAll.setSelected(false);
	    selectAllBtn(event); // Deselect all checkboxes
	    tableBook.refresh(); // Refresh the table to reflect the updated data
	}

	/**
	 * Loads the borrows of the subscriber into the table for display.
	 *
	 * @param subscriber The subscriber whose borrows are to be loaded.
	 */
	public void loadBorrows(Subscriber subscriber) {
		// Create an observable list to store the borrows and their associated error messages
		ObservableList<Entry<CheckBoxExtension, Borrow>> data = FXCollections.observableArrayList();
		List<Borrow> borrows = IPController.client.getSubscriberBorrows(subscriber);
		
		for (Borrow borrow : borrows) {
			data.add(new SimpleEntry<>(new CheckBoxExtension(), borrow)); // Add each borrow with an associated BorrowPlus object
		}
		
        // Set cell value factories for the table columns
        columnCheckBox.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getKey().getCheckBox()));
        columnAuthor.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getAuthor()));
        columnTitle.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getTitleName()));
        columnDueDate.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getDueDate().toString()));
        columnErrorMessage.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getKey().getErrorMessage()));
        
		// Set the data to the table and sort by due date
		tableBook.setItems(data);
		tableBook.getSortOrder().add(columnDueDate);
	}

	/**
	 * Handles the "Select All" button action. Selects or deselects all rows in the table.
	 *
	 * @param event The action event triggered by clicking the button.
	 */
	public void selectAllBtn(Event event) {
		// Set the selected state of all checkboxes in the table to match the "Select All" checkbox
		for (Entry<CheckBoxExtension, Borrow> entry : tableBook.getItems()) {
			entry.getKey().setCheckBox(checkBoxSelectAll.isSelected());
		}
	}
}
