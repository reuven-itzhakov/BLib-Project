package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.management.InstanceNotFoundException;

import logic.Activity;
import logic.BookCopy;
import logic.BookTitle;
import logic.Borrow;
import logic.Message;
import logic.Order;
import logic.Subscriber;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * This class represents the server of the BLib application. It extends the
 * AbstractServer from the ocsf library and manages client connections and
 * messages from clients.
 */
public class BLibServer extends AbstractServer {

	// A map storing connected clients and their respective information (IP address
	// and host name)
	private static Map<ConnectionToClient, String[]> connectedClients = new HashMap<>();

	// Instance of ReportGenerator for generating reports
	private ReportGenerator reportGenerator;

	/**
	 * Constructs a new BLibServer object, initializes the server, and starts
	 * listening for client connections on the specified port. It also initializes
	 * the report generator and starts the server timer.
	 *
	 * @param port the port number on which the server will listen for incoming
	 *             client connections
	 * @throws IOException if there is an error when starting the server
	 */
	public BLibServer(int port) throws IOException {
		super(port); // Call the superclass constructor to initialize the server
		listen(); // Start listening for client connections
		reportGenerator = new ReportGenerator();
		ServerTimer.start(this);
	}

	/**
	 * Establishes a connection to the database using the provided password.
	 *
	 * @param password the password required for the database connection
	 * @return true if the connection to the database is successful, false otherwise
	 */
	public static boolean connect(String password) {
		return BLibDBC.getInstance().connect(password); // Call the connect method from the BLibDBC class
	}

	/**
	 * This method is called when a new client connects to the server. It stores the
	 * client's connection details, including the IP address and host name, in the
	 * `connectedClients` map.
	 *
	 * @param client the connection object representing the newly connected client
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		// Store the client connection details in the connectedClients map
		connectedClients.put(client,
				new String[] { client.getInetAddress().getHostAddress(), client.getInetAddress().getHostName() });
	}

	/**
	 * Handles any exceptions that occur during communication with a client. This
	 * method ensures that the client connection is closed if an exception is
	 * encountered during communication.
	 *
	 * @param client    the client whose connection caused the exception
	 * @param exception the exception that was thrown during communication
	 */
	@Override
	protected synchronized void clientException(ConnectionToClient client, Throwable exception) {
		try {
			client.close(); // Close the client connection in case of an exception
		} catch (IOException e) {
			e.printStackTrace(); // Log the exception stack trace
		}
	}

	/**
	 * Retrieves the map of currently connected clients. The map contains entries
	 * with the {@link ConnectionToClient} as the key and an array of {@link String}
	 * as the value.
	 * 
	 * @return a map containing the connected clients and their associated
	 *         information
	 */
	public Map<ConnectionToClient, String[]> getConnectedClients() {
		return connectedClients;
	}

	/**
	 * Handles messages received from clients. The function processes various
	 * requests like user login, subscriber management, book borrowing and
	 * returning, and more based on the command received in the message.
	 * 
	 * This method listens for specific commands and executes the corresponding
	 * actions, interacting with a database to fetch, update, or delete data. After
	 * performing the requested action, it sends the appropriate response back to
	 * the client.
	 * 
	 * @param msg    the message received from the client. It contains the command
	 *               and its corresponding arguments.
	 * @param client the client connection object. It represents the client that
	 *               sent the message, enabling communication with the client.
	 */
	@SuppressWarnings("unchecked") // Suppress warnings related to type casting
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("receive message:" + msg); // Log the received message
		LocalDate today = LocalDate.now(); // Get today's date
		LocalDateTime now = LocalDateTime.now(); // Get current date and time
		String err; // Variable to store error message if any
		if (msg instanceof Message) { // If the message is of type Message
			List<Object> args = ((Message) msg).getArguments(); // Retrieve arguments from the message
			try {
				Object ret; // Variable to store the result of database operations
				// Switch-case to handle different types of commands
				switch (((Message) msg).getCommand()) {

				// Handle login request
				case "login":
					ret = BLibDBC.getInstance().login((Integer) args.get(0), (String) args.get(1)); // Perform login
																									// check in database
					if (ret != null) { // If login is successful
						client.sendToClient(new Message("loginSuccess", (String) ret)); // Send success message with
																						// user role
					} else {
						client.sendToClient(new Message("loginFail")); // Send failure message if login fails
					}
					break;

				// Handle getSubscriber request
				case "getSubscriber":
					ret = BLibDBC.getInstance().getSubscriberByID((Integer) args.get(0)); // Fetch subscriber details
																							// from the
					if (ret != null) { // If subscriber found
						client.sendToClient(new Message("subscriberFound", (Subscriber) ret)); // Send subscriber
																								// details to client
					} else {
						client.sendToClient(new Message("subscriberNotFound")); // Send failure message if subscriber
																				// not found
					}
					break;

				// Handle updateSubscriber request
				case "updateSubscriber":
					ret = BLibDBC.getInstance().updateSubscriber((Subscriber) args.get(0), (String) args.get(1)); // Update
					// in the database
					// database
					// If update is successful
					if (((Boolean) ret) == true) { // If update is successful
						client.sendToClient(new Message("subscriberUpdated")); // Send success message
					} else {
						client.sendToClient(new Message("subscriberFailedUpdated")); // Send failure message if update
																						// fails
					}
					break;

				// Handle search for book titles by keyword
				case "getTitlesByKeyword":
					ret = BLibDBC.getInstance().getTitlesByKeyword((String) args.get(0));// Search for book titles by
																							// keyword
					if (ret != null) { // If search is successful
						client.sendToClient(new Message("searchResult", (Set<BookTitle>) ret)); // Send search results
					} else {
						client.sendToClient(new Message("searchFailed")); // Send failure message if no titles found
					}
					break;

				// Handle search for book copies by title
				case "getCopiesByTitle":
					ret = BLibDBC.getInstance().getCopiesByTitle((BookTitle) args.get(0)); // Search for book copies by
																							// title
					if (ret != null) { // If search is successful
						client.sendToClient(new Message("searchResult", (Set<BookCopy>) ret)); // Send search results
					} else {
						client.sendToClient(new Message("searchFailed")); // Send failure message if no copies found
					}
					break;

				// Handle registerSubscriber request
				case "registerSubscriber":
					// Generate a random password for the new subscriber
					String pass = generatePassword(4);

					// Register the new subscriber in the database and get the result
					ret = BLibDBC.getInstance().registerSubscriber((Subscriber) args.get(0), pass);

					// Send success message with the generated password if registration is
					// successful
					if ((Boolean) ret == true) {
						client.sendToClient(new Message("success", pass));

					} else {
						client.sendToClient(new Message("failed")); // Send failure message if registration fails
					}
					break;

				// Handle creating a new borrow request
				case "createBorrow":
					// Retrieve the order and subscriber data from the database
					Order o = BLibDBC.getInstance().getOrderByCopy((Integer) args.get(1));
					Subscriber sub = BLibDBC.getInstance().getSubscriberByID((Integer) args.get(0));
					BookCopy copy = BLibDBC.getInstance().getCopyByID((Integer) args.get(1));
					// Check if the subscriber can borrow the book copy
					err = canBorrow(sub, copy);
					if (err != null) {
						// Send failure message if borrowing conditions are not met
						client.sendToClient(new Message("failed", err));
						break;
					}

					if (o != null) {
						if (o.getSubscriber().getId() != (Integer) args.get(0)) {
							// If the copy is already ordered by another subscriber, send failure message
							client.sendToClient(new Message("failed", "this copy is ordered")); // Send success message
							break;
						} else {
							
							// Cancel the order if the current subscriber is the one who ordered it
							BLibDBC.getInstance().cancelCommand("cancelOrder", "%d".formatted((Integer) args.get(1)));// Canceling
																														// the
							execute(new Message("cancelOrder", "%d".formatted((Integer) args.get(1)))); // Canceling the order
						}
					}

					// Attempt to create the borrow record in the database
					ret = BLibDBC.getInstance().createBorrow((Integer) args.get(0), (Integer) args.get(1)); // Create a
																											// borrow
					if ((Boolean) ret == true) {
						client.sendToClient(new Message("success")); // Send success message
					} else {
						client.sendToClient(new Message("failed", "DB error")); // Send failure message if borrow
																				// creation fails
					}
					break;

				// Handle fetching active borrow for a book copy
				case "getCopyActiveBorrow":
					// Retrieve the active borrow record for the given book copy
					ret = BLibDBC.getInstance().getCopyActiveBorrow((BookCopy) args.get(0)); // Retrieve active borrow
																								// for a book copy
					if (ret != null) {
						// Send the active borrow details to the client if found
						client.sendToClient(new Message("borrowFound", (Borrow) ret));
					} else {
						client.sendToClient(new Message("borrowNotFound")); // Send failure message if borrow creation
																			// fails
					}
					break;

				// Handle extend borrow duration request
				case "extend":

					if (args.get(2).equals("subscriber")) {
						// Check if the borrow can be extended for a subscriber
						err = canExtend((Borrow) args.get(0));
						if (err != null) { // Check if the borrow can be extended
							client.sendToClient(new Message("failed", err));
							break;
						}
					}

					// Check if the title is ordered by another subscriber and cannot be extended
					if (BLibDBC.getInstance().getTitleAvailability(((Borrow) args.get(0)).getBook().getTitle()) < 0) {
						client.sendToClient(new Message("failed", "this title is ordered")); // If no extension allowed,
						break;
					}

					// Attempt to extend the borrow duration in the database
					ret = BLibDBC.getInstance().extendDuration((Borrow) args.get(0), (Integer) args.get(1),
							(String) args.get(2)); // Extend the borrow duration
					if ((Boolean) ret == true) {
						client.sendToClient(new Message("success")); // Send success message
					} else {
						client.sendToClient(new Message("failed", "DB error")); // Send failure message
					}

					break;

				// Handle return book request
				case "return":
					// Retrieve the active borrow for the book copy being returned
					Borrow borrow = BLibDBC.getInstance().getCopyActiveBorrow((BookCopy) args.get(0)); // Retrieve //
																										// active borrow
					// Check if the title is ordered and if any actions need to be taken
					if (BLibDBC.getInstance().isTitleOrdered(((BookCopy) args.get(0)).getTitle().getTitleID())) {

						if (BLibDBC.getInstance().updateOrder((BookCopy) args.get(0))) {

							Order order = BLibDBC.getInstance().getOrderByCopy(((BookCopy) args.get(0)).getCopyID());

							// Send email notification to the subscriber when their ordered book arrives
							MessageController.getInstance().sendEmail(order.getSubscriber(), "Your order has arrived!",
									"Dear %s,\n\n".formatted(order.getSubscriber().getName())
											+ "Your book order of \"%s\" has arrived and is ready for pickup.\n"
													.formatted(((BookCopy) args.get(0)).getTitle())
											+ "Please collect it within the next two days, or the order will be canceled.\n\n"
											+ "Best regards, BLib library");

							// Schedule cancellation of the order if not picked up in two days
							BLibDBC.getInstance().createCommand("cancelOrder",
									"%d".formatted(((BookCopy) args.get(0)).getCopyID()), now.plusDays(2),
									"%d".formatted(((BookCopy) args.get(0)).getCopyID()));
						}
					}

					// If no borrow record is found, send a failure message
					if (borrow == null) {
						client.sendToClient(new Message("failed", "No such borrowed book")); // Send failure message if
						break;
					}
					// If the due date is passed, process as late return and potentially freeze the
					// subscriber
					if (borrow.getDueDate().compareTo(today) < 0) {
						BLibDBC.getInstance().returnBook((BookCopy) args.get(0), true);

						// Freeze the subscriber if the book is significantly late
						if (borrow.getDueDate().plusWeeks(1).compareTo(today) <= 0) {
							if (!borrow.getSubscriber().getStatus().equalsIgnoreCase("frozen")) {
								BLibDBC.getInstance().freezeSubscriber(borrow.getSubscriber().getId());
								client.sendToClient(new Message("success", "Freezing account")); // Send freeze success
																									// message
							} else {
								// Update freeze status if the account is already frozen
								BLibDBC.getInstance().cancelCommand("unfreeze",
										"%s".formatted(borrow.getSubscriber().getId()));
								BLibDBC.getInstance().createCommand("unfreeze",
										"%s".formatted(borrow.getSubscriber().getId()), now.plusMonths(1),
										"%s".formatted(borrow.getSubscriber().getId()));
								client.sendToClient(new Message("success", "Freeze updated"));
							}
						} else {
							client.sendToClient(new Message("success", "The return was late")); // Send late return
																								// success message
						}
					} else {
						BLibDBC.getInstance().cancelCommand("sendMessage",
								"%s;%s".formatted(borrow.getSubscriber().getId(), borrow.getBook().getCopyID()));
						BLibDBC.getInstance().returnBook((BookCopy) args.get(0), false);
						client.sendToClient(new Message("success", "The return was successful")); // Send success
																									// message for
																									// regular return
					}
					break;

				// Handle retrieving subscriber history
				case "history":
					// Retrieve subscriber history from the database
					ret = BLibDBC.getInstance().getSubscriberHistory((Integer) args.get(0));
					if (ret != null) {
						// Send the history data to the client if found
						client.sendToClient(new Message("historyRetrieved", (List<Activity>) ret));
					} else {
						client.sendToClient(new Message("Failed")); // Send failure message
					}
					break;

				// Handle order book request
				case "order":
					// Check if the subscriber can order the specified book
					err = canOrder((Subscriber) args.get(0), (BookTitle) args.get(1));
					if (err != null) {
						client.sendToClient(new Message("Failed", err));
						break;
					}

					// Attempt to create the book order in the database
					ret = BLibDBC.getInstance().orderBook(((Subscriber) args.get(0)).getId(),
							((BookTitle) args.get(1)).getTitleID());
					if (ret != null) {
						// Send success message if the order is created
						client.sendToClient(new Message("success"));
					} else {
						// Send failure message if there is a database error
						client.sendToClient(new Message("Failed", "DB error")); // Send failure message
					}
					break;

				// Handle retrieving subscriber's active borrows
				case "getSubscriberBorrows":
					// Retrieve the active borrows for a subscriber
					ret = BLibDBC.getInstance().getSubscriberActiveBorrows((Subscriber) args.get(0));
					if (ret != null) {
						// Send active borrows to the client
						client.sendToClient(new Message("success", (List<Borrow>) ret));
					} else {
						// Send failure message if no active borrows are found
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle retrieving all subscribers
				case "getAllSubscribers":
					// Retrieve all subscribers from the database
					ret = BLibDBC.getInstance().getAllSubscribers();
					if (ret != null) {
						// Send the list of subscribers to the client
						client.sendToClient(new Message("success", (List<Subscriber>) ret));
					} else {
						// Send failure message if no subscribers are found
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle retrieving subscriber's active orders
				case "getSubscriberOrders":
					// Retrieve the active orders for a subscriber
					ret = BLibDBC.getInstance().getSubscriberActiveOrders((Subscriber) args.get(0));
					if (ret != null) {
						// Send the active orders to the client
						client.sendToClient(new Message("success", (List<Order>) ret));
					} else {
						// Send failure message if no active orders are found
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle retrieving librarian messages
				case "getLibrarianMessages":
					// Retrieve messages for the librarian
					ret = BLibDBC.getInstance().getLibrarianMessages();
					if (ret != null) {
						// Send librarian messages to the client
						client.sendToClient(new Message("success", (List<String>) ret));
					} else {
						// Send failure message if no messages are found
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle clearing librarian messages
				case "clearLibrarianMessages":
					// Attempt to clear librarian messages in the database
					ret = BLibDBC.getInstance().clearLibrarianMessages();
					if ((Boolean) ret == true) {
						// Send success message if messages are cleared successfully
						client.sendToClient(new Message("success"));
					} else {
						// Send failure message if there is an error
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle retrieving book copy by ID
				case "getCopyByID":
					// Retrieve the book copy by its ID from the database
					ret = BLibDBC.getInstance().getCopyByID((Integer) args.get(0));

					if (ret != null) {
						// Send the book copy details to the client if found
						client.sendToClient(new Message("success", (BookCopy) ret));
					} else {
						// Send failure message if the book copy is not found
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle the "getTitleClosestReturnDate" command
				case "getTitleClosestReturnDate":
					// Retrieve the closest return date for the book title from the database
					ret = BLibDBC.getInstance().getTitleClosestReturnDate((BookTitle) args.get(0));

					if (ret != null) {
						// Send the closest return date to the client if found
						client.sendToClient(new Message("success", (LocalDate) ret));
					} else {
						// Send failure message if no closest return date is found
						client.sendToClient(new Message("failed"));
					}
					break;

				// Handle the "getGraph" command
				case "getGraph":
					// Retrieve the graph data from the database based on the provided parameters
					ret = BLibDBC.getInstance().getGraph((Integer) args.get(0), (Integer) args.get(1),
							(String) args.get(2));

					if (ret != null) {
						// Send the graph data (as bytes) to the client if found
						client.sendToClient(new Message("success", ((DataInputStream) ret).readAllBytes()));
					} else {
						// Send failure message if the graph data is not found
						client.sendToClient(new Message("failed"));
					}
					break;

				default:
					client.sendToClient(new Message("unknownCommand: " + ((Message) msg).getCommand()));

				}
			} catch (IOException e) {
				e.printStackTrace(); // Log any exceptions that occur during message handling
			}

		}

	}

	public void execute(Message msg) {
		List<Object> args = ((Message) msg).getArguments();
		Subscriber sub;
		switch (msg.getCommand()) {
		case "unfreeze":
			BLibDBC.getInstance().unfreezeSubscriber(Integer.parseInt((String) args.get(0)));
			break;

		case "sendEmail":
			sub = BLibDBC.getInstance().getSubscriberByID(Integer.parseInt((String) args.get(0)));
			MessageController.getInstance().sendEmail(sub, (String) args.get(1), (String) args.get(2));
			break;
		case "sendMessage":
			sub = BLibDBC.getInstance().getSubscriberByID(Integer.parseInt((String) args.get(0)));
			MessageController.getInstance().sendMessage(sub, (String) args.get(1), (String) args.get(2));
			break;
		case "cancelOrder":
			BLibDBC.getInstance().cancelOrder(Integer.parseInt((String) args.get(0)));
			break;

		case "generateGraphs":
			LocalDate date = LocalDate.of(Integer.parseInt((String) args.get(0)),
					Integer.parseInt((String) args.get(1)), 1);
			System.out.println(date);
			byte[] data = reportGenerator.generateSubscriberStatusReport(date);
			BLibDBC.getInstance().saveGraph(date, "subscriber status", data);
			data = reportGenerator.generateBorrowTimeReport(date);
			BLibDBC.getInstance().saveGraph(date, "borrowing report", data);
			LocalDate today = LocalDate.now();
			LocalDate nextMonth = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
			LocalDate timeOfNextExecution = nextMonth.plusMonths(1).minusDays(1);
			BLibDBC.getInstance().createCommand("generateGraphs",
					"%04d;%02d".formatted(nextMonth.getYear(), nextMonth.getMonthValue()),
					LocalDateTime.of(timeOfNextExecution, LocalTime.of(23, 30)), "");

		}
	}

	/**
	 * Checks if the borrow period can be extended.
	 * 
	 * The borrow cannot be extended if: - The subscriber's status is "frozen". -
	 * The borrow was made within the last week. - The borrow is overdue.
	 * 
	 * @param borrow the borrow object to check.
	 * @return a message explaining why the borrow can't be extended, or
	 *         {@code null} if it can.
	 */
	private String canExtend(Borrow borrow) {
		// Check if the subscriber's status is "frozen". If yes, they can't extend the
		// borrow period.
		if (borrow.getSubscriber().getStatus().equals("frozen")) {
			return "the subscriber is frozen";
		}

		// Check if the borrow period is less than or equal to one week. If the borrow
		// was made within the last week,
		// it cannot be extended.

		if (borrow.getDueDate().minusWeeks(1).compareTo(LocalDate.now()) >= 0) {
			return "extention not available until %s".formatted(borrow.getDueDate().minusWeeks(1));
		}
		if (borrow.getDueDate().compareTo(LocalDate.now()) < 0) {
			return "extention not available after due date";
		}
		// If none of the conditions above are met, the borrow can be extended.
		return null;
	}

	/**
	 * Generates a random password consisting of digits.
	 * 
	 * @param length the length of the generated password.
	 * @return the generated password as a string of digits.
	 */
	private String generatePassword(int length) {
		// Create a StringBuilder to hold the password characters.
		StringBuilder str = new StringBuilder();
		Random rand = new Random();
		// Loop to generate each character of the password.
		for (int i = 0; i < length; i++) {
			str.append(rand.nextInt(10));
		}
		return str.toString();
	}

	/**
	 * Retrieves a list of commands.
	 * 
	 * @return a List of Message objects representing the commands.
	 */
	public List<Message> getCommands() {
		// Accessing the BLibDBC singleton instance and calling the getCommands method
		// to retrieve the list of commands.
		return BLibDBC.getInstance().getCommands();
	}

	/**
	 * Checks if a subscriber can order a specific book.
	 * 
	 * @param sub   the subscriber attempting to order the book
	 * @param title the book title the subscriber wants to order
	 * @return a String message indicating the result of the check, or null if the
	 *         order is allowed
	 */
	private String canOrder(Subscriber sub, BookTitle title) {
		// Check if the subscriber is null
		if (sub == null) {
			return "The subscriber is not found";
		}

		// Check if the book title is null
		if (title == null) {
			return "The book is not found";
		}

		// Check if the subscriber's status is frozen
		if (sub.getStatus().equals("frozen"))
			return "The subscriber is frozen";

		// Check if the subscriber has already borrowed the book
		for (Borrow b : BLibDBC.getInstance().getSubscriberActiveBorrows(sub)) {
			if (b.getBook().getTitle().equals(title))
				return "This Book is already Borrowed";
		}

		// Check if the subscriber has already ordered the book
		for (Order o : BLibDBC.getInstance().getSubscriberActiveOrders(sub)) {
			if (o.getTitle().equals(title))
				return "This Book is already ordered";
		}

		// Check if any copies of the book are currently available for borrowing
		if (BLibDBC.getInstance().getTitleAvailability(title) > 0) {
			return "Not all of the title copies are borrowed";
		}

		// Check if there are too many active orders for the book
		if (BLibDBC.getInstance().getTitleAvailability(title) + BLibDBC.getInstance().getNumOfCopies(title) <= 0) {
			return "There are too many active orders";
		}

		// If none of the conditions apply, the order can be placed
		return null;
	}

	
	/**
	 * Checks if a subscriber can borrow a specific book copy.
	 * 
	 * @param sub the subscriber attempting to borrow the book copy
	 * @param copy the book copy the subscriber wants to borrow
	 * @return a String message indicating the result of the check, or null if the borrow is allowed
	 */
	private String canBorrow(Subscriber sub, BookCopy copy) {
		// Check if the book copy is null
		if (copy == null) {
			return "The book is not found";
		}
		
		// Check if the subscriber is null
		if (sub == null) {
			return "The subscriber is not found";
		}
		
		// Check if the subscriber's status is frozen
		if (sub.getStatus().equals("frozen"))
			return "The subscriber is frozen";

		// Check if the subscriber has already borrowed this book
		for (Borrow b : BLibDBC.getInstance().getSubscriberActiveBorrows(sub)) {
			if (b.getBook().getTitle().equals(copy.getTitle()))
				return "This Book is already Borrowed by the subscriber";
		}
		
		// Check if the book copy is already borrowed by someone else
		if (BLibDBC.getInstance().getCopyActiveBorrow(copy) != null) {
			return "This copy is already Borrowed, return it first";
		}
		
		for(Order o : BLibDBC.getInstance().getSubscriberActiveOrders(sub)) {
			if(o.getTitle().equals(copy.getTitle())) {
				if(o.getCopy()!=null) {
					if (o.getCopy().getCopyID() != copy.getCopyID()) {
						return "The subscriber has an active order on a different copy";
					}
				}
			}
		}
		// If none of the conditions apply, the borrow is allowed
		return null;
	}

	
	/**
	 * Retrieves the status of all subscribers for a given month.
	 * 
	 * @param date the reference date within the month for which subscriber statuses are to be retrieved
	 * @return a map where the key is a LocalDate representing a day in the month,
	 *         and the value is an array of integers representing the status of subscribers on that day
	 */
	public Map<LocalDate, Integer[]> getSubscribersStatusOnMonth(LocalDate date) {
	    // Fetch the subscribers' status for the specified month from the database
	    return BLibDBC.getInstance().getSubscribersStatusOnMonth(date);
	}

	
	/**
	 * Calculates the total number of new subscribers since the specified date.
	 * 
	 * @param now the date used to check the new subscribers
	 * @return the total number of new subscribers; returns 0 if no new subscribers are found
	 */
	public int SumNewSubscriber(LocalDate t) {
		 // Retrieve the sum of new subscribers from the database
		Integer ret = BLibDBC.getInstance().SumNewSubscriber(t);
		// If there is a result, return it; otherwise, return 0
		if (ret != null) {
			return ret;
		}
		return 0;
	}

	/**
	 * Retrieves the borrow times for each book on a specific month.
	 * 
	 * @param date the date used to determine the month for which borrow times are to be retrieved
	 * @return a map where the keys are book titles (as Strings) and the values are arrays of Doubles representing 
	 *         the borrow times for each book in the specified month
	 */
	public Map<String, Double[]> getBorrowTimeOnMonth(LocalDate date) {
		return BLibDBC.getInstance().getBorrowTimeOnMonth(date);
	}

	
	/**
	 * Retrieves the average borrow time for all books in a specific month.
	 * 
	 * @param date the date used to determine the month for which the average borrow time is to be calculated
	 * @return the average borrow time in days for all books in the specified month, or null if no data is available
	 */
	public Double getAvgBorrowTimeOnMonth(LocalDate date) {
		return BLibDBC.getInstance().getAvgBorrowTimeOnMonth(date);
	}

}
