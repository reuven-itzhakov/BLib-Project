package logic;

import java.io.Serializable;

/**
 * The Subscriber class represents a subscriber in the system. It contains
 * information about the subscriber's unique identifier, name, phone number,
 * email address, and status. This class implements {@link Serializable} to
 * allow the object to be serialized for network transmission or storage.
 */
public class Subscriber implements Serializable {

    private int id; // The unique identifier for the subscriber
    private String name; // The name of the subscriber
    private String phone; // The phone number of the subscriber
    private String email; // The email address of the subscriber
    private String status; // The status of the subscriber (e.g., "active", "inactive")

    /**
     * Constructs a Subscriber object with the given id, name, phone, and email.
     * The status is set to "active" by default.
     *
     * @param id    the unique identifier for the subscriber
     * @param name  the name of the subscriber
     * @param phone the phone number of the subscriber
     * @param email the email address of the subscriber
     */
    public Subscriber(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = "active"; // Default status is "active"
    }

    /**
     * Constructs a Subscriber object with the given id, name, phone, email, and
     * status.
     *
     * @param id     the unique identifier for the subscriber
     * @param name   the name of the subscriber
     * @param phone  the phone number of the subscriber
     * @param email  the email address of the subscriber
     * @param status the status of the subscriber (e.g., "active", "inactive")
     */
    public Subscriber(int id, String name, String phone, String email, String status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = status; // Status is set to the provided value
    }

    /**
     * Gets the id of the subscriber.
     *
     * @return the unique identifier of the subscriber
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the subscriber.
     *
     * @param id the unique identifier to be set for the subscriber
     */
    public void setId(int id) {
        this.id = id; // Set the subscriber's id
    }

    /**
     * Gets the name of the subscriber.
     *
     * @return the name of the subscriber
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the subscriber.
     *
     * @param name the name to be set for the subscriber
     */
    public void setName(String name) {
        this.name = name; // Set the subscriber's name
    }

    /**
     * Gets the phone number of the subscriber.
     *
     * @return the phone number of the subscriber
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the subscriber.
     *
     * @param phone the phone number to be set for the subscriber
     */
    public void setPhone(String phone) {
        this.phone = phone; // Set the subscriber's phone number
    }

    /**
     * Gets the email address of the subscriber.
     *
     * @return the email address of the subscriber
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the subscriber.
     *
     * @param email the email address to be set for the subscriber
     */
    public void setEmail(String email) {
        this.email = email; // Set the subscriber's email address
    }

    /**
     * Gets the status of the subscriber.
     *
     * @return the status of the subscriber (e.g., "active", "inactive")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the subscriber.
     *
     * @param status the status to be set for the subscriber
     */
    public void setStatus(String status) {
        this.status = status; // Set the subscriber's status
    }
}
