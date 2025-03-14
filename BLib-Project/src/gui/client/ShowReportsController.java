package gui.client;

import java.io.ByteArrayInputStream;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * The ShowReportsController class handles displaying reports such as graphs.
 */
public class ShowReportsController {

	@FXML
	private Button btnBack = null; // Button for going back to the previous screen
	@FXML
	private Label lblTitle; // Label for the title of the report
	@FXML
	private ImageView imgGraph; // ImageView to display the graph
	@FXML
	private HBox hboxCenter; // HBox layout for centering the graph
	@FXML
	private VBox vboxCenter; // VBox layout for the main container
	
	/**
	 * Loads the report details into the title.
	 * 
	 * @param name  The name of the report (e.g., borrowing report)
	 * @param year  The year of the report
	 * @param month The month of the report
	 */
	public void loadGraphDetails(String name, int year, int month) {
		lblTitle.setText(name + " " + month + "/" + year);
	}

	/**
	 * Loads the graph image into the ImageView.
	 * 
	 * @param image The byte array representing the graph image.
	 */
	public void loadGraph(byte[] image) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(image);
		Image fxImage = new Image(byteStream);
		imgGraph.setImage(fxImage);
		imgGraph.fitWidthProperty().bind(hboxCenter.widthProperty());
		imgGraph.fitHeightProperty().bind(hboxCenter.heightProperty());
		imgGraph.setPreserveRatio(true);
		imgGraph.setSmooth(true);
	    hboxCenter.prefWidthProperty().bind(vboxCenter.widthProperty());
	    hboxCenter.prefHeightProperty().bind(vboxCenter.heightProperty().subtract(100)); // Prevents the image from being too small
	}

	/**
	 * Handles the "Back" button action. Closes the current window.
	 * 
	 * @param event The action event triggered by clicking the button.
	 */
	public void backBtn(ActionEvent event){
		// Close the current window (hide it)
		((Node) event.getSource()).getScene().getWindow().hide();
	}
}
