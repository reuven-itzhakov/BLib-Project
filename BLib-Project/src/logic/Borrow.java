package logic;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a borrowing transaction of a book by a subscriber.
 * This class stores details about the subscriber, the book copy, 
 * and the borrowing and return dates.
 * Implements {@link Serializable} to allow serialization of instances.
 */
public class Borrow implements Serializable {

    // Private member variables for storing borrowing details
    protected Subscriber subscriber; // The subscriber who borrowed the book
    protected BookCopy book; // The borrowed book copy
    protected LocalDate dateOfBorrow; // The date when the book was borrowed
    protected LocalDate dueDate; // The due date for returning the book
    protected LocalDate dateOfReturn; // The actual date when the book was returned

    /**
     * Constructs a Borrow instance with all necessary details.
     *
     * @param subscriber   The subscriber who borrowed the book.
     * @param book         The borrowed book copy.
     * @param dateOfBorrow The date when the book was borrowed.
     * @param dueDate      The due date for returning the book.
     * @param dateOfReturn The actual return date of the book (can be null if not returned yet).
     */
    public Borrow(Subscriber subscriber, BookCopy book, LocalDate dateOfBorrow, LocalDate dueDate, LocalDate dateOfReturn) {
        this.subscriber = subscriber;
        this.book = book;
        this.dateOfBorrow = dateOfBorrow; // Set the date of borrow
        this.dueDate = dueDate; // Set the due date
        this.dateOfReturn = dateOfReturn; // Set the return date
    }

    /**
     * Returns the date when the book was borrowed.
     *
     * @return The borrowing date.
     */
    public LocalDate getDateOfBorrow() {
        return dateOfBorrow;
    }

    /**
     * Returns the due date for returning the book.
     *
     * @return The due date.
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Returns the actual date when the book was returned.
     *
     * @return The return date.
     */
    public LocalDate getDateOfReturn() {
        return dateOfReturn;
    }

    /**
     * Sets the date when the book was borrowed.
     *
     * @param dateOfBorrow The borrowing date to set.
     */
    public void setDateOfBorrow(LocalDate dateOfBorrow) {
        this.dateOfBorrow = dateOfBorrow;
    }

    /**
     * Sets the due date for returning the book.
     *
     * @param dueDate The due date to set.
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Sets the actual date when the book was returned.
     *
     * @param dateOfReturn The return date to set.
     */
    public void setDateOfReturn(LocalDate dateOfReturn) {
        this.dateOfReturn = dateOfReturn;
    }

    /**
     * Returns the subscriber who borrowed the book.
     *
     * @return The {@link Subscriber} object associated with this borrowing transaction.
     */
    public Subscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Returns the book copy that was borrowed.
     *
     * @return The {@link BookCopy} object associated with this borrowing transaction.
     */
    public BookCopy getBook() {
        return book;
    }

    /**
     * Returns the copy ID of the borrowed book as a string.
     *
     * @return The copy ID.
     */
    public String getCopyId() {
        return String.valueOf(book.getCopyID());
    }

    /**
     * Returns the title name of the borrowed book.
     *
     * @return The title name.
     */
    public String getTitleName() {
        return book.getTitle().getTitleName();
    }

    /**
     * Returns the author name of the borrowed book.
     *
     * @return The author name.
     */
    public String getAuthor() {
        return book.getTitle().getAuthorName();
    }
}
