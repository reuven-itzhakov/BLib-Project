package logic;

import java.io.Serializable;

/**
 * Represents a book title with details such as title ID, name, author, 
 * description, number of orders, number of copies, and genre.
 * This class is used to encapsulate the attributes of a book title.
 */
public class BookTitle implements Serializable {
    private int titleID; // Unique identifier for the book title
    private String titleName; // Name of the book title
    private String authorName; // Name of the author of the book
    private String description; // Description of the book
    private int numOfOrders; // Number of orders for this book title
    private int numOfCopies; // Number of copies available for this book title
    private String genre; // Genre of the book

    /**
     * Constructs a new BookTitle object with the specified details.
     * 
     * @param titleID    the unique ID of the book title
     * @param titleName  the name of the book title
     * @param authorName the name of the book's author
     * @param description a brief description of the book
     * @param numOfOrders the number of orders for this book title
     * @param numOfCopies the number of copies available for this book
     * @param genre      the genre of the book
     */
    public BookTitle(int titleID, String titleName, String authorName, String description, int numOfOrders,
            int numOfCopies, String genre) {
        this.titleID = titleID;
        this.titleName = titleName;
        this.authorName = authorName;
        this.description = description;
        this.numOfOrders = numOfOrders;
        this.numOfCopies = numOfCopies;
        this.genre = genre;
    }

    /**
     * Returns the genre of the book title.
     * 
     * @return the genre of the book title
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Sets the genre of the book title.
     * 
     * @param genre the new genre of the book title
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Returns the unique ID of the book title.
     * 
     * @return the title ID
     */
    public int getTitleID() {
        return titleID;
    }

    /**
     * Returns the name of the book title.
     * 
     * @return the title name
     */
    public String getTitleName() {
        return titleName;
    }

    /**
     * Returns the name of the author of the book.
     * 
     * @return the author's name
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Returns a brief description of the book.
     * 
     * @return the book description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the number of orders for this book title.
     * 
     * @return the number of orders
     */
    public int getNumOfOrders() {
        return numOfOrders;
    }

    /**
     * Returns the number of copies available for this book title.
     * 
     * @return the number of copies
     */
    public int getNumOfCopies() {
        return numOfCopies;
    }

    /**
     * Compares this BookTitle with another object for equality based on the title ID.
     * 
     * @param obj the object to compare with
     * @return true if the object is a BookTitle with the same title ID, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookTitle)
            return ((BookTitle) obj).titleID == this.titleID;
        return false;
    }

    /**
     * Returns a string representation of the book title in the format "Author : Title".
     * 
     * @return the string representation of the book title
     */
    @Override
    public String toString() {
        return authorName + " : " + titleName;
    }
}
