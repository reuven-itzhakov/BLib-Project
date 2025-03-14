package server;

import java.util.List;

import logic.Message;

/**
 * The ServerTimer class runs a timer thread that periodically checks and
 * executes commands on the server at a specified interval. It executes each
 * command from the command list sequentially and sleeps for a predefined delay
 * between each check.
 */
public class ServerTimer implements Runnable {
	private Thread t; // Thread that runs the timer
	private BLibServer server; // Reference to the server instance
	private static ServerTimer instance = null; // Singleton instance
	private int delayMinutes = 30; // Delay time in minutes between command checks

	/**
	 * Starts the ServerTimer if it hasn't been started yet.
	 *
	 * @param server the server instance to associate with the timer
	 */
	public static void start(BLibServer server) {
		// If the instance is not yet created, initialize it
		if (!(instance instanceof ServerTimer)) {
			instance = new ServerTimer(server);
		}
	}

	/**
	 * Private constructor for ServerTimer that initializes the server and starts
	 * the timer thread.
	 *
	 * @param server the server instance to associate with the timer
	 */
	private ServerTimer(BLibServer server) {
		this.server = server;
		t = new Thread(this); // Create a new thread to run the timer
		t.setDaemon(true); // Make the thread a daemon thread so it does not block shutdown
		t.start(); // Start the timer thread
	}

	/**
	 * The main method that runs the timer logic. This method is executed in a
	 * separate thread. It repeatedly checks the server for commands and executes
	 * them, then waits for a specified delay.
	 */
	@Override
	public void run() {
		while (true) {
			System.out.println("server timer running");
			// check relevant commands
			List<Message> list = server.getCommands();

			// execute one by one
			if (list != null)
				for (Message msg : list) {
					server.execute(msg);
				}
			// sleep for delayMinutes minutes
			try {
				Thread.sleep(delayMinutes * 60 * 1000); // sleep
			} catch (InterruptedException e) {
			}
		}
	}

}
