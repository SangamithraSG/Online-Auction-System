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
 * Window to display user's bid history
 */
public class MyBidsWindow {
    
    private Stage stage;
    private User currentUser;
    private AuctionManager manager;
    
    public MyBidsWindow(User currentUser, AuctionManager manager) {
        this.currentUser = currentUser;
        this.manager = manager;
        this.stage = new Stage();
    }
    
    public void show() {
        stage.setTitle("My Bids");
        stage.setWidth(800);
        stage.setHeight(600);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        
        Label titleLabel = new Label("My Bids");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label subtitleLabel = new Label("Track all your bidding activity");
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(header);
        
        // Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-border-color: transparent;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        List<String> myBidIds = currentUser.getMyBidIds();
        
        if (myBidIds.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            
            Label emptyLabel = new Label("📭 No Bids Yet");
            emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            emptyLabel.setTextFill(Color.GRAY);
            
            Label tipLabel = new Label("Start bidding on active auctions to see your bid history here!");
            tipLabel.setTextFill(Color.GRAY);
            
            emptyBox.getChildren().addAll(emptyLabel, tipLabel);
            content.getChildren().add(emptyBox);
        } else {
            Label statsLabel = new Label("Total Bids Placed: " + myBidIds.size());
            statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            content.getChildren().add(statsLabel);
            
            // Group bids by auction
            Map<AuctionItem, List<Bid>> bidsByAuction = new HashMap<>();
            
            for (AuctionItem auction : manager.getAllAuctions()) {
                List<Bid> relevantBids = new ArrayList<>();
                for (Bid bid : auction.getBidHistory()) {
                    if (myBidIds.contains(bid.getBidId())) {
                        relevantBids.add(bid);
                    }
                }
                if (!relevantBids.isEmpty()) {
                    bidsByAuction.put(auction, relevantBids);
                }
            }
            
            // Display each auction with user's bids
            for (Map.Entry<AuctionItem, List<Bid>> entry : bidsByAuction.entrySet()) {
                AuctionItem auction = entry.getKey();
                List<Bid> bids = entry.getValue();
                
                VBox auctionBox = createAuctionBidBox(auction, bids);
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
    
    private VBox createAuctionBidBox(AuctionItem auction, List<Bid> bids) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        // Auction title and status
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📦 " + auction.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label stateLabel = new Label(auction.getState().toString());
        stateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        stateLabel.setPadding(new Insets(5, 10, 5, 10));
        stateLabel.setStyle("-fx-background-radius: 3;");
        
        switch (auction.getState()) {
            case ACTIVE:
                stateLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 3;");
                break;
            case CLOSED:
                stateLabel.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-background-radius: 3;");
                break;
            case PENDING:
                stateLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 3;");
                break;
        }
        
        titleBox.getChildren().addAll(titleLabel, spacer, stateLabel);
        
        // Auction info
        Label infoLabel = new Label(String.format("Current Bid: $%.2f | Your Bids: %d", 
            auction.getCurrentBid(), bids.size()));
        infoLabel.setTextFill(Color.GRAY);
        
        // Check if user is winning
        boolean isWinning = false;
        if (auction.getCurrentHighestBidderId() != null && 
            auction.getCurrentHighestBidderId().equals(currentUser.getUserId())) {
            isWinning = true;
        }
        
        if (isWinning && auction.getState().toString().equals("ACTIVE")) {
            Label winningLabel = new Label("🏆 You are currently winning!");
            winningLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            winningLabel.setTextFill(Color.GREEN);
            box.getChildren().addAll(titleBox, infoLabel, winningLabel);
        } else {
            box.getChildren().addAll(titleBox, infoLabel);
        }
        
        // User's bids
        VBox bidsBox = new VBox(5);
        bidsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label bidsHeader = new Label("Your Bids:");
        bidsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        bidsBox.getChildren().add(bidsHeader);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
        
        // Sort bids by timestamp (most recent first)
        bids.sort((b1, b2) -> b2.getTimestamp().compareTo(b1.getTimestamp()));
        
        for (Bid bid : bids) {
            HBox bidRow = new HBox(10);
            bidRow.setAlignment(Pos.CENTER_LEFT);
            
            Label bidLabel = new Label(String.format("$%.2f", bid.getAmount()));
            bidLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            
            Label timeLabel = new Label("placed on " + bid.getTimestamp().format(formatter));
            timeLabel.setTextFill(Color.GRAY);
            timeLabel.setFont(Font.font("Arial", 11));
            
            // Highlight if this is the highest bid
            if (bid.getAmount() == auction.getCurrentBid() && 
                bid.getBidderId().equals(currentUser.getUserId())) {
                bidLabel.setTextFill(Color.GREEN);
                Label highestLabel = new Label("(Highest)");
                highestLabel.setTextFill(Color.GREEN);
                highestLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                bidRow.getChildren().addAll(bidLabel, timeLabel, highestLabel);
            } else {
                bidRow.getChildren().addAll(bidLabel, timeLabel);
            }
            
            bidsBox.getChildren().add(bidRow);
        }
        
        box.getChildren().add(bidsBox);
        
        // Action button
        if (auction.getState().toString().equals("ACTIVE")) {
            Button viewButton = new Button("View Auction");
            viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            viewButton.setOnAction(e -> {
                AuctionDetailsWindow detailsWindow = new AuctionDetailsWindow(auction, currentUser, manager);
                detailsWindow.show();
            });
            
            HBox buttonBox = new HBox();
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            buttonBox.getChildren().add(viewButton);
            box.getChildren().add(buttonBox);
        }
        
        return box;
    }
}
