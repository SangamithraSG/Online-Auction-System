package com.auction.javafx;

import com.auction.enums.ItemCategory;
import com.auction.managers.AuctionManager;
import com.auction.models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Window for creating a new auction
 */
public class CreateAuctionWindow {
    
    private Stage stage;
    private User currentUser;
    private AuctionManager manager;
    
    public CreateAuctionWindow(User currentUser, AuctionManager manager) {
        this.currentUser = currentUser;
        this.manager = manager;
        this.stage = new Stage();
    }
    
    public void showAndWait() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Create New Auction");
        stage.setWidth(550);
        stage.setHeight(600);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        header.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Create New Auction");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label subtitleLabel = new Label("Fill in the details below");
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(header);
        
        // Form
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: white;");
        
        // Title field
        Label titleFieldLabel = new Label("Auction Title *");
        titleFieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextField titleField = new TextField();
        titleField.setPromptText("Enter auction title");
        titleField.setStyle("-fx-font-size: 14px;");
        
        // Description field
        Label descLabel = new Label("Description *");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter detailed description");
        descArea.setPrefRowCount(4);
        descArea.setWrapText(true);
        
        // Starting Price
        Label startPriceLabel = new Label("Starting Price ($) *");
        startPriceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextField startPriceField = new TextField();
        startPriceField.setPromptText("e.g., 100.00");
        
        // Reserve Price
        Label reservePriceLabel = new Label("Reserve Price ($) *");
        reservePriceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextField reservePriceField = new TextField();
        reservePriceField.setPromptText("e.g., 150.00");
        
        Label reserveNote = new Label("(Minimum price to sell the item)");
        reserveNote.setFont(Font.font("Arial", 10));
        reserveNote.setTextFill(Color.GRAY);
        
        // Duration
        Label durationLabel = new Label("Duration (minutes) *");
        durationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        HBox durationBox = new HBox(10);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField durationField = new TextField();
        durationField.setPromptText("e.g., 60");
        durationField.setPrefWidth(150);
        
        // Quick duration buttons
        Button dur15 = new Button("15 min");
        dur15.setOnAction(e -> durationField.setText("15"));
        
        Button dur30 = new Button("30 min");
        dur30.setOnAction(e -> durationField.setText("30"));
        
        Button dur60 = new Button("1 hour");
        dur60.setOnAction(e -> durationField.setText("60"));
        
        durationBox.getChildren().addAll(durationField, dur15, dur30, dur60);
        
        // Category
        Label categoryLabel = new Label("Category *");
        categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        ComboBox<ItemCategory> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(ItemCategory.values());
        categoryCombo.setPromptText("Select category");
        categoryCombo.setPrefWidth(300);
        
        // Error message label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        
        form.getChildren().addAll(
            titleFieldLabel, titleField,
            descLabel, descArea,
            startPriceLabel, startPriceField,
            reservePriceLabel, reservePriceField, reserveNote,
            durationLabel, durationBox,
            categoryLabel, categoryCombo,
            errorLabel
        );
        
        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        root.setCenter(scrollPane);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(15));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-background-color: #f5f5f5;");
        
        Button createButton = new Button("Create Auction");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 30px;");
        createButton.setOnAction(e -> {
            if (validateAndCreate(titleField, descArea, startPriceField, 
                                 reservePriceField, durationField, categoryCombo, errorLabel)) {
                stage.close();
            }
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");
        cancelButton.setOnAction(e -> stage.close());
        
        buttonBox.getChildren().addAll(createButton, cancelButton);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    private boolean validateAndCreate(TextField titleField, TextArea descArea,
                                     TextField startPriceField, TextField reservePriceField,
                                     TextField durationField, ComboBox<ItemCategory> categoryCombo,
                                     Label errorLabel) {
        
        // Clear previous error
        errorLabel.setText("");
        
        // Validate title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("❌ Please enter an auction title");
            return false;
        }
        
        // Validate description
        String description = descArea.getText().trim();
        if (description.isEmpty()) {
            errorLabel.setText("❌ Please enter a description");
            return false;
        }
        
        // Validate starting price
        double startPrice;
        try {
            startPrice = Double.parseDouble(startPriceField.getText().trim());
            if (startPrice <= 0) {
                errorLabel.setText("❌ Starting price must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("❌ Please enter a valid starting price");
            return false;
        }
        
        // Validate reserve price
        double reservePrice;
        try {
            reservePrice = Double.parseDouble(reservePriceField.getText().trim());
            if (reservePrice <= 0) {
                errorLabel.setText("❌ Reserve price must be greater than 0");
                return false;
            }
            if (reservePrice < startPrice) {
                errorLabel.setText("❌ Reserve price must be greater than or equal to starting price");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("❌ Please enter a valid reserve price");
            return false;
        }
        
        // Validate duration
        int duration;
        try {
            duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                errorLabel.setText("❌ Duration must be greater than 0");
                return false;
            }
            if (duration > 10080) { // 1 week in minutes
                errorLabel.setText("❌ Duration cannot exceed 1 week (10080 minutes)");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("❌ Please enter a valid duration in minutes");
            return false;
        }
        
        // Validate category
        ItemCategory category = categoryCombo.getValue();
        if (category == null) {
            errorLabel.setText("❌ Please select a category");
            return false;
        }
        
        // Create the auction
        String auctionId = manager.createAuction(title, description, startPrice,
                                                reservePrice, currentUser, category, duration);
        
        if (auctionId != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success!");
            alert.setHeaderText("Auction Created Successfully");
            alert.setContentText("Your auction '" + title + "' has been created and is now live!\n\n" +
                               "Auction ID: " + auctionId.substring(0, 8) + "\n" +
                               "Duration: " + duration + " minutes");
            alert.showAndWait();
            return true;
        } else {
            errorLabel.setText("❌ Failed to create auction. Please try again.");
            return false;
        }
    }
}
