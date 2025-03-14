package logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The Order class represents an order in the system, containing information
 * about the order's unique identifier, the subscriber who placed the order,
 * the book title, the specific book copy, and the order and arrival dates.
 * This class implements {@link Serializable} to allow the object to be 
 * serialized for storage or transmission.
 */
public class Order implements Serializable {

    private int id; // The unique identifier for the order
    private Subscriber subscriber; // The subscriber who placed the order
    private BookTitle title; // The title of the book being ordered
    private BookCopy copy; // The specific copy of the book (null if not assigned yet)
    private LocalDateTime order_date; // The date and time when the order was placed
    private LocalDate arive_date; // The date when the order arrived (null if not set)

    /**
     * Constructs a new Order with the given parameters.
     *
     * @param id the unique identifier for the order
     * @param subscriber the subscriber who placed the order
     * @param title the title of the book being ordered
     * @param order_date the date and time when the order was placed
     */
    public Order(int id, Subscriber subscriber, BookTitle title, LocalDateTime order_date) {
        this.id = id;
        this.subscriber = subscriber;
        this.title = title;
        this.copy = null; // Initially, there is no book copy assigned to the order
        this.order_date = order_date;
        this.arive_date = null; // Initially, the arrival date is not set
    }

    /**
     * Attempts to assign a book copy to the order. The copy must match the title
     * of the ordered book.
     *
     * @param copy the book copy to assign to the order
     * @return true if the copy was successfully assigned, false otherwise
     */
    public boolean setCopy(BookCopy copy) {
        if (copy.getTitle().equals(title)) { // Ensure the copy matches the title of the order
            this.copy = copy;
            return true;
        }
        return false; // Return false if the copy does not match the title
    }

    /**
     * Sets the arrival date for the order.
     *
     * @param arive_date the date when the order arrived
     */
    public void setAriveDate(LocalDate arive_date) {
        this.arive_date = arive_date; // Set the arrival date of the order
    }

    /**
     * Sets the unique identifier for the order.
     *
     * @param id the unique identifier to be set for the order
     */
    public void setId(int id) {
        this.id = id; // Set the id of the order
    }

    /**
     * Gets the unique identifier of the order.
     *
     * @return the unique identifier of the order
     */
    public int getId() {
        return this.id; // Return the id of the order
    }

    /**
     * Gets the subscriber who placed the order.
     *
     * @return the subscriber associated with this order
     */
    public Subscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Gets the title of the book being ordered.
     *
     * @return the title of the ordered book
     */
    public BookTitle getTitle() {
        return title;
    }

    /**
     * Gets the date and time when the order was placed.
     *
     * @return the order's placement date and time
     */
    public LocalDateTime getOrder_date() {
        return order_date;
    }

    /**
     * Gets the arrival date of the order.
     *
     * @return the arrival date of the order, or null if not set
     */
    public LocalDate getArive_date() {
        return arive_date;
    }
    /**
     * Gets the assigned copy of the order.
     *
     * @return  the assigned copy, or null if not yet set 
     */
	public BookCopy getCopy() {
		return copy;
	}
    
}
