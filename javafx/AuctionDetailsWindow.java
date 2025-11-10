package com.auction.javafx;

import com.auction.enums.AuctionState;
import com.auction.managers.AuctionManager;
import com.auction.models.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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

import java.time.format.DateTimeFormatter;

/**
 * Window to display auction details and allow bidding
 */
public class AuctionDetailsWindow {
    
    private Stage stage;
    private AuctionItem item;
    private User currentUser;
    private AuctionManager manager;
    private Label currentBidLabel;
    private Label timeRemainingLabel;
    private Label statusLabel;
    private ListView<String> bidHistoryList;
    private AnimationTimer updateTimer;
    
    public AuctionDetailsWindow(AuctionItem item, User currentUser, AuctionManager manager) {
        this.item = item;
        this.currentUser = currentUser;
        this.manager = manager;
        this.stage = new Stage();
    }
    
    public void show() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Auction Details - " + item.getTitle());
        stage.setWidth(700);
        stage.setHeight(650);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label categoryLabel = new Label("Category: " + item.getCategory());
        categoryLabel.setTextFill(Color.LIGHTGRAY);
        categoryLabel.setFont(Font.font("Arial", 14));
        
        header.getChildren().addAll(titleLabel, categoryLabel);
        root.setTop(header);
        
        // Center - Details
        VBox center = new VBox(15);
        center.setPadding(new Insets(20));
        
        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        TextArea descArea = new TextArea(item.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-control-inner-background: white;");
        
        // Info Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10));
        infoGrid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        int row = 0;
        addInfoRow(infoGrid, row++, "Seller:", item.getSellerUsername());
        addInfoRow(infoGrid, row++, "Starting Price:", String.format("$%.2f", item.getStartingPrice()));
        addInfoRow(infoGrid, row++, "Reserve Price:", String.format("$%.2f", item.getReservePrice()));
        
        currentBidLabel = new Label(String.format("$%.2f", item.getCurrentBid()));
        currentBidLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        currentBidLabel.setTextFill(Color.GREEN);
        addInfoRowWithControl(infoGrid, row++, "Current Bid:", currentBidLabel);
        
        addInfoRow(infoGrid, row++, "Minimum Next Bid:", 
            String.format("$%.2f", item.getCurrentBid() + item.getMinimumBidIncrement()));
        
        timeRemainingLabel = new Label(formatTimeRemaining());
        timeRemainingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        timeRemainingLabel.setTextFill(Color.DARKORANGE);
        addInfoRowWithControl(infoGrid, row++, "Time Remaining:", timeRemainingLabel);
        
        statusLabel = new Label(item.isReserveMet() ? "✅ Reserve Met" : "⚠️ Reserve Not Met");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        addInfoRowWithControl(infoGrid, row++, "Status:", statusLabel);
        
        // Bid History
        Label historyLabel = new Label("Bid History (" + item.getBidHistory().size() + " bids):");
        historyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        bidHistoryList = new ListView<>();
        bidHistoryList.setPrefHeight(150);
        bidHistoryList.setStyle("-fx-control-inner-background: white;");
        updateBidHistory();
        
        center.getChildren().addAll(descLabel, descArea, infoGrid, historyLabel, bidHistoryList);
        root.setCenter(center);
        
        // Bottom - Bid Section
        if (item.getState() == AuctionState.ACTIVE && 
            !item.getSellerId().equals(currentUser.getUserId())) {
            
            VBox bidSection = new VBox(10);
            bidSection.setPadding(new Insets(20));
            bidSection.setStyle("-fx-background-color: #e8f5e9;");
            
            Label bidLabel = new Label("Place Your Bid:");
            bidLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            
            HBox bidBox = new HBox(10);
            bidBox.setAlignment(Pos.CENTER_LEFT);
            
            TextField bidField = new TextField();
            bidField.setPromptText("Enter bid amount");
            bidField.setPrefWidth(150);
            
            Button bidButton = new Button("Place Bid");
            bidButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            bidButton.setOnAction(e -> placeBid(bidField));
            
            Button quickBidBtn = new Button("Quick Bid (Min)");
            quickBidBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            quickBidBtn.setOnAction(e -> {
                double minBid = item.getCurrentBid() + item.getMinimumBidIncrement();
                bidField.setText(String.format("%.2f", minBid));
            });
            
            bidBox.getChildren().addAll(new Label("$"), bidField, bidButton, quickBidBtn);
            
            bidSection.getChildren().addAll(bidLabel, bidBox);
            root.setBottom(bidSection);
        } else if (item.getSellerId().equals(currentUser.getUserId())) {
            Label sellerNote = new Label("You cannot bid on your own auction");
            sellerNote.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            sellerNote.setTextFill(Color.RED);
            sellerNote.setPadding(new Insets(15));
            sellerNote.setAlignment(Pos.CENTER);
            root.setBottom(sellerNote);
        } else {
            Label closedNote = new Label("This auction is " + item.getState());
            closedNote.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            closedNote.setTextFill(Color.GRAY);
            closedNote.setPadding(new Insets(15));
            closedNote.setAlignment(Pos.CENTER);
            root.setBottom(closedNote);
        }
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // Start real-time updates
        startRealTimeUpdates();
        
        stage.setOnCloseRequest(e -> stopUpdates());
        stage.show();
    }
    
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelControl = new Label(label);
        labelControl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Label valueControl = new Label(value);
        valueControl.setFont(Font.font("Arial", 12));
        
        grid.add(labelControl, 0, row);
        grid.add(valueControl, 1, row);
    }
    
    private void addInfoRowWithControl(GridPane grid, int row, String label, Label control) {
        Label labelControl = new Label(label);
        labelControl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        grid.add(labelControl, 0, row);
        grid.add(control, 1, row);
    }
    
    private void updateBidHistory() {
        bidHistoryList.getItems().clear();
        var bids = item.getBidHistory();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss");
        
        if (bids.isEmpty()) {
            bidHistoryList.getItems().add("No bids yet - Be the first!");
        } else {
            // Show most recent bids first
            for (int i = bids.size() - 1; i >= 0; i--) {
                Bid bid = bids.get(i);
                String bidInfo = String.format("$%.2f by %s at %s",
                    bid.getAmount(),
                    bid.getBidderUsername(),
                    bid.getTimestamp().format(formatter));
                bidHistoryList.getItems().add(bidInfo);
            }
        }
    }
    
    private String formatTimeRemaining() {
        long minutes = item.getTimeRemainingMinutes();
        if (minutes > 60) {
            long hours = minutes / 60;
            long mins = minutes % 60;
            return String.format("%d hour(s) %d minute(s)", hours, mins);
        }
        return minutes + " minute(s)";
    }
    
    private void startRealTimeUpdates() {
        updateTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { // Update every second
                    Platform.runLater(() -> {
                        // Update labels
                        currentBidLabel.setText(String.format("$%.2f", item.getCurrentBid()));
                        timeRemainingLabel.setText(formatTimeRemaining());
                        statusLabel.setText(item.isReserveMet() ? "✅ Reserve Met" : "⚠️ Reserve Not Met");
                        updateBidHistory();
                    });
                    lastUpdate = now;
                }
            }
        };
        updateTimer.start();
    }
    
    private void stopUpdates() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
    
    private void placeBid(TextField bidField) {
        String bidText = bidField.getText().trim();
        
        if (bidText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Bid", "Please enter a bid amount");
            return;
        }
        
        try {
            double bidAmount = Double.parseDouble(bidText);
            
            boolean success = item.placeBid(currentUser, bidAmount);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success!", 
                    String.format("Your bid of $%.2f has been placed successfully!", bidAmount));
                bidField.clear();
                updateBidHistory();
            } else {
                showAlert(Alert.AlertType.ERROR, "Bid Failed", 
                    "Your bid was rejected. Check the minimum bid requirement.");
            }
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Bid", "Please enter a valid number");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
