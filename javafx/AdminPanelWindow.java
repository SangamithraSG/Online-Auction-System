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

import java.util.List;

/**
 * Admin Panel for managing users and auctions
 */
public class AdminPanelWindow {
    
    private Stage stage;
    private AuctionManager manager;
    private Admin admin;
    
    public AdminPanelWindow(AuctionManager manager, Admin admin) {
        this.manager = manager;
        this.admin = admin;
        this.stage = new Stage();
    }
    
    public void show() {
        stage.setTitle("Admin Panel");
        stage.setWidth(900);
        stage.setHeight(650);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ff6b6b;");
        
        Label titleLabel = new Label("⚙️ Admin Control Panel");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label subtitleLabel = new Label("System Management & Oversight");
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(header);
        
        // Tabs for different admin functions
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab 1: System Statistics
        Tab statsTab = new Tab("📊 Statistics");
        statsTab.setContent(createStatisticsPane());
        
        // Tab 2: User Management
        Tab usersTab = new Tab("👥 Users");
        usersTab.setContent(createUsersPane());
        
        // Tab 3: Auction Management
        Tab auctionsTab = new Tab("🔨 Auctions");
        auctionsTab.setContent(createAuctionsPane());
        
        tabPane.getTabs().addAll(statsTab, usersTab, auctionsTab);
        root.setCenter(tabPane);
        
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
    
    /**
     * Create statistics pane
     */
    private VBox createStatisticsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(30));
        
        Label title = new Label("System Statistics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Statistics cards
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);
        
        List<User> allUsers = manager.getAllUsers();
        List<AuctionItem> allAuctions = manager.getAllAuctions();
        long activeAuctions = manager.getActiveAuctions().size();
        
        long totalBids = allAuctions.stream()
            .mapToLong(a -> a.getBidHistory().size())
            .sum();
        
        VBox usersCard = createStatCard("Total Users", String.valueOf(allUsers.size()), "#2196F3");
        VBox auctionsCard = createStatCard("Total Auctions", String.valueOf(allAuctions.size()), "#4CAF50");
        VBox activeCard = createStatCard("Active Auctions", String.valueOf(activeAuctions), "#FF9800");
        VBox bidsCard = createStatCard("Total Bids", String.valueOf(totalBids), "#9C27B0");
        
        statsCards.getChildren().addAll(usersCard, auctionsCard, activeCard, bidsCard);
        
        // Detailed breakdown
        VBox breakdown = new VBox(15);
        breakdown.setPadding(new Insets(20));
        breakdown.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label breakdownTitle = new Label("Detailed Breakdown");
        breakdownTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // User roles
        long adminCount = allUsers.stream()
            .filter(u -> u.getRole().toString().equals("ADMIN")).count();
        long regularUsers = allUsers.size() - adminCount;
        
        Label usersBreakdown = new Label(String.format("Users: %d Regular, %d Admins", 
            regularUsers, adminCount));
        usersBreakdown.setFont(Font.font("Arial", 14));
        
        // Auction states
        long closedAuctions = allAuctions.stream()
            .filter(a -> a.getState().toString().equals("CLOSED")).count();
        long pendingAuctions = allAuctions.stream()
            .filter(a -> a.getState().toString().equals("PENDING")).count();
        
        Label auctionsBreakdown = new Label(String.format("Auctions: %d Active, %d Closed, %d Pending",
            activeAuctions, closedAuctions, pendingAuctions));
        auctionsBreakdown.setFont(Font.font("Arial", 14));
        
        // Reserve met
        long reserveMet = allAuctions.stream()
            .filter(AuctionItem::isReserveMet).count();
        
        Label reserveLabel = new Label(String.format("Auctions with Reserve Met: %d (%.1f%%)",
            reserveMet, allAuctions.isEmpty() ? 0 : (reserveMet * 100.0 / allAuctions.size())));
        reserveLabel.setFont(Font.font("Arial", 14));
        
        // Average bids per auction
        double avgBids = allAuctions.isEmpty() ? 0 : (double) totalBids / allAuctions.size();
        Label avgBidsLabel = new Label(String.format("Average Bids per Auction: %.2f", avgBids));
        avgBidsLabel.setFont(Font.font("Arial", 14));
        
        breakdown.getChildren().addAll(breakdownTitle, usersBreakdown, auctionsBreakdown, 
                                       reserveLabel, avgBidsLabel);
        
        pane.getChildren().addAll(title, statsCards, breakdown);
        
        return pane;
    }
    
    /**
     * Create users management pane
     */
    private VBox createUsersPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        
        Label title = new Label("User Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Users list
        ListView<String> usersList = new ListView<>();
        usersList.setPrefHeight(400);
        usersList.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        List<User> allUsers = manager.getAllUsers();
        for (User user : allUsers) {
            String userInfo = String.format("%-20s | %-30s | %-10s | Bids: %d",
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getMyBidIds().size());
            usersList.getItems().add(userInfo);
        }
        
        Label countLabel = new Label("Total Users: " + allUsers.size());
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> {
            usersList.getItems().clear();
            List<User> users = manager.getAllUsers();
            for (User user : users) {
                String userInfo = String.format("%-20s | %-30s | %-10s | Bids: %d",
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getMyBidIds().size());
                usersList.getItems().add(userInfo);
            }
            countLabel.setText("Total Users: " + users.size());
        });
        
        pane.getChildren().addAll(title, countLabel, usersList, refreshBtn);
        
        return pane;
    }
    
    /**
     * Create auctions management pane
     */
    private VBox createAuctionsPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        
        Label title = new Label("Auction Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Auctions list
        ListView<HBox> auctionsList = new ListView<>();
        auctionsList.setPrefHeight(400);
        
        List<AuctionItem> allAuctions = manager.getAllAuctions();
        
        for (AuctionItem auction : allAuctions) {
            HBox auctionRow = new HBox(15);
            auctionRow.setPadding(new Insets(10));
            auctionRow.setAlignment(Pos.CENTER_LEFT);
            auctionRow.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
            
            VBox infoBox = new VBox(5);
            
            Label titleLabel = new Label(auction.getTitle());
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            
            Label detailsLabel = new Label(String.format("State: %s | Bids: %d | Current: $%.2f | Seller: %s",
                auction.getState(),
                auction.getBidHistory().size(),
                auction.getCurrentBid(),
                auction.getSellerUsername()));
            detailsLabel.setFont(Font.font("Arial", 11));
            detailsLabel.setTextFill(Color.GRAY);
            
            infoBox.getChildren().addAll(titleLabel, detailsLabel);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button viewBtn = new Button("View");
            viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            viewBtn.setOnAction(e -> {
                AuctionDetailsWindow detailsWindow = new AuctionDetailsWindow(auction, admin, manager);
                detailsWindow.show();
            });
            
            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            removeBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Remove Auction");
                confirm.setHeaderText("Remove this auction?");
                confirm.setContentText("Auction: " + auction.getTitle() + "\n\nThis action cannot be undone.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        admin.removeAuction(auction.getItemId());
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Success");
                        success.setContentText("Auction removed successfully");
                        success.showAndWait();
                        stage.close();
                    }
                });
            });
            
            auctionRow.getChildren().addAll(infoBox, spacer, viewBtn, removeBtn);
            auctionsList.getItems().add(auctionRow);
        }
        
        Label countLabel = new Label("Total Auctions: " + allAuctions.size());
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        HBox buttonBox = new HBox(10);
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> {
            stage.close();
            AdminPanelWindow newPanel = new AdminPanelWindow(manager, admin);
            newPanel.show();
        });
        
        Button viewAllBtn = new Button("📋 Print All to Console");
        viewAllBtn.setOnAction(e -> admin.viewAllAuctions());
        
        buttonBox.getChildren().addAll(refreshBtn, viewAllBtn);
        
        pane.getChildren().addAll(title, countLabel, auctionsList, buttonBox);
        
        return pane;
    }
    
    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        card.setPrefWidth(180);
        card.setPrefHeight(150);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.WHITE);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        labelText.setTextFill(Color.WHITE);
        
        card.getChildren().addAll(valueLabel, labelText);
        return card;
    }
}
