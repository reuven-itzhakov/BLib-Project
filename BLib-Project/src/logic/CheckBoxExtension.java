package logic;

import java.io.Serializable;

import javafx.scene.control.CheckBox;

/**
 * Represents an enhanced version of a borrowing entity, 
 * including additional UI-specific properties such as a {@link CheckBox} 
 * and an error message for validation or status purposes.
 * Implements {@link Serializable} to allow serialization of instances.
 */
public class CheckBoxExtension implements Serializable {

    // Private member variables for storing borrowing details
    private CheckBox checkBox; // A CheckBox component for UI interaction
    private String errorMessage; // A string to store an error or status message

    /**
     * Default constructor to initialize a BorrowPlus object with default values.
     * The {@link CheckBox} is initialized as unselected, and the error message is set to an empty string.
     */
    public CheckBoxExtension() {
        checkBox = new CheckBox(); // Initialize the CheckBox
        checkBox.setSelected(false); // Set the CheckBox as unselected
        errorMessage = ""; // Initialize the error message as an empty string
    }

    /**
     * Returns the {@link CheckBox} associated with this object.
     *
     * @return The CheckBox instance.
     */
    public CheckBox getCheckBox() {
        return checkBox;
    }

    /**
     * Sets the state of the {@link CheckBox}.
     *
     * @param state A boolean value representing whether the CheckBox should be selected or not.
     */
    public void setCheckBox(boolean state) {
        checkBox.setSelected(state); // Set the CheckBox's selected state
    }

    /**
     * Returns the error message associated with this object.
     *
     * @return The error message as a string.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message for this object.
     *
     * @param errorMessage A string containing the error or status message to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage; // Set the error message
    }
}
