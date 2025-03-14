package server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import logic.Activity;
import logic.BookCopy;
import logic.BookTitle;
import logic.Borrow;
import logic.Message;
import logic.Order;
import logic.Subscriber;

/**
 * This class BLibDBC represents a timed singleton database connection manager
 * for the BLibDB database. It handles connecting, disconnecting, and
 * transaction management for the database.
 */
public class BLibDBC {
	private static String pass = null; // Password used for connecting to the database
	private static Calendar ILTimeZone; // Timezone for the database connection
	private static volatile BLibDBC instance;
	private static Connection conn; // Connection object to interact with the database
	private static PreparedStatement pstmt; // Statement object for executing SQL queries
	private static boolean timerRunning = false;
	private static Thread timerThread;
	
	/**
	 * Private constructor to prevent instantiation. Ensures that only one instance
	 * of BLibDBC is created.
	 */
	private BLibDBC() {
	}

	/**
	 * Disconnects from the database.
	 * 
	 * @return true if the disconnection was successful, false otherwise.
	 */
	public boolean disconnect() {
		try {
			if (conn == null) {
				System.out.println("SQL disconnection failed (connection is null)");
				return false; // Return false if the connection is null
			}
			if (conn.isClosed()) {
				System.out.println("SQL disconnection failed (connection is closed)");
				return false; // Return false if the connection is already closed
			}
			// Try closing the connection
			conn.close();
			return true; // Return true if disconnection is successful
		} catch (SQLException e) {
			// If disconnection fails, print error message and return false
			System.out.println("SQL disconnection failed (exception thrown)");
			return false;
		}
	}

	/**
	 * Rolls back the current transaction. If the rollback fails, attempts to
	 * disconnect and reconnect to the database.
	 */
	private void rollback() {
		try {
			conn.rollback(); // Rollback the current transaction
		} catch (SQLException e) {
			// If rollback fails, attempt to disconnect and reconnect to the database
			disconnect();
			connect(pass);
		}
	}

	/**
	 * Resets the inactivity timer. If a timer is already running, it interrupts the
	 * current thread and starts a new one. The timer disconnects the database
	 * connection after 5 minutes of inactivity.
	 *
	 * This method is synchronized to ensure thread safety.
	 */
	private synchronized static void resetTimer() {

		if (timerRunning && timerThread != null) {
			timerThread.interrupt(); // Interrupt the existing thread
		}
		timerThread = new Thread(() -> {
			try {
				timerRunning = true;
				Thread.sleep((long) (5 * 60 * 1000)); // Sleep for 5 minutes
				synchronized (BLibDBC.class) {
					if (instance != null) {
						instance.disconnect();
						instance = null;
						System.out.println("DB connection has been closed due to inactivity.");
					}
					timerRunning = false;
				}
			} catch (InterruptedException e) {
				// Timer reset or instance reused
			}
		});
		timerThread.setDaemon(true); // Make the thread a daemon thread
		timerThread.start();

	}

	/**
	 * Connects to the BLibDB database with the given password.
	 * 
	 * @param password the password for the database connection.
	 * @return true if the connection was successful, false otherwise.
	 */
	public boolean connect(String password) {
		pass = password; // Store the password for later use in reconnection
		try {
			// Try loading the MySQL JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			// If the driver fails to load, print error message and return false
			System.out.println("Driver definition failed");
			return false;
		}
		try {
			ILTimeZone = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
			// Try connecting to the database
			conn = DriverManager.getConnection(
					"jdbc:mysql://localhost/BLibDB?useSSL=FALSE&serverTimezone=Asia/Jerusalem", "root", password);
			conn.setAutoCommit(false); // Disable auto-commit to handle transactions manually
			System.out.println("SQL connection succeed");
			return true; // Return true if connection is successful
		} catch (Exception e) {
			// If the connection fails, print error message and return false
			System.out.println("SQL connection failed");
			return false;
		}
	}

	/**
	 * Returns the singleton instance of {@code BLibDBC}. If the instance is not
	 * initialized, it creates and connects it in a thread-safe manner. Resets the
	 * inactivity timer on each call to keep the connection active.
	 *
	 * @return the singleton instance of {@code BLibDBC}
	 */
	public static BLibDBC getInstance() {
		if (instance == null)
			synchronized (BLibDBC.class) {
				if (instance == null) {
					instance = new BLibDBC();
					if (pass != null)
						instance.connect(pass);
				}
			}
		resetTimer();
		return instance;
	}

	/**
	 * Retrieves a BookTitle object from the database based on the given title ID.
	 * 
	 * @param titleID The ID of the title to retrieve.
	 * @return The BookTitle object corresponding to the given title ID, or null if
	 *         no such title is found or if an error occurs.
	 */
	public BookTitle getTitleByID(int titleID) {
		try {
			pstmt = conn.prepareStatement("SELECT * FROM titles WHERE title_id = ?");
			pstmt.setInt(1, titleID); // Set the title ID parameter
			ResultSet rs = pstmt.executeQuery();
			// If a result is found, return a BookTitle object created from the result
			if (rs.next()) {
				return new BookTitle(titleID, rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5),
						rs.getInt(6), rs.getString(7));
			}
			return null; // Return null if no result is found
		} catch (SQLException e) {
			return null;// Return null if an error occurs
		}

	}

	/**
	 * Retrieves a set of BookCopy objects associated with a specific BookTitle.
	 * 
	 * @param title The BookTitle for which to retrieve the associated copies.
	 * @return A Set of BookCopy objects corresponding to the given BookTitle, or
	 *         null if an error occurs.
	 */
	public Set<BookCopy> getCopiesByTitle(BookTitle title) {
		try {
			// Execute SQL query
			pstmt = conn.prepareStatement("SELECT * FROM copies WHERE title_id = ?;");
			pstmt.setInt(1, title.getTitleID()); // Set the title ID parameter
			ResultSet rs = pstmt.executeQuery();
			Set<BookCopy> bookSet = new HashSet<>();
			// Loop through result set and create BookCopy objects
			while (rs.next()) {
				BookCopy copy = new BookCopy(title, rs.getInt(2), rs.getString(3), rs.getBoolean(4));
				bookSet.add(copy);
			}
			return bookSet; // Return the set of book copies
		} catch (SQLException e) {
			return null; // Return null if an error occurs
		}
	}

	/**
	 * Retrieves a set of BookTitle objects that match a specific keyword in their
	 * name, author, description, or genre.
	 * 
	 * @param keyword The keyword to search for in the title's name, author's name,
	 *                description, or genre.
	 * @return A Set of BookTitle objects that match the given keyword, or null if
	 *         an error occurs.
	 */
	public Set<BookTitle> getTitlesByKeyword(String keyword) {
		try {
			String keywordWildcard = "%" + keyword + "%"; // Use wildcard for partial matching
			// Execute SQL query
			pstmt = conn.prepareStatement(
					"SELECT * FROM titles WHERE title_name LIKE ? OR author_name LIKE ? OR title_description LIKE ? OR genre LIKE ?;");
			pstmt.setString(1, keywordWildcard);
			pstmt.setString(2, keywordWildcard);
			pstmt.setString(3, keywordWildcard);
			pstmt.setString(4, keywordWildcard);
			ResultSet rs = pstmt.executeQuery();
			Set<BookTitle> bookSet = new HashSet<>();
			// Loop through result set and create BookTitle objects
			while (rs.next()) {
				BookTitle title = new BookTitle(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getInt(5), rs.getInt(6), rs.getString(7));
				bookSet.add(title);
			}
			return bookSet; // Return the set of book titles
		} catch (SQLException e) {
			return null; // Return null if an error occurs
		}
	}

	/**
	 * Retrieves a Subscriber object from the database based on the given subscriber
	 * ID.
	 * 
	 * @param subscriberID The ID of the subscriber to retrieve.
	 * @return The Subscriber object corresponding to the given subscriber ID, or
	 *         null if no such subscriber is found or if an error occurs.
	 */
	public Subscriber getSubscriberByID(int subscriberID) {
		try {
			// Execute SQL query to fetch the subscriber by their ID
			pstmt = conn.prepareStatement("SELECT * FROM subscribers WHERE subscriber_id = ?");
			pstmt.setInt(1, subscriberID); // Set the subscriber ID parameter
			ResultSet rs = pstmt.executeQuery();
			// If a result is found, create and return a Subscriber object
			if (rs.next()) {
				return new Subscriber(subscriberID, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
			}
			return null; // Return null if no result is found
		} catch (SQLException e) {
			return null; // Return null if an error occurs
		}
	}

	/**
	 * Retrieves an Order object from the database based on the given copy ID.
	 * 
	 * @param copyID The ID of the book copy to retrieve the associated order for.
	 * @return The Order object corresponding to the given copy ID, or null if no
	 *         such order is found or if an error occurs.
	 */
	public Order getOrderByCopy(int copyID) {
		try {

			// Fetch the book copy details using the provided copy ID
			BookCopy copy = getCopyByID(copyID);
			if (copy == null)
				return null; // Return null if no book copy is found for the order

			// Prepare a SQL statement to fetch the order by the given copy ID
			pstmt = conn.prepareStatement("SELECT * FROM orders WHERE copy_id = ?");
			pstmt.setInt(1, copyID);
			ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
			// If a result is found in the database, process the order details
			if (rs.next()) {

				// Fetch the subscriber associated with the order using the subscriber ID from
				// the result
				Subscriber sub = getSubscriberByID(rs.getInt(2));
				if (sub == null)
					return null; // Return null if no subscriber is found for the order

				// Create a new Order object based on the retrieved details from the database
				Order order = new Order(rs.getInt(1), sub, copy.getTitle(),
						rs.getTimestamp(5, ILTimeZone).toLocalDateTime());

				order.setCopy(copy); // Set the book copy on the order
				order.setAriveDate(rs.getDate(6, ILTimeZone).toLocalDate()); // Set the arrival date of the order

				return order; // Return the fully populated order object
			}
			// Return null if no matching order is found in the database
			return null;

		} catch (SQLException e) {
			// Return null if an error occurs during database interaction
			return null;
		}
	}

	/**
	 * Creates a borrow record in the database, updates the book copy's status, logs
	 * the borrow activity, and schedules a reminder message to be sent to the
	 * subscriber.
	 * 
	 * @param subscriberID The ID of the subscriber borrowing the book.
	 * @param copyID       The ID of the book copy being borrowed.
	 * @return True if the borrow process is successful, false if any error occurs
	 *         or if the subscriber or copy doesn't exist.
	 */
	public Boolean createBorrow(int subscriberID, int copyID) {
		try {
			// Fetch subscriber and book copy objects to ensure they exist
			Subscriber sub = getSubscriberByID(subscriberID);
			if (sub == null) {
				return false; // Return false if subscriber not found
			}
			BookCopy copy = getCopyByID(copyID);
			if (copy == null) {
				return false; // Return false if book copy not found
			}

			// Calculate today's date and the due date (2 weeks from today)
			LocalDate today = LocalDate.now();
			LocalDate dueDate = today.plusWeeks(2);

			// Insert new borrow record into the database
			pstmt = conn.prepareStatement(
					"INSERT INTO borrows(subscriber_id,copy_id,date_of_borrow,due_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, subscriberID);
			pstmt.setInt(2, copyID);
			pstmt.setDate(3, Date.valueOf(today), ILTimeZone);
			pstmt.setDate(4, Date.valueOf(dueDate), ILTimeZone);
			pstmt.execute();

			// Update the book copy's status to borrowed
			pstmt = conn.prepareStatement("UPDATE copies SET is_borrowed = ? WHERE copy_id = ?");
			pstmt.setBoolean(1, true);
			pstmt.setInt(2, copyID);
			pstmt.execute();

			// Log the borrow activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, subscriberID);
			pstmt.setString(2, "borrow");
			pstmt.setString(3,
					"\"%s\" borrowed by %s on %s".formatted(copy.getTitle(), sub.getName(), today.toString()));
			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();

			// time the execution of send message
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime reminderTime = now.plusWeeks(2).minusDays(1);

			// Create a command to send a reminder message to the subscriber about the due
			// book
			createCommand("sendMessage", "%s;%s;".formatted(sub.getId(), "Reminder: Book Due Tomorrow")
					+ "dear %s,\n\nJust a reminder that the book you borrowed (\"%s\") is due tomorrow %s. Please make sure to return it by the due date.\nBraude Library"
							.formatted(sub.getName(), copy.getTitle(), dueDate),
					reminderTime, "%s;%s".formatted(sub.getId(), copy.getCopyID()), false);

			// Commit the transaction
			conn.commit();
			return true; // Return true if all operations succeed

		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Retrieves a BookCopy object from the database by its copy ID.
	 * 
	 * @param copyID The ID of the book copy to be retrieved.
	 * @return A BookCopy object if found, null if no copy is found or if an error
	 *         occurs.
	 */
	public BookCopy getCopyByID(int copyID) {
		try {
			// Prepare the SQL query to retrieve a book copy by its ID
			pstmt = conn.prepareStatement("SELECT * FROM copies WHERE copy_id = ?");
			pstmt.setInt(1, copyID); // Set the copy ID parameter
			ResultSet rs = pstmt.executeQuery();
			// If a result is found, create a BookCopy object
			if (rs.next()) {
				BookTitle title = getTitleByID(rs.getInt(1)); // Get the title associated with this copy
				if (title == null) {
					return null; // Return null if the title is not found
				}
				return new BookCopy(title, copyID, rs.getString(3), rs.getBoolean(4));
			}
			return null; // Return null if no copy is found
		} catch (SQLException e) {
			return null; // Return null if an error occurs
		}
	}

	/**
	 * Updates the subscriber's details in the database and logs the activity.
	 * 
	 * @param newSubscriber The new subscriber object with updated details.
	 * @param userType      The type of user making the update (e.g., "subscriber"
	 *                      or the librarian's name).
	 * @return true if the update and logging were successful, false if an error
	 *         occurred.
	 */
	public Boolean updateSubscriber(Subscriber newSubscriber, String userType) {
		try {
			// Get today's date for logging purposes
			LocalDate today = LocalDate.now();

			// Fetch the old subscriber details from the database based on the subscriber ID
			Subscriber oldSubscriber = getSubscriberByID(newSubscriber.getId());

			// Build a string for the update description, depending on the user type
			StringBuilder str = new StringBuilder();
			if (userType.equalsIgnoreCase("subscriber")) {
				str.append(newSubscriber.getName() + " updated their details: ");
			} else {
				str.append(userType + " updated " + newSubscriber.getName() + "'s details: ");
			}

			// Prepare the SQL query to update subscriber details
			pstmt = conn.prepareStatement(
					"UPDATE subscribers SET subscriber_email = ?, subscriber_phone_number = ?  WHERE subscriber_id = ?");
			pstmt.setString(1, newSubscriber.getEmail());
			pstmt.setString(2, newSubscriber.getPhone());
			pstmt.setInt(3, newSubscriber.getId());
			pstmt.execute(); // Execute the update

			// Check if any changes have been made to the email or phone number, and append
			// to the log string
			if (!newSubscriber.getEmail().equals(oldSubscriber.getEmail())) {
				str.append(
						"changed email from %s to %s ".formatted(oldSubscriber.getEmail(), newSubscriber.getEmail()));
			}

			if (!newSubscriber.getPhone().equals(oldSubscriber.getPhone())) {
				str.append(
						"changed phone from %s to %s ".formatted(oldSubscriber.getPhone(), newSubscriber.getPhone()));
			}

			// Append the current date to the log string
			str.append("on %s".formatted(today));

			// Log the update activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, newSubscriber.getId());
			pstmt.setString(2, "update subscriber");
			pstmt.setString(3, str.toString());

			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute(); // Execute the log insert

			// Commit the transaction
			conn.commit();
			return true; // Return true if all operations succeed

		} catch (SQLException e) {
			e.printStackTrace();
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Registers a new subscriber in the system by inserting their details into the
	 * database and creating an associated user account.
	 * 
	 * @param subscriber The subscriber object containing the subscriber's details.
	 * @param password   The password for the new user account.
	 * @return true if the registration is successful, false if an error occurs.
	 */
	public Boolean registerSubscriber(Subscriber subscriber, String password) {
		try {
			// Get today's date for logging and history purposes
			LocalDate today = LocalDate.now();

			// Insert the new subscriber details into the 'subscribers' table
			pstmt = conn.prepareStatement("INSERT INTO subscribers VALUE(?,?,?,?,?)");
			pstmt.setInt(1, subscriber.getId());
			pstmt.setString(2, subscriber.getName());
			pstmt.setString(3, subscriber.getPhone());
			pstmt.setString(4, subscriber.getEmail());
			pstmt.setString(5, subscriber.getStatus());
			pstmt.execute(); // Execute the insert

			int numActive = 0; // Number of active subscribers
			int numFrozen = 0; // Number of frozen subscribers

			// Retrieve the number of active and frozen subscribers to log the registration
			// statistics
			pstmt = conn.prepareStatement(
					"SELECT subscriber_status, COUNT(*) FROM subscribers GROUP BY subscriber_status;");
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				if (rs.getString(1).equals("active"))
					numActive = rs.getInt(2);
				if (rs.getString(1).equals("frozen"))
					numFrozen = rs.getInt(2);
			}

			// Log the registration activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, subscriber.getId());
			pstmt.setString(2, "new subscriber");
			pstmt.setString(3, "%s is now a subscriber since %s;%d;%d".formatted(subscriber.getName(), today, numActive,
					numFrozen));
			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();// Execute the insert

			// Create a new user account for the subscriber with the provided password
			pstmt = conn.prepareStatement("INSERT INTO users VALUE (?,?,?)");
			pstmt.setInt(1, subscriber.getId());
			pstmt.setString(2, password);
			pstmt.setString(3, "subscriber");
			pstmt.execute();

			// Commit the transaction
			conn.commit();
			return true; // Return true if all operations were successful
		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Authenticates a user by checking their user ID and password against the
	 * database.
	 * 
	 * @param userID   The user ID of the person attempting to log in.
	 * @param password The password entered by the user.
	 * @return if the login is successful return subscriber or librarian's name, or
	 *         null if authentication fails or an error occurs.
	 */
	public String login(int userID, String password) {
		try {
			// Execute SQL query to check if the user ID and password match
			pstmt = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?");
			pstmt.setInt(1, userID);
			ResultSet rs = pstmt.executeQuery();

			// If a result is found and the credentials match, return 'subscriber' or
			// librarian name
			if (rs.next()) {
				if (userID == rs.getInt(1) && password.equals(rs.getString(2)))
					return rs.getString(3);
			}

			// If login fails, return null
			return null;
		} catch (SQLException e) {
			// If an error occurs, return null
			return null;
		}
	}

	/**
	 * Retrieves the active borrow record for a specific book copy.
	 * 
	 * @param copy The BookCopy object representing the book copy.
	 * @return A Borrow object representing the active borrow record, or null if no
	 *         active borrow is found or an error occurs.
	 */
	public Borrow getCopyActiveBorrow(BookCopy copy) {
		try {
			// Prepare the SQL query to find an active borrow record for the provided book
			// copy
			pstmt = conn.prepareStatement("SELECT * FROM borrows WHERE copy_id = ? AND date_of_return IS NULL");
			pstmt.setInt(1, copy.getCopyID());
			ResultSet rs = pstmt.executeQuery(); // Execute the query to retrieve the result

			// Check if a result is found, indicating an active borrow record
			if (rs.next()) {
				// Retrieve the subscriber who borrowed the copy
				Subscriber sub = getSubscriberByID(rs.getInt(2));
				if (sub == null) {
					return null; // Return null if the subscriber cannot be found
				}
				// Return a new Borrow object representing the active borrow, with the borrow
				// details
				return new Borrow(sub, copy, rs.getDate(4, ILTimeZone).toLocalDate(),
						rs.getDate(5, ILTimeZone).toLocalDate(), null);
			}

			// Return null if no active borrow record is found
			return null;
		} catch (SQLException e) {
			// If an error occurs, return null
			return null;
		}

	}

	/**
	 * Extends the due date of a borrow by a given number of days and logs the
	 * activity.
	 * 
	 * @param borrow   The Borrow object representing the borrow record to be
	 *                 extended.
	 * @param days     The number of days by which the due date should be extended.
	 * @param userType The type of the user requesting the extension ("subscriber"
	 *                 or the librarian's name).
	 * @return True if the extension is successful, false if an error occurs.
	 */
	public Boolean extendDuration(Borrow borrow, int days, String userType) {
		try {

			// Calculate the new due date by adding the specified number of days
			LocalDate newDueDate = borrow.getDueDate().plusDays(days);

			// Get today's date to log the activity
			LocalDate today = LocalDate.now();

			// Update the borrow record with the new due date
			pstmt = conn.prepareStatement("UPDATE borrows SET due_date = ? WHERE "
					+ "subscriber_id = ? AND copy_id = ? AND date_of_borrow = ?");
			pstmt.setDate(1, Date.valueOf(newDueDate), ILTimeZone);
			pstmt.setInt(2, borrow.getSubscriber().getId());
			pstmt.setInt(3, borrow.getBook().getCopyID());
			pstmt.setDate(4, Date.valueOf(borrow.getDateOfBorrow()), ILTimeZone);
			pstmt.execute();

			// Log the extension activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, borrow.getSubscriber().getId());

			// If the user is librarian, the action of extending the borrow duration is
			// logged as a "manual extension".
			if (userType.equals("subscriber")) {
				pstmt.setString(2, "extension");
				pstmt.setString(3, "\"%s\" extended borrow by %s on %s, the new due date is %s"
						.formatted(borrow.getBook().getTitle(), borrow.getSubscriber().getName(), today, newDueDate));

			} else {
				pstmt.setString(2, "manual extension");
				pstmt.setString(3,
						"\"%s\" manually extended borrow of %s by %s on %s, the new due date is %s".formatted(
								borrow.getBook().getTitle(), borrow.getSubscriber().getName(), userType, today,
								newDueDate));
			}
			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();

			// If the subscriber made the request, send a message to the librarian
			if (userType.equals("subscriber")) {
				LocalDateTime now = LocalDateTime.now();
				pstmt = conn.prepareStatement("INSERT INTO librarian_messages(message, time) VALUE(?,?);");
				pstmt.setString(1,
						"the subscriber %s extended their borrow duration of %s by %d days, the new due date is %s"
								.formatted(borrow.getSubscriber().getName(), borrow.getBook().getTitle(), days,
										newDueDate));
				pstmt.setTimestamp(2, Timestamp.valueOf(now), ILTimeZone);
				pstmt.execute();
			}

			// Set the notification time for sending a reminder message the day before the
			// new due date
			LocalDateTime notification = LocalDateTime.of(newDueDate.minusDays(1), LocalTime.now());
			pstmt = conn
					.prepareStatement("UPDATE commands SET time_of_execution=? WHERE command = ? AND identifyer = ?");
			pstmt.setTimestamp(1, Timestamp.valueOf(notification), ILTimeZone);
			pstmt.setString(2, "sendMessage");
			pstmt.setString(3, "%s;%s".formatted(borrow.getSubscriber().getId(), borrow.getBook().getCopyID()));

			// Commit the transaction
			conn.commit();
			return true; // Return true if the extension is successful
		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Calculates the availability status of a book title based on the number of
	 * copies, active borrows, and active orders.
	 *
	 * @param title The {@link BookTitle} for which to calculate availability.
	 * @return The availability status: positive for available copies, zero if all
	 *         are borrowed, negative for active orders, or {@code null} if an error
	 *         occurs.
	 */
	public Integer getTitleAvailability(BookTitle title) {
		// 0 < num <= num of copies : { there are between 0 to num of copies active
		// borrows for the title}
		// num = 0 : {all the copies are borrowed}
		// -num of copies <= num < 0 : {there are |num| active orders for the title}
		// num = num of copies - num of borrows - num of orders
		int sum = 0;
		ResultSet rs;

		try {
			// Query to retrieve the number of copies for the given title.
			pstmt = conn.prepareStatement("SELECT num_of_copies FROM titles WHERE title_id = ?;");
			pstmt.setInt(1, title.getTitleID());
			rs = pstmt.executeQuery();

			// If the title exists in the database, add the number of copies to the sum.
			if (rs.next()) {
				sum += rs.getInt(1); // + num of copies
			} else {
				return null;
			}

			// Query to retrieve the total number of borrowed copies for the given title.
			pstmt = conn.prepareStatement("SELECT sum(is_borrowed) FROM copies WHERE title_id = ? GROUP BY title_id;");
			pstmt.setInt(1, title.getTitleID());
			rs = pstmt.executeQuery();

			// If the result is found, subtract the number of borrowed copies from the sum.
			if (rs.next()) {
				sum -= rs.getInt(1); // - num of borrows
			} else {
				return null;
			}

			// Query to retrieve the number of active orders for the given title.
			pstmt = conn.prepareStatement("SELECT num_of_orders FROM titles WHERE title_id = ?;");
			pstmt.setInt(1, title.getTitleID());
			rs = pstmt.executeQuery();

			// If the result is found, subtract the number of orders from the sum.
			if (rs.next()) {
				sum -= rs.getInt(1); // - num of orders
			} else {
				return null;
			}

			// Return the calculated sum, which represents the availability for the title.
			return sum;
		} catch (SQLException e) {
			// If any SQL exception occurs, return null to indicate an error.
			return null;
		}
	}

	/**
	 * Checks if a book title has active orders.
	 * 
	 * This method queries the database to determine if there are any active orders
	 * for a given book title by checking if the number of orders is greater than
	 * zero.
	 * 
	 * @param titleID The ID of the book title to check for active orders.
	 * @return {@code true} if there are active orders for the title, {@code false}
	 *         otherwise. If an error occurs while querying the database, the method
	 *         returns {@code null}.
	 */
	public Boolean isTitleOrdered(int titleID) {
		try {
			// Prepare a query to check if there are any orders for the given title.
			pstmt = conn.prepareStatement("SELECT num_of_orders>0 FROM titles WHERE title_id = ?;");
			pstmt.setInt(1, titleID);
			ResultSet rs = pstmt.executeQuery();

			// If the result is found, return the boolean indicating if there are orders.
			if (rs.next())
				return rs.getBoolean(1);
			// If no orders exist, return false.
			return false;
		} catch (SQLException e) {
			// Return null in case of an error querying the database.
			return null;
		}
	}

	/**
	 * Returns a borrowed book and updates the borrow and copy information in the
	 * database.
	 * 
	 * This method updates the return date for the given book copy in the borrows
	 * table, marks the copy as available in the copies table, and logs the return
	 * activity in the history table. If the return is late, it records the number
	 * of late days.
	 * 
	 * @param book         The book copy that is being returned.
	 * @param isLateReturn A boolean indicating if the book is being returned late.
	 * @return {@code true} if the book is successfully returned and the database is
	 *         updated, {@code false} if the return process fails due to an error or
	 *         invalid borrow data.
	 */
	public Boolean returnBook(BookCopy book, boolean isLateReturn) {
		try {
			// Get the current date for the return transaction
			LocalDate today = LocalDate.now();

			// Get the active borrow record for the given book copy
			Borrow borrow = getCopyActiveBorrow(book);
			if (borrow == null)
				return false; // Return false if no active borrow is found

			// Update the return date in the borrows table
			pstmt = conn.prepareStatement(
					"UPDATE borrows SET date_of_return = ? WHERE subscriber_id = ? AND copy_id =? AND date_of_borrow = ?;");
			pstmt.setDate(1, Date.valueOf(today), ILTimeZone);
			pstmt.setInt(2, borrow.getSubscriber().getId());
			pstmt.setInt(3, book.getCopyID());
			pstmt.setDate(4, Date.valueOf(borrow.getDateOfBorrow()), ILTimeZone);
			pstmt.execute();

			// Mark the copy as no longer borrowed in the copies table
			pstmt = conn.prepareStatement("UPDATE copies SET is_borrowed = ? WHERE copy_id = ?;");
			pstmt.setBoolean(1, false);
			pstmt.setInt(2, book.getCopyID());
			pstmt.execute();

			// Log the return activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, borrow.getSubscriber().getId());

			// Log return or late return activity
			if (!isLateReturn) {
				pstmt.setString(2, "return");
				pstmt.setString(3, "\"%s\" return by %s on %s".formatted(book.getTitle(),
						borrow.getSubscriber().getName(), today));
			} else {
				int late = today.compareTo(borrow.getDueDate());
				pstmt.setString(2, "late return");
				pstmt.setString(3, "\"%s\" late return by %s on %s late by %d days".formatted(book.getTitle(),
						borrow.getSubscriber().getName(), today, Math.abs(late)));
			}

			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();

			// Commit the transaction
			conn.commit();
			return true; // Return true if the return is successfully processed

		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Freezes the subscriber's account by setting its status to "frozen" in the
	 * database.
	 * 
	 * This method updates the subscriber's status to "frozen" and logs the activity
	 * in the history table. It also creates a command to automatically unfreeze the
	 * subscriber's account after one month. Additionally, the number of active and
	 * frozen subscribers is recorded in the activity log.
	 * 
	 * @param subID The ID of the subscriber whose account is being frozen.
	 * @return {@code true} if the subscriber's account is successfully frozen and
	 *         the necessary changes are made in the database, {@code false} if an
	 *         error occurs. {@code null} is returned if the subscriber does not
	 *         exist.
	 */
	public Boolean freezeSubscriber(int subID) {
		try {
			// Get the current date and the subscriber object
			LocalDate today = LocalDate.now();

			Subscriber sub = getSubscriberByID(subID);
			if (sub == null)
				return null;// Return null if the subscriber does not exist

			// Update the subscriber's status to "frozen" in the subscribers table
			pstmt = conn.prepareStatement("UPDATE subscribers SET subscriber_status = ? WHERE subscriber_id = ? ;");
			pstmt.setString(1, "frozen");
			pstmt.setInt(2, subID);
			pstmt.execute();

			// Get the count of active and frozen subscribers
			pstmt = conn.prepareStatement(
					"SELECT subscriber_status, COUNT(*) FROM subscribers GROUP BY subscriber_status;");
			ResultSet rs = pstmt.executeQuery();
			int numActive = 0;
			int numFrozen = 0;
			while (rs.next()) {
				if (rs.getString(1).equals("active"))
					numActive = rs.getInt(2);
				if (rs.getString(1).equals("frozen"))
					numFrozen = rs.getInt(2);
			}

			// Log the freeze Subscriber activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, subID);
			pstmt.setString(2, "freeze");
			pstmt.setString(3, "%s got frozen on %s until %s;%d;%d".formatted(sub.getName(), today, today.plusMonths(1),
					numActive, numFrozen));
			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();

			// Schedule the unfreeze action after one month
			LocalDateTime unfreezeTime = LocalDateTime.now().plusMonths(1);
			createCommand("unfreeze", "%d".formatted(sub.getId()), unfreezeTime, "%d".formatted(sub.getId()), false);

			// Commit the transaction
			conn.commit();
			return true;

		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Unfreezes a subscriber's account by changing their status to "active" in the
	 * database.
	 * 
	 * This method updates the subscriber's status to "active" and logs the activity
	 * in the history table. It also retrieves and logs the number of active and
	 * frozen subscribers at the time of the unfreeze. The changes are committed to
	 * the database to ensure data consistency.
	 * 
	 * @param subID The ID of the subscriber whose account is being unfrozen.
	 * @return {@code true} if the subscriber's account is successfully unfrozen and
	 *         the necessary changes are made in the database, {@code false} if an
	 *         error occurs. {@code null} is returned if the subscriber does not
	 *         exist.
	 */
	public Boolean unfreezeSubscriber(int subID) {
		try {
			// Get the current date and the subscriber object
			LocalDate today = LocalDate.now();

			Subscriber sub = getSubscriberByID(subID);
			if (sub == null)
				return null;// Return null if the subscriber does not exist

			// Update the subscriber's status to "active" in the subscribers table
			pstmt = conn.prepareStatement("UPDATE subscribers SET subscriber_status = ? WHERE subscriber_id = ? ;");
			pstmt.setString(1, "active");
			pstmt.setInt(2, subID);
			pstmt.execute();

			// Get the count of active and frozen subscribers
			pstmt = conn.prepareStatement(
					"SELECT subscriber_status, COUNT(*) FROM subscribers GROUP BY subscriber_status;");
			ResultSet rs = pstmt.executeQuery();

			int numActive = 0;
			int numFrozen = 0;
			while (rs.next()) {
				if (rs.getString(1).equals("active"))
					numActive = rs.getInt(2);
				if (rs.getString(1).equals("frozen"))
					numFrozen = rs.getInt(2);
			}

			// Log the unfreeze Subscriber activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, subID);
			pstmt.setString(2, "unfreeze");
			pstmt.setString(3, "%s got unfrozen on %s;%d;%d".formatted(sub.getName(), today, numActive, numFrozen));

			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();
			// Commit the transaction
			conn.commit();
			return true; // Return true if the unfreeze operation was successful

		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return false; // Return false if an error occurs
		}
	}

	/**
	 * Retrieves the activity history of a subscriber from the history table.
	 * 
	 * This method fetches all activities associated with a specific subscriber from
	 * the database, ordered by the activity date. It returns a list of
	 * {@link Activity} objects representing each activity, or {@code null} if the
	 * subscriber does not exist or an error occurs during the database query.
	 * 
	 * @param subID The ID of the subscriber whose activity history is being
	 *              retrieved.
	 * @return A {@link Activity} objects representing the subscriber's activity
	 *         history, ordered by date. Returns {@code null} if the subscriber does
	 *         not exist or if an error occurs.
	 */
	public List<Activity> getSubscriberHistory(int subID) {
		try {
			// Fetch the subscriber based on the given ID
			Subscriber sub = getSubscriberByID(subID);
			if (sub == null)
				return null;// Return null if the subscriber does not exist

			// Prepare and execute the SQL query to fetch activities from the history table
			pstmt = conn.prepareStatement("SELECT * FROM history WHERE subscriber_id = ? ORDER BY activity_date;");
			pstmt.setInt(1, subID);
			ResultSet rs = pstmt.executeQuery();

			// Create a list to store the activities
			List<Activity> ret = new ArrayList<>();

			// Iterate over the result set and create Activity objects
			while (rs.next()) {
				Activity activity = new Activity(rs.getInt(1), rs.getString(3), rs.getString(4).split(";")[0],
						rs.getDate(5, ILTimeZone).toLocalDate());
				ret.add(activity);// Add the activity to the list
			}

			return ret;// Return the list of activities

		} catch (SQLException e) {
			return null;// Return null if any SQL error occurs
		}
	}

	/**
	 * Retrieves and processes commands from the database that have a scheduled
	 * execution time earlier than the current time.
	 * 
	 * This method queries the `commands` table for commands whose
	 * `time_of_execution` is in the past. It processes each command into a
	 * {@link Message} object and returns a list of these messages. After retrieving
	 * and processing the commands, the method deletes them from the database to
	 * prevent further execution.
	 * 
	 * @return A list of {@link Message} objects representing the commands that were
	 *         retrieved and processed. If an error occurs or no commands match the
	 *         criteria, {@code null} is returned.
	 */
	public List<Message> getCommands() {
		LocalDateTime now = LocalDateTime.now();
		try {
			// Prepare SQL query to fetch commands scheduled to execute before the current
			// time
			pstmt = conn.prepareStatement("SELECT * FROM commands WHERE time_of_execution < ?");
			pstmt.setTimestamp(1, Timestamp.valueOf(now), ILTimeZone);
			ResultSet rs = pstmt.executeQuery();

			// Initialize lists to store command IDs and processed messages
			List<Integer> commandIDs = new ArrayList<>();
			List<Message> ret = new ArrayList<>();

			// Iterate through the result set to process each command
			while (rs.next()) {
				commandIDs.add(rs.getInt(1));
				// Create a new Message object for each command
				Message msg = new Message(rs.getString(2));

				// Split the command arguments by semicolon and add them to the Message object
				for (String arg : rs.getString(3).split(";")) {
					msg.addArgument(arg);
				}

				// Add the Message to the return list
				ret.add(msg);
			}

			// Delete the processed commands from the database
			for (int i : commandIDs) {
				pstmt = conn.prepareStatement("DELETE FROM commands WHERE id = ?");
				pstmt.setInt(1, i);
				pstmt.execute();
			}

			// Commit the transaction
			conn.commit();
			return ret;

		} catch (SQLException e) {
			rollback(); // Rollback transaction if any error occurs
			return null; // Return null if an error occurs
		}
	}

	/**
	 * Retrieves a list of active borrows for a specific subscriber.
	 * 
	 * This method queries the `borrows` table for all active borrows (where
	 * `date_of_return` is `NULL`) associated with a particular subscriber. It
	 * creates and returns a list of `Borrow` objects representing the active borrow
	 * records for the given subscriber.
	 * 
	 * @param sub The {@link Subscriber} whose active borrows are to be retrieved.
	 * @return A list of {@link Borrow} objects representing the subscriber's active
	 *         borrows. If no active borrows are found or if an error occurs,
	 *         {@code null} is returned.
	 */
	public List<Borrow> getSubscriberActiveBorrows(Subscriber sub) {
		try {
			// Prepare SQL query to fetch active borrows for the given subscriber
			pstmt = conn.prepareStatement("SELECT * FROM borrows WHERE subscriber_id = ? AND date_of_return IS NULL");
			pstmt.setInt(1, sub.getId());
			ResultSet rs = pstmt.executeQuery();

			// Initialize a list to store the active borrow records
			List<Borrow> ret = new ArrayList<>();

			// Iterate through the result set and create Borrow objects for each active
			// borrow
			while (rs.next()) {
				BookCopy copy = getCopyByID(rs.getInt(3));
				if (copy == null) {
					return null;
				}
				// Add the borrow record to the list
				ret.add(new Borrow(sub, copy, rs.getDate(4, ILTimeZone).toLocalDate(),
						rs.getDate(5, ILTimeZone).toLocalDate(), null));
			}
			// Return the list of active borrows
			return ret;

		} catch (SQLException e) {
			// If an error occurs, return null
			return null;
		}

	}

	/**
	 * Retrieves a list of messages for the librarian, ordered by time.
	 * 
	 * This method queries the `librarian_messages` table to fetch all the messages
	 * for the librarian, ordering them by the time of arrival. It returns the
	 * messages as a list of strings, where each string represents a message. If an
	 * error occurs during the database query, {@code null} is returned.
	 * 
	 * @return A list of strings representing the librarian's messages, ordered by
	 *         time. If an error occurs, {@code null} is returned.
	 */
	public List<String> getLibrarianMessages() {
		try {
			// Prepare SQL query to fetch all librarian messages ordered by time
			pstmt = conn.prepareStatement("SELECT message FROM librarian_messages ORDER BY time");
			ResultSet rs = pstmt.executeQuery();

			// Initialize a list to store the retrieved messages
			List<String> ret = new ArrayList<>();
			while (rs.next()) {
				ret.add(rs.getString(1));
			}
			// Return the list of messages
			return ret;
		} catch (SQLException e) {
			// If an error occurs, return null
			return null;
		}
	}

	/**
	 * Clears all messages from the librarian messages table.
	 * 
	 * This method deletes all records from the `librarian_messages` table. It
	 * commits the transaction if the deletion is successful, and returns
	 * {@code true}. If an error occurs during the deletion, the transaction is
	 * rolled back and the method returns {@code false}.
	 * 
	 * @return {@code true} if the messages were successfully cleared, {@code false}
	 *         if an error occurred during the process.
	 */
	public Boolean clearLibrarianMessages() {
		try {
			// Prepare and execute SQL query to delete all messages from the
			// librarian_messages table
			pstmt = conn.prepareStatement("DELETE FROM librarian_messages;");
			pstmt.execute();

			// Commit the transaction
			conn.commit();
			return true;
		} catch (SQLException e) {
			// If an error occurs, rollback the transaction and return false
			rollback();
			return false;
		}
	}

	/**
	 * Orders a book for a subscriber by adding an order record, updating the book's
	 * order count, and logging the activity in the history table.
	 * 
	 * @param subID   The subscriber's ID.
	 * @param titleID The book title's ID.
	 * @return {@code true} if the order was successful, {@code false} if an error
	 *         occurred.
	 */
	public Boolean orderBook(int subID, int titleID) {
		try {
			// Retrieve subscriber and book title
			Subscriber sub = getSubscriberByID(subID);
			if (sub == null) {
				return false;
			}
			BookTitle title = getTitleByID(titleID);
			if (title == null) {
				return false;
			}
			LocalDate today = LocalDate.now();
			LocalDateTime now = LocalDateTime.now();

			// Insert order, update title orders, and log activity
			pstmt = conn.prepareStatement("INSERT INTO orders(subscriber_id, title_id, order_date) VALUE (?,?,?);");
			pstmt.setInt(1, subID);
			pstmt.setInt(2, titleID);
			pstmt.setTimestamp(3, Timestamp.valueOf(now), ILTimeZone);
			pstmt.execute();

			// Update the number of orders for the book title
			pstmt = conn.prepareStatement("UPDATE titles SET num_of_orders = num_of_orders + 1 WHERE title_id = ?;");
			pstmt.setInt(1, titleID);
			pstmt.execute();

			// Log the order activity in the history table
			pstmt = conn.prepareStatement(
					"INSERT INTO history(subscriber_id,activity_type,activity_description,activity_date) VALUE(?,?,?,?)");
			pstmt.setInt(1, subID);
			pstmt.setString(2, "order");
			pstmt.setString(3, "%s ordered the book \"%s\" on %s".formatted(sub.getName(), title, today));
			pstmt.setDate(4, Date.valueOf(today), ILTimeZone);
			pstmt.execute();

			// Commit the transaction
			conn.commit();
			return true;
		} catch (SQLException e) {
			// Rollback transaction in case of an error
			rollback();
			return false;
		}
	}

	/**
	 * Retrieves the total number of borrowed copies for a given book title.
	 *
	 * @param title The book title.
	 * @return The number of borrowed copies, or {@code null} if an error occurs or
	 *         no copies are borrowed.
	 */
	public Integer getNumOfCopies(BookTitle title) {
		try {
			// Prepare the SQL query to select the sum of borrowed copies for the given book
			// title
			pstmt = conn.prepareStatement("SELECT sum(is_borrowed) FROM copies WHERE title_id = ? GROUP BY title_id;");
			pstmt.setInt(1, title.getTitleID());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				// Return the number of borrowed copies
				return rs.getInt(1);
			} else {
				// If no result, return null (no borrowed copies)
				return null;
			}
		} catch (SQLException e) {
			// If an error occurs during query execution, return null
			return null;
		}
	}

	/**
	 * Retrieves a list of all subscribers from the database.
	 *
	 * @return A list of all subscribers, or {@code null} if an error occurs.
	 */
	public List<Subscriber> getAllSubscribers() {
		try {
			// Prepare the SQL query to select all subscriber details from the subscribers
			// table
			pstmt = conn.prepareStatement("SELECT * FROM subscribers;");
			ResultSet rs = pstmt.executeQuery();

			// Initialize an empty list to store the subscriber objects
			List<Subscriber> ret = new ArrayList<>();
			// Iterate through the result set and add each subscriber to the list
			while (rs.next()) {
				ret.add(new Subscriber(rs.getInt("subscriber_id"), rs.getString("subscriber_name"),
						rs.getString("subscriber_phone_number"), rs.getString("subscriber_email"),
						rs.getString("subscriber_status")));
			}
			// Return the list of subscribers
			return ret;
		} catch (SQLException e) {
			// If an error occurs during query execution, return null
			return null;
		}
	}

	/**
	 * Retrieves all active orders for a specific subscriber from the database.
	 *
	 * @param sub The subscriber whose active orders are to be retrieved.
	 * @return A list of active orders for the subscriber, or {@code null} if an
	 *         error occurs.
	 */
	public List<Order> getSubscriberActiveOrders(Subscriber sub) {
		try {
			// Prepare the SQL query to retrieve all orders for the given subscriber
			pstmt = conn.prepareStatement("SELECT * FROM orders WHERE subscriber_id = ? ORDER BY order_date");
			pstmt.setInt(1, sub.getId());
			ResultSet rs = pstmt.executeQuery();

			// Initialize an empty list to store the active orders
			List<Order> ret = new ArrayList<>();

			// Iterate through the result set
			while (rs.next()) {
				BookTitle title = getTitleByID(rs.getInt(3));
				if (title == null) {
					System.out.println("title id " + rs.getInt(3) + " not found");
					return null;
				}
				Order order = new Order(rs.getInt(1), sub, title, rs.getTimestamp(5, ILTimeZone).toLocalDateTime());

				// If a copy is assigned to the order, set the copy and arrival date
				if (rs.getInt(4) != 0) {
					BookCopy copy = getCopyByID(rs.getInt(4));
					order.setCopy(copy);
					order.setAriveDate(rs.getDate(6, ILTimeZone).toLocalDate());
				}
				// Add the created order to the list of active orders
				ret.add(order);
			}

			return ret;
		} catch (SQLException e) {
			// If an error occurs, return null
			return null;
		}

	}

	/**
	 * Updates an order by assigning a copy to it and setting the arrival date.
	 *
	 * @param copy The book copy that will be assigned to the order.
	 * @return {@code true} if the order was successfully updated, {@code false}
	 *         otherwise.
	 */
	public Boolean updateOrder(BookCopy copy) {
		try {
			// Prepare the SQL query to fetch the first order that does not have an assigned
			// copy
			pstmt = conn.prepareStatement(
					"SELECT * FROM orders WHERE title_id = ? AND copy_id is null ORDER BY order_date;");
			pstmt.setInt(1, copy.getTitle().getTitleID());
			ResultSet rs = pstmt.executeQuery();

			// If no matching orders are found, return false
			if (!rs.next()) {
				return false;
			}
			// Retrieve the order ID of the first matching order
			int orderID = rs.getInt(1);

			// Get the current date
			LocalDate today = LocalDate.now();

			// Prepare the SQL query to update the order with the copy ID and arrival date
			pstmt = conn.prepareStatement("UPDATE orders SET copy_id = ?, arive_date = ? WHERE order_id = ?;");
			pstmt.setInt(1, copy.getCopyID());
			pstmt.setDate(2, Date.valueOf(today), ILTimeZone);
			pstmt.setInt(3, orderID);
			pstmt.execute();

			// Commit the transaction to persist the changes
			conn.commit();
			return true;
		} catch (SQLException e) {
			// If an error occurs, rollback the transaction to maintain data integrity
			rollback();
			return false;
		}

	}

	/**
	 * Creates a command record in the database with the specified parameters.
	 * 
	 * @param command    The command to be inserted into the database.
	 * @param arguments  The arguments associated with the command.
	 * @param timeOfExe  The time when the command is to be executed.
	 * @param identifyer A unique identifier for the command.
	 * @param commit     If {@code true}, the transaction will be committed;
	 *                   otherwise, the method call is a part of a larger
	 *                   transaction.
	 * @return {@code true} if the command was successfully inserted, {@code false}
	 *         if there was an error or rollback occurred.
	 * @throws SQLException if there is a database access error and commit is set to
	 *                      {@code false}.
	 */
	public Boolean createCommand(String command, String arguments, LocalDateTime timeOfExe, String identifyer,
			boolean commit) throws SQLException {
		try {
			// Prepare the SQL query to insert a new command record
			pstmt = conn.prepareStatement(
					"INSERT INTO commands(command, arguments, time_of_execution, identifyer) VALUE(?,?,?,?)");
			pstmt.setString(1, command);
			pstmt.setString(2, arguments);
			pstmt.setTimestamp(3, Timestamp.valueOf(timeOfExe), ILTimeZone);
			pstmt.setString(4, identifyer);
			pstmt.execute();

			// If commit flag is true, commit the transaction
			if (commit) {
				conn.commit();
			}
			return true;

		} catch (SQLException e) {
			// If an error occurs and commit flag is true, perform rollback and return false
			if (commit) {
				rollback();
				return false;
			} else {
				// If commit flag is false, rethrow the SQLException
				throw e;
			}

		}

	}

	/**
	 * Overloaded method to create a command with automatic commit. Meant for use as
	 * a whole transaction.
	 * 
	 * @param command    The command to be inserted into the database.
	 * @param arguments  The arguments associated with the command.
	 * @param timeOfExe  The time when the command is to be executed.
	 * @param identifyer A unique identifier for the command.
	 * @return {@code true} if the command was successfully inserted, {@code false}
	 *         if there was an error.
	 */
	public Boolean createCommand(String command, String arguments, LocalDateTime timeOfExe, String identifyer) {
		try {
			// Calls the original method with commit set to true
			return createCommand(command, arguments, timeOfExe, identifyer, true);
		} catch (SQLException e) {
			// Return false if an error occurs
			return false;
		}
	}

	/**
	 * Cancels a command by deleting it from the database with automatic commit.
	 * Meant for use as a whole transaction.
	 * 
	 * @param command    The command type to be deleted.
	 * @param identifyer The unique identifier of the command to be deleted.
	 * @return {@code true} if the command was successfully deleted, {@code false}
	 *         if there was an error.
	 */
	public Boolean cancelCommand(String command, String identifyer) {
		try {
			// Calls the original method with commit set to true
			return cancelCommand(command, identifyer, true);
		} catch (SQLException e) {
			// Return false if an error occurs
			return false;
		}

	}

	/**
	 * Cancels a command by deleting it from the database.
	 * 
	 * @param command    The command to be deleted.
	 * @param identifyer The unique identifier of the command to be deleted.
	 * @param commit     If {@code true}, the transaction will be committed;
	 *                   otherwise, the method call is a part of a larger
	 *                   transaction.
	 * @return {@code true} if the command was successfully deleted, {@code false}
	 *         if an error occurred or rollback was triggered.
	 * @throws SQLException if there is a database access error and commit is set to
	 *                      {@code false}.
	 */
	public Boolean cancelCommand(String command, String identifyer, boolean commit) throws SQLException {
		try {
			// Prepare the SQL query to delete the command from the database
			pstmt = conn.prepareStatement("DELETE FROM commands WHERE command = ? AND identifyer = ?");
			pstmt.setString(1, command);
			pstmt.setString(2, identifyer);
			pstmt.execute();

			// If commit flag is true, commit the transaction
			if (commit)
				conn.commit();
			return true;
		} catch (Exception e) {
			// If an error occurs and commit flag is true, perform rollback and return false
			if (commit) {
				rollback();
				return false;
			} else {
				// If commit flag is false, rethrow the exception
				throw e;
			}

		}

	}

	/**
	 * Cancels an order by deleting the order record from the database and adjusting
	 * the number of orders for the associated book title.
	 * 
	 * @param copyID The unique identifier for the book copy associated with the
	 *               order to be canceled.
	 * @return {@code true} if the order was successfully canceled, {@code false} if
	 *         an error occurred or the book copy is not found.
	 */
	public Boolean cancelOrder(int copyID) {
		try {
			BookCopy copy = getCopyByID(copyID);
			// If no BookCopy is found, return null
			if (copy == null)
				return null;

			// Prepare and execute the SQL query to delete the order associated with the
			// given copyID
			pstmt = conn.prepareStatement("DELETE FROM orders WHERE copy_id = ?");
			pstmt.setInt(1, copyID);
			pstmt.execute();

			// Prepare and execute the SQL query to decrease the number of orders for the
			// associated book title
			pstmt = conn.prepareStatement("UPDATE titles SET num_of_orders = num_of_orders-1 WHERE title_id = ?");
			pstmt.setInt(1, copy.getTitle().getTitleID());
			pstmt.execute();

			conn.commit();
			return true;
		} catch (Exception e) {
			// Rollback the transaction in case of any error and return false
			rollback();
			return false;
		}
	}

	/**
	 * Retrieves the closest return date for a book title by querying the borrow
	 * records for copies of the book. If there are borrowed copies, the method
	 * returns the latest due date; otherwise, it returns {@code null}.
	 * 
	 * @param title The book title for which to find the closest return date.
	 * @return The closest return date if there are borrowed copies, or {@code null}
	 *         if no copies are borrowed or there is an error.
	 */
	public LocalDate getTitleClosestReturnDate(BookTitle title) {
		try {
			// If the title has a "magic number" greater than 0, return null (indicating no
			// borrowable copies)
			if (getTitleAvailability(title) > 0)
				return null;

			// Prepare and execute a query to get the latest due date for borrowed copies of
			// the title
			pstmt = conn.prepareStatement(
					"SELECT due_date FROM (SELECT copy_id FROM copies WHERE title_id = ? AND is_borrowed = TRUE) AS a NATURAL JOIN borrows ORDER BY due_date DESC;");
			pstmt.setInt(1, title.getTitleID());
			ResultSet rs = pstmt.executeQuery();

			// If a result is found, return the due date of the first result (latest return
			// date)
			if (rs.next())
				return rs.getDate(1, ILTimeZone).toLocalDate();

			// If no borrowed copies are found, return null
			return null;
		} catch (SQLException e) {
			// Return null if there is an error executing the query
			return null;
		}

	}

	/**
	 * Retrieves the subscribers status on a given month by querying the history of
	 * activities. It returns the number of active/frozen subscribers for each day
	 * in the month.
	 * 
	 * @param date The reference date (any date within the target month).
	 * @return A map where keys are the dates in the month, and values are arrays
	 *         containing two integers: [0] - number of active subscribers, [1] -
	 *         number of frozen subscribers.
	 */
	public Map<LocalDate, Integer[]> getSubscribersStatusOnMonth(LocalDate date) {
		Month curMonth = date.getMonth(); // Get the current month from the provided date
		date = LocalDate.of(date.getYear(), curMonth, 1).plusMonths(1).minusDays(1); // Get the last day of the month

		try {
			Map<LocalDate, Integer[]> ret = new HashMap<>();

			// Prepare the SQL query to retrieve the subscriber activity data
			pstmt = conn.prepareStatement("SELECT h.activity_date, h.activity_description " + "FROM history h "
					+ "INNER JOIN (" + "SELECT activity_date, MAX(item_id) AS item_id " + "FROM history "
					+ "WHERE activity_date <= ? AND activity_type IN ('new subscriber', 'freeze', 'unfreeze') "
					+ "GROUP BY activity_date) subquery "
					+ "ON h.activity_date = subquery.activity_date AND h.item_id = subquery.item_id "
					+ "ORDER BY h.activity_date DESC;");

			pstmt.setDate(1, Date.valueOf(date), ILTimeZone); // Set the provided date as parameter
			ResultSet rs = pstmt.executeQuery();

			Integer[] lst = new Integer[2];// Array to hold subscriber counts

			while (rs.next()) {
				lst[0] = Integer.parseInt(rs.getString(2).split(";")[1]); // active count
				lst[1] = Integer.parseInt(rs.getString(2).split(";")[2]); // frozen count
				ret.put(rs.getDate(1).toLocalDate(), lst.clone());
				// If the result is from a different month, stop processing
				if (!rs.getDate(1).toLocalDate().getMonth().equals(curMonth)) {
					break;
				}
			}
			return ret; // Return the map containing subscriber statuses
		} catch (NumberFormatException | SQLException e) {
			return null; // Return null if any exception occurs
		}
	}

	/**
	 * Counts the number of new subscribers in a given month.
	 * 
	 * @param date The reference date (any date within the target month).
	 * @return The number of new subscribers for the specified month.
	 */
	public Integer SumNewSubscriber(LocalDate date) {
		// Format the month and year for the query
		String dateWildCard = "%04d-%02d-%%".formatted(date.getYear(), date.getMonth().getValue());
		try {
			// Prepare the SQL query to count new subscribers
			pstmt = conn.prepareStatement(
					"SELECT count(*) FROM history where activity_date like ? and activity_type = 'new subscriber';");
			pstmt.setString(1, dateWildCard);
			ResultSet rs = pstmt.executeQuery();
			
			// Return the count of new subscribers
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0; // Return 0 if no new subscribers are found

		} catch (SQLException e) {
			return null; // Return null in case of any SQL exceptions
		}

	}

	/**
	 * Saves a graph's data (in binary format) to the database.
	 * 
	 * @param day        The date associated with the graph.
	 * @param graph_type The type of the graph (e.g., 'subscriber count').
	 * @param data       The graph data in byte array format.
	 * @return true if the graph is successfully saved, false otherwise.
	 */
	public Boolean saveGraph(LocalDate day, String graph_type, byte[] data) {

		try {
			// Prepare the SQL query to insert the graph data into the database
			pstmt = conn.prepareStatement(
					"INSERT INTO graphs (graph_type, graph_month, graph_year, graph) VALUE ( ?, ?, ?, ?);");
			pstmt.setString(1, graph_type);
			pstmt.setInt(2, day.getMonthValue());
			pstmt.setInt(3, day.getYear());

			// Convert the byte array into an InputStream and set it as the graph data
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			pstmt.setBlob(4, in);

			pstmt.execute(); // Execute the insert query
			conn.commit(); // Commit the transaction
			return true; // Return true if successful

		} catch (SQLException e) {
			rollback(); // Rollback in case of an error
			return false;
		}

	}

	/**
	 * Retrieves a saved graph from the database based on the specified year, month,
	 * and graph type.
	 * 
	 * @param year      The year associated with the graph.
	 * @param month     The month associated with the graph.
	 * @param graphType The type of the graph.
	 * @return A DataInputStream containing the graph data, or null if the graph is
	 *         not found.
	 */
	public DataInputStream getGraph(int year, int month, String graphType) {
		try {
			// Prepare the SQL query to retrieve the graph data
			pstmt = conn.prepareStatement(
					"SELECT graph FROM graphs WHERE graph_type = ? AND graph_month = ? AND graph_year = ?;");
			pstmt.setString(1, graphType);
			pstmt.setInt(2, month);
			pstmt.setInt(3, year);

			ResultSet rs = pstmt.executeQuery();

			// If a result is found, return the graph data as an InputStream
			if (rs.next()) {
				return new DataInputStream(rs.getBinaryStream(1));
			}

			return null; // Return null if no graph is found
		} catch (SQLException e) {
			return null; // Return null if there was an error
		}

	}

	/**
	 * Retrieves the average borrow time and the percentage of late returns for each
	 * genre in the given month.
	 * 
	 * @param date The reference date (any date within the target month).
	 * @return A map where keys are genres and values are arrays with the following:
	 *         [0] - average borrow time, [1] - percentage of late returns.
	 */
	public Map<String, Double[]> getBorrowTimeOnMonth(LocalDate date) {
		// Format the month and year for the query
		String dateWildCard = "%04d-%02d-%%".formatted(date.getYear(), date.getMonth().getValue());
		try {
			Map<String, Double[]> ret = new HashMap<>();
			// Prepare the SQL query to calculate average borrow time and late return
			// percentage by genre
			pstmt = conn.prepareStatement(
					"SELECT genre, AVG(DATEDIFF(date_of_return, date_of_borrow)), SUM(CASE WHEN date_of_return > due_date THEN 1 ELSE 0 END)/COUNT(*)*100 FROM (titles NATURAL JOIN copies) NATURAL JOIN borrows WHERE date_of_return LIKE ? GROUP BY genre;");
			pstmt.setString(1, dateWildCard);
			ResultSet rs = pstmt.executeQuery();

			// Process the result set
			Double[] lst = new Double[2];
			while (rs.next()) {
				lst[0] = rs.getDouble(2); // Average borrow time
				lst[1] = rs.getDouble(3); // late return percentage
				ret.put(rs.getString(1), lst.clone());
			}
			return ret; // Return the map containing the data
		} catch (NumberFormatException | SQLException e) {
			return null; // Return null if there is an error
		}
	}

	/**
	 * Retrieves the average borrow time for a given month.
	 * 
	 * @param date The reference date (any date within the target month).
	 * @return The average borrow time in days, or 0.0 if no data is found.
	 */
	public Double getAvgBorrowTimeOnMonth(LocalDate date) {
		// Format the month and year for the query
		String dateWildCard = "%04d-%02d-%%".formatted(date.getYear(), date.getMonth().getValue());
		try {
			// Prepare the SQL query to calculate the average borrow time
			pstmt = conn.prepareStatement(
					"SELECT AVG(DATEDIFF(date_of_return, date_of_borrow)) FROM (titles NATURAL JOIN copies) NATURAL JOIN borrows WHERE date_of_return LIKE ?;");
			pstmt.setString(1, dateWildCard);
			ResultSet rs = pstmt.executeQuery();

			// Return the average borrow time if found
			if (rs.next()) {
				return rs.getDouble(1);
			}
			return 0.0; // Return 0.0 if no data is found
		} catch (NumberFormatException | SQLException e) {
			return null; // Return null in case of an error
		}
	}
}
