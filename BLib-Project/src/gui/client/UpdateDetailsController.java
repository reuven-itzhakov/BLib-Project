package gui.client;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import logic.Subscriber;

public class UpdateDetailsController {
	private Subscriber s; // Holds the Subscriber instance associated with this controller.

	// Labels to display error messages and subscriber details.
	@FXML
	private Label lblError;
	@FXML
	private Label lblStatus;

	// Text fields to edit subscriber details.
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtPhone;
	@FXML
	private TextField txtEmail;

	// Buttons for closing the application and saving subscriber details.
	@FXML
	private Button btnBack = null;
	@FXML
	private Button btnSave = null;

	/**
	 * Loads the given Subscriber's details into the text fields.
	 * This method sets the values of the Subscriber's ID, name, phone, and email
	 * into the respective text fields and sets the status label accordingly.
	 * 
	 * @param s1 The Subscriber object to be displayed.
	 */
	public void loadSubscriber(Subscriber s1) {
		this.s = s1; // Assigns the subscriber to the controller.
		this.txtId.setText(String.valueOf(s.getId())); // Sets the subscriber's ID.
		this.txtName.setText(s.getName()); // Sets the subscriber's name.
		this.txtPhone.setText(s.getPhone()); // Sets the subscriber's phone.
		this.txtEmail.setText(s.getEmail()); // Sets the subscriber's email.
		if (s.getStatus().equals("active")) {
			this.lblStatus.setTextFill(Color.GREEN);
		} else {
			this.lblStatus.setTextFill(Color.RED);
		}
		String capitalizedStatus = s.getStatus().substring(0, 1).toUpperCase() + s.getStatus().substring(1);
		this.lblStatus.setText(capitalizedStatus);
	}

	/**
	 * Validates and saves the updated subscriber details.
	 * This method checks if there are any changes in the phone or email fields, 
	 * validates them, and saves the updated information if valid.
	 * 
	 * @param event The ActionEvent triggered by clicking the Save button.
	 */
	public void saveBtn(Event event) {
		boolean changed = false;
		
		// Validate phone number if changed.
		if (!txtPhone.getText().equals(s.getPhone())) {
			try {
				Long.parseLong(txtPhone.getText()); // Validates that the phone number contains only digits.
			} catch (Exception e) {
				IPController.client.display(lblError, "Phone must have only digits", Color.RED); // Error message for invalid phone number.
				return;
			}
			changed = true;
		}
		
		// Validate email if changed.
		if (!txtEmail.getText().equals(s.getEmail())) {
			String regex = "^[A-Za-z0-9.]{1,99}@" + "[A-Za-z0-9]{1,99}" + "(?:\\.[A-Za-z0-9]{1,99}){0,99}" + "\\.[A-Za-z]{1,}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(txtEmail.getText());

			if (!matcher.matches()) {
				IPController.client.display(lblError, "Email not valid", Color.RED); // Error message for invalid email.
				return;
			}
			changed = true;
		}

		// If changes were made, update and save.
		if (changed) {
			this.s.setEmail(txtEmail.getText());
			this.s.setPhone(txtPhone.getText());

			// Attempt to save the updated subscriber details.
			if (IPController.client.updateSubscriber(s)) {
				IPController.client.display(lblError, "Saved Successfully!", Color.GREEN); // Success message.
				return;
			}
			// Error message if saving fails.
			IPController.client.display(lblError, "Could not save", Color.RED);
			return;
		}
		
		// If no changes were made, display an error message.
		IPController.client.display(lblError, "You didn't change anything", Color.RED);
	}

	/**
	 * Closes the application when the Close button is clicked.
	 * This method navigates the user back to the subscriber's main menu screen.
	 * 
	 * @param event The ActionEvent triggered by clicking the Close button.
	 */
	public void backBtn(ActionEvent event){
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/client/" + "SubscriberClientGUIFrame" + ".fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		SubscriberClientGUIController subscriberClientGUIController = loader.getController();
		subscriberClientGUIController.loadSubscriber();
		IPController.client.nextPage(loader, root, event, "Subscriber Main Menu");
	}
	
	/**
	 * Enables the enter key to activate the Save button.
	 * This method listens for the ENTER key press and triggers the save action.
	 * 
	 * @param event The KeyEvent triggered by pressing a key.
	 */
	public void handleKey(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER)) {
			saveBtn(event); // Calls the saveBtn method when ENTER is pressed.
		}
	}
}
