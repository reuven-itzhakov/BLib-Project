

package logic;

import java.io.Serializable;
import java.time.LocalDate;

import gui.client.IPController;

/**
 * Represents a single copy of a book in the library.
 * This class stores details about the book copy, including its title, copy ID, 
 * shelf location, and borrowing status.
 * Implements {@link Serializable} to allow serialization of instances.
 */
public class BookCopy implements Serializable {

    // Private member variables for storing book details
    private BookTitle title; // The title details of the book
    private int copyID; // Unique ID for this book copy
    private String shelf; // Shelf location of the book
    private boolean isBorrowed; // Indicates whether the book is currently borrowed or not

    /**
     * Constructs a BookCopy instance with all necessary details.
     *
     * @param title      The title of the book.
     * @param copyID     The unique ID of this book copy.
     * @param shelf      The shelf location of the book.
     * @param isBorrowed The borrowing status of the book (true if borrowed, false otherwise).
     */
    public BookCopy(BookTitle title, int copyID, String shelf, boolean isBorrowed) {
        this.title = title; // Set the title of the book
        this.copyID = copyID; // Set the copy ID
        this.shelf = shelf; // Set the shelf location
        this.isBorrowed = isBorrowed; // Set the borrowing status
    }

    /**
     * Returns the unique ID of this book copy.
     *
     * @return The copy ID.
     */
    public int getCopyID() {
        return copyID;
    }

    /**
     * Returns the shelf location of the book.
     * If the book is currently borrowed, the shelf location is represented as "-".
     *
     * @return The shelf location or "-" if the book is borrowed.
     */
    public String getShelf() {
        if (isBorrowed) {
            return "-";
        }
        return shelf;
    }

    /**
     * Returns the title details of the book.
     *
     * @return The {@link BookTitle} object associated with this book copy.
     */
    public BookTitle getTitle() {
        return title;
    }

    /**
     * Checks if the book is currently borrowed.
     *
     * @return True if the book is borrowed, false otherwise.
     */
    public Boolean isBorrowed() {
        return isBorrowed;
    }

    /**
     * Sets the borrowing status of the book.
     *
     * @param isBorrowed True if the book is borrowed, false otherwise.
     */
    public void setBorrowed(boolean isBorrowed) {
        this.isBorrowed = isBorrowed;
    }

    /**
     * Returns the availability status of the book as a string.
     *
     * @return "Not available" if the book is borrowed, otherwise "Available".
     */
    public String isAvailable() {
        return isBorrowed ? "Not available" : "Available";
    }
    
    /*
     * The following commented methods could be used for additional functionality
     * if implemented properly:
     * 
     * - getDueDate: Fetch and return the due date if the book is borrowed.
     */
    

	//	public String getDueDate() {
	//		// ruben this is not effective
	//		if (isBorrowed) {
	//			return IPController.client.getCopyActiveBorrow(this).getDueDate().toString();
	//		}
	//		return "-";
	//	}
}


