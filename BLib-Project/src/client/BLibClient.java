package client;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import logic.Activity;
import logic.BookCopy;
import logic.BookTitle;
import logic.Borrow;
import logic.Message;
import logic.Subscriber;
import ocsf.client.AbstractClient;

public class BLibClient extends AbstractClient {

	// Static variables to store the message and await response status
	public static Message msg;
	public static boolean awaitResponse = false;

	/**
	 * Constructor to initialize the client and open a connection.
	 * 
	 * @param host the host address to connect to
	 * @param port the port to connect to
	 * @throws Exception if an error occurs while opening the connection
	 */
	public BLibClient(String host, int port) throws Exception {
		super(host, port);
		try {
			openConnection(); // Attempt to open a connection
		} catch (IOException exception) {
			throw new Exception(); // Handle connection errors
		}
	}

	/**
	 * Handles messages received from the server.
	 * 
	 * @param msg the message received from the server
	 */
	@Override
	protected void handleMessageFromServer(Object msg) {
		BLibClient.msg = (Message) msg; // Update static message variable
		awaitResponse = false; // Set response status to false
	}

	/**
	 * Handles messages sent from the client UI to the server.
	 * 
	 * @param message The message from the UI.
	 */
	public void handleMessageFromClientUI(Object message) {
		try {
			awaitResponse = true; // Indicate that a response is awaited
			sendToServer(message); // Send the message to the server
			// Wait for a response in a loop
			while (awaitResponse) {
				try {
					Thread.sleep(100); // Avoid busy-waiting
				} catch (InterruptedException e) {
					e.printStackTrace(); // Handle interruptions
				}
			}
		} catch (IOException e) {
			e.printStackTrace(); // Handle errors during communication
			System.out.println("Could not send message to server: Terminating client." + e);
			quit(); // Terminate the client
		}
	}

	/**
	 * Terminates the client by closing the connection and exiting the program.
	 */
	public void quit() {
		try {
			closeConnection(); // Close the connection
		} catch (IOException e) {
		}
		System.exit(0); // Exit the program
	}

	/**
	 * Handles the login process for a user.
	 * 
	 * @param userName The user's username.
	 * @param password The user's password.
	 * @return The login status ("fail" or a success message).
	 */
	public String login(int userName, String password) {
		msg = new Message("login", userName, password); // Create login message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("loginSuccess")) { // Check for success
			return (String) msg.getArguments().get(0);
		}
		return "fail"; // Login failed
	}

	/**
	 * Searches for books by a keyword.
	 * 
	 * @param keyword the keyword to search for
	 * @return a set of matching book titles, or null if no results are found
	 */
	public Set<BookTitle> getTitlesByKeyword(String keyword) {
		msg = new Message("getTitlesByKeyword", keyword); // Create search message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("searchResult")) {
			return (Set<BookTitle>) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Retrieves the book copies for a given title.
	 * 
	 * @param bt the book title to search for
	 * @return a set of book copies for the title, or null if no results are found
	 */
	public Set<BookCopy> getCopiesByTitle(BookTitle bt) {
		msg = new Message("getCopiesByTitle", bt); // Create search message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("searchResult")) {
			return (Set<BookCopy>) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Retrieves the active borrow for a given book copy.
	 * 
	 * @param bc the book copy to check for an active borrow
	 * @return the active borrow, or null if no active borrow exists
	 */
	public Borrow getCopyActiveBorrow(BookCopy bc) {
		msg = new Message("getCopyActiveBorrow", bc); // Create search message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("borrowFound")) {
			return (Borrow) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Borrows a book for a subscriber.
	 * 
	 * @param subID the subscriber ID
	 * @param bookID the book ID
	 * @return the message result of the borrow request
	 */
	public Message createBorrow(Integer subID, Integer bookID) {
		msg = new Message("createBorrow", subID, bookID); // Create borrow message
		handleMessageFromClientUI(msg); // Send to server
		return msg;
	}

	/**
	 * Extends the borrowing duration for a book.
	 * 
	 * @param borrow the borrow object to extend
	 * @param days the number of days to extend the duration
	 * @param type the type of extension
	 * @return the message result of the extension request
	 */
	public Message extendDuration(Borrow borrow, int days, String type) {
		msg = new Message("extend", borrow, days, type); // Create message
		handleMessageFromClientUI(msg); // Send to server
		return msg; // Return message result
	}

	/**
	 * Returns a borrowed book.
	 * 
	 * @param book the book copy to return
	 * @return the message result of the return request
	 */
	public Message returnBook(BookCopy book) {
		msg = new Message("return", book); // Create return message
		handleMessageFromClientUI(msg); // Send to server
		return msg;
	}

	/**
	 * Retrieves the subscriber's activity history.
	 * 
	 * @param subID the subscriber ID
	 * @return the list of activities, or null if no activities are found
	 */
	public List<Activity> getSubscriberHistory(int subID) {
		msg = new Message("history", subID); // Create history message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("historyRetrieved")) {
			return (List<Activity>) msg.getArguments().get(0); // Return activity history
		}
		return null; // Placeholder return value
	}

	/**
	 * Retrieves all librarian messages.
	 * 
	 * @return a list of librarian messages, or null if no messages are found
	 */
	public List<String> getLibrarianMessages() {
		msg = new Message("getLibrarianMessages"); // Create message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("success")) {
			return (List<String>) msg.getArguments().get(0);
		}
		return null; // Placeholder return value
	}

	/**
	 * Clears all librarian messages.
	 * 
	 * @return the result of the clear message request
	 */
	public String clearLibrarianMessages() {
		msg = new Message("clearLibrarianMessages"); // Create message
		handleMessageFromClientUI(msg); // Send to server
		return (String) msg.getCommand();
	}

	/**
	 * Updates subscriber information.
	 * 
	 * @param updated the updated subscriber object
	 * @return true if the update was successful, false otherwise
	 */
	public boolean updateSubscriber(Subscriber updated) {
		msg = new Message("updateSubscriber", updated, "subscriber"); // Create update message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("subscriberUpdated")) {
			return true;
		}
		return false; // Update failed
	}

	/**
	 * Retrieves a subscriber by their ID.
	 * 
	 * @param id the subscriber's ID
	 * @return the subscriber object if found, null otherwise
	 */
	public Subscriber getSubscriber(int id) {
		msg = new Message("getSubscriber", id); // Create getSubscriber message
		handleMessageFromClientUI(msg); // Send to server
		if (msg.getCommand().equals("subscriberFound")) {
			return (Subscriber) msg.getArguments().get(0); // Return subscriber
		}
		return null; // Subscriber not found
	}

	/**
	 * Retrieves the number of allowed extensions for a book title.
	 * 
	 * @param bt the book title to check
	 * @return the number of allowed extensions, or null if not found
	 */
	public Integer getTitleNumOfAllowedExtend(BookTitle bt) {
		msg = new Message("getTitleNumOfAllowedExtend", bt);
		handleMessageFromClientUI(msg);
		if (msg.getCommand().equals("allowedOrder")) {
			return (Integer) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Orders a book title for a subscriber.
	 * 
	 * @param sub the subscriber ordering the book
	 * @param bt the book title to order
	 * @return the message result of the order request
	 */
	public Message orderTitle(Subscriber sub, BookTitle bt) {
		msg = new Message("order", sub, bt);
		handleMessageFromClientUI(msg);
		return msg;
	}

	/**
	 * Retrieves a list of borrowed books for a subscriber.
	 * 
	 * @param subscriber the subscriber whose borrows are to be retrieved
	 * @return a list of borrows, or null if no borrows are found
	 */
	public List<Borrow> getSubscriberBorrows(Subscriber subscriber) {
		msg = new Message("getSubscriberBorrows", subscriber);
		handleMessageFromClientUI(msg);
		if (msg.getCommand().equals("success")) {
			return (List<Borrow>) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Retrieves all subscribers in the system.
	 * 
	 * @return a list of all subscribers, or null if no subscribers are found
	 */
	public List<Subscriber> getAllSubscribers() {
		msg = new Message("getAllSubscribers");
		handleMessageFromClientUI(msg);
		if (msg.getCommand().equals("success")) {
			return (List<Subscriber>) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Retrieves a book copy by its ID.
	 * 
	 * @param id the book copy's ID
	 * @return the book copy if found, null otherwise
	 */
	public BookCopy getCopyByID(int id) {
		msg = new Message("getCopyByID", id);
		handleMessageFromClientUI(msg);
		if (msg.getCommand().equals("success")) {
			return (BookCopy) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Retrieves the closest return date for a book title.
	 * 
	 * @param bt the book title to check
	 * @return the closest return date, or null if not found
	 */
	public LocalDate getTitleClosestReturnDate(BookTitle bt) {
		msg = new Message("getTitleClosestReturnDate", bt);
		handleMessageFromClientUI(msg);
		if (msg.getCommand().equals("success")) {
			return (LocalDate) msg.getArguments().get(0);
		}
		return null;
	}

	/**
	 * Registers a new subscriber.
	 * 
	 * @param subscriber the subscriber to register
	 * @return the result of the registration request
	 */
	public String registerSubscriber(Subscriber subscriber) {
		msg = new Message("registerSubscriber", subscriber);
		handleMessageFromClientUI(msg);
		if (msg.getCommand().equals("success"))
			return (String) msg.getArguments().get(0);
		return null;
	}

	/**
	 * Retrieves data for a graph, typically for displaying statistics.
	 * 
	 * @param year the year for the graph data
	 * @param month the month for the graph data
	 * @param name the name of the graph (e.g., "borrowedBooks")
	 * @return the graph data message
	 */
	public Message getGraph(Integer year, Integer month, String name) {
		msg = new Message("getGraph", year, month, name);
		handleMessageFromClientUI(msg);
		return msg;
	}

	/**
	 * Navigates to the next page in the UI.
	 * 
	 * @param loader the FXMLLoader to load the next page
	 * @param parent the parent node for the new scene
	 * @param event the event triggering the page change
	 * @param title the title of the new page
	 */
	public void nextPage(FXMLLoader loader, Parent parent, Event event, String title) {
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(parent);
        scene.getStylesheets().add(getClass().getResource("/gui/client/stylesheet.css").toExternalForm());
        appStage.setOnCloseRequest(e -> System.exit(0));
        appStage.setTitle(title);
        appStage.setScene(scene);
        appStage.show();
	}

	/**
	 * Displays an error message in the UI.
	 * 
	 * @param lblError the label to display the error message
	 * @param message the error message to display
	 * @param color the color of the message text
	 */
	public void display(Label lblError, String message, Color color) {
		lblError.setText(message);
		lblError.setTextFill(color);
	}
}
