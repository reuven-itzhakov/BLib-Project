package gui.client;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import logic.Subscriber;

/**
 * The {@code AuthenticationController} class handles user authentication and login functionality.
 * It manages ID and password validation, processes login attempts, and transitions
 * users to appropriate application interfaces based on their roles.
 */
public class AuthenticationController {
    public static Subscriber subscriber; // Static reference to the currently authenticated subscriber.
    public static String librarianName; // Static reference to the librarian's name if logged in.

    @FXML
    private Label lblError; // Label for displaying error messages to the user.
    @FXML
    private TextField txtId; // TextField for the user to enter their ID.
    @FXML
    private PasswordField txtPassword; // PasswordField for the user to enter their password.
    @FXML
    private Button btnSend = null; // Button for submitting the login form.
    @FXML
    private Button btnGuest = null; // Button for proceeding as a guest.
    @FXML
    private ImageView img; // ImageView for displaying the application's logo.

    /**
     * Loads the application's logo into the ImageView.
     */
    public void loadImage() {
        img.setImage(new Image("/images/logoBackground.png"));
    }

    /**
     * Handles the login process when the user clicks the "Send" button.
     * Validates the input fields, processes the credentials, and redirects the user to the appropriate interface.
     * 
     * @param event The event triggered by the user's action.
     */
    public void sendBtn(Event event) {
        String id; // To store the entered ID.
        int digit_id = 0; // Variable to hold the parsed numeric value of the ID.
        id = txtId.getText(); // Get the entered ID from the TextField.
        String password = txtPassword.getText(); // Get the entered password from the PasswordField.

        try {
            // Attempt to parse the ID as an integer.
            digit_id = Integer.parseInt(id);
        } catch (Exception e) {
            // Display an error message if the ID is not numeric.
            IPController.client.display(lblError, "Invalid ID - only digits allowed", Color.RED);
        }

        // Validate that the ID and password fields are not empty.
        if (id.trim().isEmpty()) {
            IPController.client.display(lblError, "You must enter an ID number", Color.RED);
        } else if (txtPassword.getText().isEmpty()) {
            IPController.client.display(lblError, "You must enter a password", Color.RED);
        } else {
            // If both fields are valid, attempt to log in.
            String name = IPController.client.login(digit_id, password);

            switch (name) {
                case "subscriber":
                    // Handle login for subscribers.
                    subscriber = IPController.client.getSubscriber(digit_id);
                    FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/gui/client/" + "SubscriberClientGUIFrame" + ".fxml"));
					Parent root1 = null;
					try {
						root1 = loader1.load();
					} catch (IOException e) {e.printStackTrace();}
                    SubscriberClientGUIController subscriberClientGUIController = loader1.getController();
                    subscriberClientGUIController.loadSubscriber();
                    IPController.client.nextPage(loader1, root1, event, "Subscriber Main Menu");
                    break;

                case "fail":
                    // Handle login failure.
                    IPController.client.display(lblError, "ID or password is incorrect", Color.RED);
                    break;

                default:
                    // Handle login for librarians.
                    librarianName = name;
                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/gui/client/" + "LibrarianClientGUIFrame" + ".fxml"));
					Parent root2 = null;
					try {
						root2 = loader2.load();
					} catch (IOException e) {e.printStackTrace();}
                    LibrarianClientGUIController librarianClientGUIController = loader2.getController();
                    librarianClientGUIController.loadLibrarian();
                    librarianClientGUIController.updateMessageCount();
                    IPController.client.nextPage(loader2, root2, event, "Librarian Main Menu");
                    break;
            }
        }
    }

    /**
     * Enables the Enter key to trigger the "Send" button's functionality.
     * 
     * @param event The KeyEvent triggered when a key is pressed.
     */
    public void handleKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            sendBtn(event); // Call the send button functionality when Enter is pressed.
        }
    }

    /**
     * Handles the guest button click event. 
     * Redirects the user to the search interface as a guest.
     * 
     * @param event The ActionEvent triggered by the guest button click.
     */
    public void guestBtn(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        Pane root = null;
		try {
			root = loader.load(getClass().getResource("/gui/client/" + "SearchFrame" + ".fxml").openStream());
		} catch (IOException e) {e.printStackTrace();}
        IPController.client.nextPage(loader, root, event, "Guest - Search");
    }
}
