package logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a message object used for communication between the client and server. 
 * The message contains a command and its associated arguments, enabling structured 
 * data transfer over the network. Implements {@link Serializable} for object serialization.
 */
public class Message implements Serializable {

    private String command; // The command representing the action or operation
    private List<Object> arguments; // A list of arguments associated with the command

    /**
     * Constructs a {@code Message} object with a specified command and an optional 
     * list of arguments.
     * 
     * @param command the command string representing the action or operation to perform
     * @param args    optional arguments associated with the command
     */
    public Message(String command, Object... args) {
        this.command = command; // Initialize the command
        arguments = new ArrayList<>(); // Initialize the arguments list
        // Populate the arguments list with the provided arguments
        for (Object o : args) {
            arguments.add(o);
        }
    }

    /**
     * Retrieves the command of this message.
     * 
     * @return the command string
     */
    public String getCommand() {
        return command; // Return the command
    }

    /**
     * Retrieves the list of arguments associated with this message.
     * 
     * @return a {@link List} containing the arguments
     */
    public List<Object> getArguments() {
        return arguments; // Return the list of arguments
    }

    /**
     * Adds an argument to the existing list of arguments.
     * 
     * @param o the argument to be added
     */
    public void addArgument(Object o) {
        arguments.add(o); // Add the argument to the list
    }

    /**
     * Returns a string representation of the {@code Message} object, including the 
     * command and its associated arguments.
     * 
     * @return a string representation of the message
     */
    @Override
    public String toString() {
        return command + Arrays.deepToString(arguments.toArray()); // Convert to a readable format
    }
}
