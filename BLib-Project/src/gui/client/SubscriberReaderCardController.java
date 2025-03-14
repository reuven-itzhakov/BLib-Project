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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import logic.Borrow;
import logic.CheckBoxExtension;
import logic.Message;
import logic.Subscriber; 

/**
 * Controller for managing the Subscriber's Reader Card interface.
 * This controller handles displaying subscriber details, borrowing books,
 * extending due dates, and navigating to other views like borrowing history.
 */
public class SubscriberReaderCardController {
	private Subscriber subscriber; // Reference to the currently authenticated subscriber.

	@FXML
	private Label lblTitle; // Label displaying the title with the subscriber's name.
	@FXML
	private Label lblTable; // Label for the table, currently unused.
	@FXML
	private Label lblError; // Label for displaying error messages.
	@FXML
	private Text txtStatus; // Text field to display the subscriber's status.
	@FXML
	private Text txtId; // Text field to display the subscriber's ID.
	@FXML
	private Text txtName; // Text field to display the subscriber's name.
	@FXML
	private Text txtPhone; // Text field to display the subscriber's phone number.
	@FXML
	private Text txtEmail; // Text field to display the subscriber's email.
	@FXML
	private Button btnBack = null; // Button to navigate back to the previous screen.
	@FXML
	private Button btnHistory = null; // Button to navigate to the borrowing history screen.
	@FXML
	private Button btnExtend = null; // Button to extend the borrow period for selected books.
	@FXML
	private TableView<Entry<CheckBoxExtension, Borrow>> tableBook; // Table displaying the borrowed books.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, CheckBox> columnCheckBox; // Column for selecting books.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnBookId; // Column for book ID.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnTitle; // Column for book title.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnAuthor; // Column for book author.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnDueDate; // Column for due date of borrowed books.
	@FXML
	private TableColumn<Entry<CheckBoxExtension, Borrow>, String> columnErrorMessage; // Column for error messages.
	@FXML
	private CheckBox checkBoxSelectAll; // Checkbox to select or deselect all books.
	@FXML
	private ChoiceBox<Integer> choiceBoxDays; // Choice box for selecting the number of days to extend.

	/**
	 * Loads the available days for extending the borrow period into the choice box.
	 * The default value is set to 1 day, with options ranging from 1 to 14 days.
	 */
	public void loadChoiceBox() {
		choiceBoxDays.setValue(1);
		ObservableList<Integer> data = FXCollections.observableArrayList();
		for(Integer number = 1; number < 15; number++) {
			data.add(number);
		}
		choiceBoxDays.setItems(data);
	}
	
	/**
	 * Loads the details of the given subscriber into the text fields and status label.
	 * 
	 * @param subscriber The subscriber whose details are to be displayed.
	 */
	public void loadSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
		this.lblTitle.setText("Subscriber " + subscriber.getName());
		this.txtId.setText(String.valueOf(subscriber.getId()));
		this.txtName.setText(subscriber.getName()); // Sets the subscriber's name.
		this.txtPhone.setText(subscriber.getPhone()); // Sets the subscriber's phone number.
		this.txtEmail.setText(subscriber.getEmail()); // Sets the subscriber's email address.
		if(subscriber.getStatus().equals("active")) {
			this.txtStatus.setFill(Color.GREEN);
		}
		else {
			this.txtStatus.setFill(Color.RED);
		}
		String capitalizedStatus = subscriber.getStatus().substring(0, 1).toUpperCase() + subscriber.getStatus().substring(1);
		this.txtStatus.setText(capitalizedStatus); // Capitalizes and displays the subscriber's status.
	}
	
	/**
	 * Loads the list of borrowed books for the subscriber into the table view.
	 * 
	 * @param subscriber The subscriber whose borrowed books are to be displayed.
	 */
	public void loadBorrows(Subscriber subscriber) {
		ObservableList<Entry<CheckBoxExtension, Borrow>> data = FXCollections.observableArrayList();
		List<Borrow> borrows = IPController.client.getSubscriberBorrows(subscriber);
		
		for (Borrow borrow : borrows) {
			data.add(new SimpleEntry<>(new CheckBoxExtension(), borrow));
		}
		
        columnCheckBox.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getKey().getCheckBox()));
        columnBookId.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getCopyId()));
        columnAuthor.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getAuthor()));
        columnTitle.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getTitleName()));
        columnDueDate.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getValue().getDueDate().toString()));
        columnErrorMessage.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getKey().getErrorMessage()));
        
		tableBook.setItems(data);
		tableBook.getSortOrder().add(columnDueDate); // Sorts the table by due date.
	}
	
	/**
	 * Handles the extend button action. Extends the due date of the selected borrowed books by the number of days specified.
	 * 
	 * @param event The action event triggered when the extend button is clicked.
	 */
	public void extendBtn(ActionEvent event) {
	    for(Entry<CheckBoxExtension, Borrow> entry : tableBook.getItems()) {
	    	Borrow borrow = entry.getValue();
	    	CheckBoxExtension borrowPlus = entry.getKey();
	        if (borrowPlus.getCheckBox().isSelected()) {
	            Message msg = IPController.client.extendDuration(borrow, choiceBoxDays.getValue(), AuthenticationController.librarianName);
	            if (msg.getCommand().equals("failed")) {
                    if(((String) msg.getArguments().get(0)).equals("the subscriber is frozen")) {
                        IPController.client.display(lblError,"Your account is suspended", Color.RED);
                        break;
                    }
                    borrowPlus.setErrorMessage((String) msg.getArguments().get(0)); // Displays error message if extension fails.
                }
	            else {
	            	borrowPlus.setErrorMessage("Extend succeed"); // Displays success message after extension.
	            	borrow.setDueDate(borrow.getDueDate().plusDays(choiceBoxDays.getValue())); // Extends the due date.
	            }
            }
        }
	    checkBoxSelectAll.setSelected(false);
	    selectAllBtn(event); // Deselects the "Select All" checkbox.
	    tableBook.refresh(); // Refreshes the table to show updated data.
	}
	
	/**
	 * Handles the "Select All" checkbox action. Toggles the selection of all checkboxes in the table.
	 * 
	 * @param event The action event triggered when the "Select All" checkbox is clicked.
	 */
	public void selectAllBtn(Event event) {
		for (Entry<CheckBoxExtension, Borrow> entry : tableBook.getItems()) {
			entry.getKey().setCheckBox(checkBoxSelectAll.isSelected());
		}
	}
	
	/**
	 * Navigates to the subscriber's borrowing history view when the history button is clicked.
	 * 
	 * @param event The action event triggered by clicking the history button.
	 */
	public void historyBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "ViewHistoryFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		ViewHistoryController viewHistoryController = loader.getController();
		viewHistoryController.loadHistory(subscriber);
		IPController.client.nextPage(loader, root, event, "Librarian - View History");
	}

	/**
	 * Navigates back to the previous page when the back button is clicked.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
	public void backBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/"+ "SubscriberListFrame" +".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		IPController.client.nextPage(loader, root, event, "List of Subscribers");
	}
}
