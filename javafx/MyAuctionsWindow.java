package com.auction.javafx;

import com.auction.managers.AuctionManager;
import com.auction.models.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Window to display user's created auctions
 */
public class MyAuctionsWindow {
    
    private Stage stage;
    private User currentUser;
    private AuctionManager manager;
    
    public MyAuctionsWindow(User currentUser, AuctionManager manager) {
        this.currentUser = currentUser;
        this.manager = manager;
        this.stage = new Stage();
    }
    
    public void show() {
        stage.setTitle("My Auctions");
        stage.setWidth(850);
        stage.setHeight(600);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        
        Label titleLabel = new Label("My Auctions");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label subtitleLabel = new Label("Manage your created auctions");
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(header);
        
        // Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        List<AuctionItem> myAuctions = manager.getAuctionsBySeller(currentUser);
        
        if (myAuctions.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            
            Label emptyLabel = new Label("📦 No Auctions Created");
            emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            emptyLabel.setTextFill(Color.GRAY);
            
            Label tipLabel = new Label("Create your first auction to start selling!");
            tipLabel.setTextFill(Color.GRAY);
            
            Button createBtn = new Button("Create Auction");
            createBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 30px;");
            createBtn.setOnAction(e -> {
                CreateAuctionWindow createWindow = new CreateAuctionWindow(currentUser, manager);
                createWindow.showAndWait();
                stage.close();
            });
            
            emptyBox.getChildren().addAll(emptyLabel, tipLabel, createBtn);
            content.getChildren().add(emptyBox);
        } else {
            // Statistics
            long activeCount = myAuctions.stream()
                .filter(a -> a.getState().toString().equals("ACTIVE")).count();
            long closedCount = myAuctions.stream()
                .filter(a -> a.getState().toString().equals("CLOSED")).count();
            
            HBox statsBox = new HBox(30);
            statsBox.setPadding(new Insets(10));
            statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
            
            VBox totalBox = createStatBox("Total Auctions", String.valueOf(myAuctions.size()), "#2196F3");
            VBox activeBox = createStatBox("Active", String.valueOf(activeCount), "#4CAF50");
            VBox closedBox = createStatBox("Closed", String.valueOf(closedCount), "#757575");
            
            statsBox.getChildren().addAll(totalBox, activeBox, closedBox);
            content.getChildren().add(statsBox);
            
            // Display each auction
            for (AuctionItem auction : myAuctions) {
                VBox auctionBox = createAuctionBox(auction);
                content.getChildren().add(auctionBox);
            }
        }
        
        scrollPane.setContent(content);
        root.setCenter(scrollPane);
        
        // Bottom button
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(15));
        bottomBar.setAlignment(Pos.CENTER);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");
        closeButton.setOnAction(e -> stage.close());
        
        bottomBar.getChildren().add(closeButton);
        root.setBottom(bottomBar);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createStatBox(String label, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
        box.setPrefWidth(150);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.WHITE);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Arial", 14));
        labelText.setTextFill(Color.WHITE);
        
        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }
    
    private VBox createAuctionBox(AuctionItem auction) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        // Title and status
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(auction.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label stateLabel = new Label(auction.getState().toString());
        stateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        stateLabel.setPadding(new Insets(5, 15, 5, 15));
        stateLabel.setStyle("-fx-background-radius: 3;");
        
        switch (auction.getState().toString()) {
            case "ACTIVE":
                stateLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 3;");
                break;
            case "CLOSED":
                stateLabel.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-background-radius: 3;");
                break;
            case "PENDING":
                stateLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 3;");
                break;
        }
        
        titleBox.getChildren().addAll(titleLabel, spacer, stateLabel);
        
        // Category
        Label categoryLabel = new Label("Category: " + auction.getCategory());
        categoryLabel.setTextFill(Color.GRAY);
        categoryLabel.setFont(Font.font("Arial", 12));
        
        // Bid information
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(40);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(10, 0, 10, 0));
        
        int row = 0;
        addInfoRow(infoGrid, row++, "Starting Price:", String.format("$%.2f", auction.getStartingPrice()));
        addInfoRow(infoGrid, row++, "Current Bid:", String.format("$%.2f", auction.getCurrentBid()));
        addInfoRow(infoGrid, row++, "Reserve Price:", String.format("$%.2f", auction.getReservePrice()));
        addInfoRow(infoGrid, row++, "Total Bids:", String.valueOf(auction.getBidHistory().size()));
        
        if (auction.getState().toString().equals("ACTIVE")) {
            long timeLeft = auction.getTimeRemainingMinutes();
            String timeStr = timeLeft > 60 ? (timeLeft/60) + "h " + (timeLeft%60) + "m" : timeLeft + " min";
            addInfoRow(infoGrid, row++, "Time Remaining:", timeStr);
        }
        
        // Reserve status
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(5, 0, 5, 0));
        
        Label statusLabel = new Label(auction.isReserveMet() ? "✅ Reserve Price Met" : "⚠️ Reserve Price Not Met");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setTextFill(auction.isReserveMet() ? Color.GREEN : Color.ORANGE);
        
        statusBox.getChildren().add(statusLabel);
        
        // Winner information (if closed and sold)
        if (auction.getState().toString().equals("CLOSED") && auction.isReserveMet()) {
            Label winnerLabel = new Label("🏆 Sold to highest bidder for $" + 
                String.format("%.2f", auction.getCurrentBid()));
            winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            winnerLabel.setTextFill(Color.GREEN);
            box.getChildren().addAll(titleBox, categoryLabel, infoGrid, statusBox, winnerLabel);
        } else if (auction.getState().toString().equals("CLOSED") && !auction.isReserveMet()) {
            Label noSaleLabel = new Label("❌ Not sold - Reserve price was not met");
            noSaleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            noSaleLabel.setTextFill(Color.RED);
            box.getChildren().addAll(titleBox, categoryLabel, infoGrid, statusBox, noSaleLabel);
        } else {
            box.getChildren().addAll(titleBox, categoryLabel, infoGrid, statusBox);
        }
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewDetailsBtn.setOnAction(e -> {
            AuctionDetailsWindow detailsWindow = new AuctionDetailsWindow(auction, currentUser, manager);
            detailsWindow.show();
        });
        
        buttonBox.getChildren().add(viewDetailsBtn);
        
        // Show end button only for active auctions
        if (auction.getState().toString().equals("ACTIVE")) {
            Button endBtn = new Button("End Auction Now");
            endBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
            endBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("End Auction");
                confirm.setHeaderText("End this auction now?");
                confirm.setContentText("This action cannot be undone.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        auction.endAuction();
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Auction Ended");
                        info.setHeaderText("Auction has been ended");
                        info.showAndWait();
                        stage.close();
                    }
                });
            });
            buttonBox.getChildren().add(endBtn);
        }
        
        box.getChildren().add(buttonBox);
        
        return box;
    }
    
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelControl = new Label(label);
        labelControl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        labelControl.setTextFill(Color.GRAY);
        
        Label valueControl = new Label(value);
        valueControl.setFont(Font.font("Arial", 13));
        
        grid.add(labelControl, 0, row);
        grid.add(valueControl, 1, row);
    }
}
