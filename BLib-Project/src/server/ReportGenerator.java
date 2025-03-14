package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ReportGenerator class is responsible for generating reports based on
 * subscriber status and borrow time data, and returns the generated reports as
 * byte arrays.
 */
public class ReportGenerator {
	private static LocalDate date;
	private static byte[] data;
	private DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM");

	/**
	 * Generates a subscriber status report for the given date and returns it as a
	 * byte array.
	 *
	 * @param date the date for which the report is generated
	 * @return a byte array representing the generated subscriber status report
	 */
	public byte[] generateSubscriberStatusReport(LocalDate date) {
		ReportGenerator.date = date;
		data = null;
		subscriberStatus(ServerGUI.primaryStage); // Call method to generate subscriber status report
		// Wait for the report to be generated before returning the data
		while (data == null)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		return data;
	}

	/**
	 * Generates a borrow time report for the given date and returns it as a byte
	 * array.
	 *
	 * @param date the date for which the report is generated
	 * @return a byte array representing the generated borrow time report
	 */
	public byte[] generateBorrowTimeReport(LocalDate date) {
		ReportGenerator.date = date;
		data = null;
		borrowTime(ServerGUI.primaryStage); // Call method to generate borrow time report
		// Wait for the report to be generated before returning the data
		while (data == null)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		return data;
	}

	/**
	 * Generates a report of subscriber status (active vs frozen) over time and
	 * displays it as a bar chart. The chart is then saved as a PNG image byte
	 * array.
	 *
	 * @param primaryStage the main stage for the application
	 */
	@SuppressWarnings("unchecked")
	public void subscriberStatus(Stage primaryStage) {
		// Setup for the X and Y axes of the bar chart
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();

		xAxis.setLabel("Date");
		yAxis.setLabel("Number of Members");

		// Create the bar chart for subscriber status
		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setTitle("Frozen and Active Members Over Time");

		// Create series for frozen and active members
		XYChart.Series<String, Number> frozenSeries = new XYChart.Series<>();
		frozenSeries.setName("Frozen");
		XYChart.Series<String, Number> activeSeries = new XYChart.Series<>();
		activeSeries.setName("Active");

		// Initialize the date to the first day of the month
		date = LocalDate.of(date.getYear(), date.getMonth(), 1);

		// Get the subscriber status data from the server
		Map<LocalDate, Integer[]> map = null;
		map = ServerGUI.server.getSubscribersStatusOnMonth(date);

		// TreeSet to store dates for which we have data, sorted in ascending order
		TreeSet<LocalDate> dates = new TreeSet<>();
		dates.addAll(map.keySet());

		// Last known status of the subscribers
		Integer[] lastKnownStatus = { 0, 0 }; // To track the last known status
		Month curMonth = date.getMonth();

		// Iterate through each day of the month
		while (date.getMonth().equals(curMonth)) {
			LocalDate min = dates.isEmpty() ? null : dates.first(); // Get the earliest date from the TreeSet
			if (min != null && min.compareTo(date) <= 0) {
				// Found data for this date
				Integer[] status = map.get(min); // Get the status for the current date

				if (status != null) {
					// Update last known status
					lastKnownStatus = status;

					// Add the new data to the chart
					frozenSeries.getData().add(new XYChart.Data<>(date.format(f), status[1])); // Frozen members
					activeSeries.getData().add(new XYChart.Data<>(date.format(f), status[0])); // Active members
				}

				// Remove the processed date from the set
				dates.remove(min);
			} else {

				// If no data for this date, use the last known status
				frozenSeries.getData().add(new XYChart.Data<>(date.format(f), lastKnownStatus[1]));
				activeSeries.getData().add(new XYChart.Data<>(date.format(f), lastKnownStatus[0]));

			}

			// Move to the next day
			date = date.plusDays(1);
		}

		// Add the series to the chart
		barChart.getData().addAll(activeSeries, frozenSeries);

		date = date.minusMonths(1);
		// Calculate the total new subscribers for the current month
		int sum = ServerGUI.server.SumNewSubscriber(date);
		// Label showing the total new subscribers
		Label label = new Label("The total new subscribers this month is :%d".formatted(sum));
		label.setFont(new Font("Arial", 24)); // Set font and size
		label.setAlignment(Pos.CENTER);

		// Configure the layout of the chart and label
		VBox.setVgrow(barChart, Priority.ALWAYS);

		VBox vbox = new VBox(barChart, label);
		vbox.setPrefSize(800, 600);
		vbox.setAlignment(Pos.BOTTOM_CENTER); // Center the label at the bottom
		vbox.setSpacing(20);
		vbox.setPadding(new Insets(10, 20, 50, 20));

		// Create the scene for the chart and label
		Scene scene = new Scene(vbox, 1400, 800);

		// Set some styling for the chart
		barChart.setCategoryGap(2);
		barChart.setBarGap(5);

		// Timeline to capture the scene as an image after 2 seconds
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
			// Capture the chart as an image
			WritableImage image = scene.snapshot(null);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			try {
				// Write the image to the output stream as PNG
				System.out.println(ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", out));
				data = out.toByteArray(); // Store the image data as byte array

				System.out.println("subscriber status graph generated");
				// Platform.exit();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}));
		timeline.setCycleCount(1);
		timeline.play();

	}

	/**
	 * Generates a report of borrow time statistics for different genres and
	 * displays it as a bar chart. The chart is then saved as a PNG image byte
	 * array.
	 *
	 * @param primaryStage the main stage for the application
	 */
	@SuppressWarnings("unchecked")
	public void borrowTime(Stage primaryStage) {
		// Setup for the X and Y axes of the bar chart
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();

		xAxis.setLabel("Genre");
		yAxis.setLabel("Value");

		// Create the bar chart for borrow time statistics
		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setTitle("Borrow Statistics per Genre");

		// Create series for borrow duration and late returns
		XYChart.Series<String, Number> borrowDuration = new XYChart.Series<>();
		borrowDuration.setName("Average Borrow Duration (Days)");
		XYChart.Series<String, Number> lateReturns = new XYChart.Series<>();
		lateReturns.setName("Late Returns (%)");

		date = LocalDate.of(date.getYear(), date.getMonth(), 1);

		// Get the borrow time statistics from the server
		Map<String, Double[]> map = ServerGUI.server.getBorrowTimeOnMonth(date);

		// Add data for each genre to the chart
		for (String genre : map.keySet()) {
			Double[] status = map.get(genre);
			borrowDuration.getData().add(new XYChart.Data<>(genre, status[0]));
			lateReturns.getData().add(new XYChart.Data<>(genre, status[1]));

		}

		// Add the series to the chart
		barChart.getData().addAll(lateReturns, borrowDuration);

		// Calculate the average borrow time for the current month
		double avg = ServerGUI.server.getAvgBorrowTimeOnMonth(date);

		// Label showing the average borrow time
		Label label = new Label("The average borrow duration this month is :%.2f days".formatted(avg));
		label.setFont(new Font("Arial", 24)); // Set font and size
		label.setAlignment(Pos.CENTER);

		// Configure the layout of the chart and label
		VBox.setVgrow(barChart, Priority.ALWAYS);

		VBox vbox = new VBox(barChart, label);
		vbox.setPrefSize(800, 600);
		vbox.setAlignment(Pos.BOTTOM_CENTER); // Center the label at the bottom
		vbox.setSpacing(20);
		vbox.setPadding(new Insets(10, 20, 50, 20));

		// Create the scene for the chart and label
		Scene scene = new Scene(vbox, 1400, 800);

		// Set some styling for the chart
		barChart.setCategoryGap(20);
		barChart.setBarGap(5);

		// Timeline to capture the scene as an image after 2 seconds
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
			// Capture the chart as an image
			WritableImage image = scene.snapshot(null);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			try {
				// Write the image to the output stream as PNG
				System.out.println(ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", out));
				data = out.toByteArray(); // Store the image data as byte array

				System.out.println("borrow time graph generated");

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}));
		timeline.setCycleCount(1);
		timeline.play();

	}

}
