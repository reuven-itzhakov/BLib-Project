package logic;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents an activity associated with a subscriber.
 * This class stores details such as the activity ID, type, description, and date.
 * Implements {@link Serializable} to allow instances of this class to be serialized.
 */
public class Activity implements Serializable {

    // Private member variables to store the activity details
    private int id; // Unique ID of the subscriber associated with the activity
    private String type; // Type of the activity (e.g., "Borrow", "Order", etc.)
    private String description; // A detailed description of the activity
    private LocalDate date; // The date the activity occurred

    /**
     * Constructs an Activity instance with the specified details.
     *
     * @param id          The unique ID of the subscriber associated with the activity.
     * @param type        The type of the activity (e.g., "Borrow", "Order").
     * @param description A detailed description of the activity.
     * @param date        The date the activity occurred.
     */
    public Activity(int id, String type, String description, LocalDate date) {
        this.id = id; // Initialize the activity ID
        this.type = type; // Initialize the activity type
        this.description = description; // Initialize the activity description
        this.date = date; // Initialize the activity date
    }

    /**
     * Returns the unique ID of the subscriber associated with this activity.
     *
     * @return The activity ID.
     */
    public int getId() {
        return id; // Return the activity ID
    }

    /**
     * Sets the unique ID of the subscriber associated with this activity.
     *
     * @param id The new activity ID.
     */
    public void setId(int id) {
        this.id = id; // Update the activity ID
    }

    /**
     * Returns the type of the activity.
     *
     * @return The activity type (e.g., "Meeting", "Task").
     */
    public String getType() {
        return type; // Return the activity type
    }

    /**
     * Sets the type of the activity.
     *
     * @param type The new activity type.
     */
    public void setType(String type) {
        this.type = type; // Update the activity type
    }

    /**
     * Returns the description of the activity.
     *
     * @return The activity description.
     */
    public String getDescription() {
        return description; // Return the activity description
    }

    /**
     * Sets the description of the activity.
     *
     * @param description The new activity description.
     */
    public void setDescription(String description) {
        this.description = description; // Update the activity description
    }

    /**
     * Returns the date the activity occurred.
     *
     * @return The activity date.
     */
    public LocalDate getDate() {
        return date; // Return the activity date
    }

    /**
     * Sets the date the activity occurred.
     *
     * @param date The new activity date.
     */
    public void setDate(LocalDate date) {
        this.date = date; // Update the activity date
    }
}
