package com.auction.javafx;

import com.auction.enums.*;
import com.auction.managers.AuctionManager;
import com.auction.models.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Dashboard Controller showing active auctions in TableView
 */
public class DashboardController {
    
    private Stage stage;
    private AuctionManager manager;
    private User currentUser;
    private TableView<AuctionItemRow> auctionTable;
    private ObservableList<AuctionItemRow> auctionData;
    private AnimationTimer refreshTimer;
    
    public DashboardController(Stage stage, AuctionManager manager, User currentUser) {
        this.stage = stage;
        this.manager = manager;
        this.currentUser = currentUser;
        this.auctionData = FXCollections.observableArrayList();
    }
    
    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Top bar with user info
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Center - Auction table
        VBox center = new VBox(15);
        center.setPadding(new Insets(20));
        
        Label title = new Label("Active Auctions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search auctions...");
        searchField.setPrefWidth(300);
        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        searchBtn.setOnAction(e -> searchAuctions(searchField.getText()));
        
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            refreshAuctions();
        });
        
        searchBox.getChildren().addAll(searchField, searchBtn, clearBtn);
        
        // Table
        auctionTable = createAuctionTable();
        
        // Buttons
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> refreshAuctions());
        
        Button createBtn = new Button("➕ Create Auction");
        createBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> showCreateAuction());
        
        Button myBidsBtn = new Button("📊 My Bids");
        myBidsBtn.setOnAction(e -> showMyBids());
        
        Button myAuctionsBtn = new Button("📦 My Auctions");
        myAuctionsBtn.setOnAction(e -> showMyAuctions());
        
        buttonBar.getChildren().addAll(refreshBtn, createBtn, myBidsBtn, myAuctionsBtn);
        
        if (currentUser.getRole() == UserRole.ADMIN) {
            Button adminBtn = new Button("⚙️ Admin Panel");
            adminBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
            adminBtn.setOnAction(e -> showAdminPanel());
            buttonBar.getChildren().add(adminBtn);
        }
        
        center.getChildren().addAll(title, searchBox, auctionTable, buttonBar);
        root.setCenter(center);
        
        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        
        // Initial load and start auto-refresh
        refreshAuctions();
        startAutoRefresh();
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #667eea;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername());
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label roleLabel = new Label("[" + currentUser.getRole() + "]");
        roleLabel.setTextFill(Color.LIGHTGRAY);
        roleLabel.setFont(Font.font("Arial", 14));
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> logout());
        
        topBar.getChildren().addAll(welcomeLabel, spacer, roleLabel, logoutBtn);
        return topBar;
    }
    
    private TableView<AuctionItemRow> createAuctionTable() {
        TableView<AuctionItemRow> table = new TableView<>();
        table.setItems(auctionData);
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Columns
        TableColumn<AuctionItemRow, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        TableColumn<AuctionItemRow, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        
        TableColumn<AuctionItemRow, String> currentBidCol = new TableColumn<>("Current Bid");
        currentBidCol.setCellValueFactory(new PropertyValueFactory<>("currentBid"));
        currentBidCol.setPrefWidth(120);
        
        TableColumn<AuctionItemRow, String> timeRemainingCol = new TableColumn<>("Time Left");
        timeRemainingCol.setCellValueFactory(new PropertyValueFactory<>("timeRemaining"));
        timeRemainingCol.setPrefWidth(100);
        
        TableColumn<AuctionItemRow, String> statusCol = new TableColumn<>("Reserve Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);
        
        TableColumn<AuctionItemRow, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View & Bid");
            {
                viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                viewBtn.setOnAction(e -> {
                    AuctionItemRow row = getTableView().getItems().get(getIndex());
                    showAuctionDetails(row.getAuctionItem());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        table.getColumns().addAll(titleCol, categoryCol, currentBidCol,
                                  timeRemainingCol, statusCol, actionCol);
        
        return table;
    }
    
    private void refreshAuctions() {
        auctionData.clear();
        for (AuctionItem item : manager.getActiveAuctions()) {
            auctionData.add(new AuctionItemRow(item));
        }
    }
    
    private void startAutoRefresh() {
        refreshTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { // Update every second
                    Platform.runLater(() -> {
                        // Update time remaining and bid info for each row
                        for (AuctionItemRow row : auctionData) {
                            row.refresh();
                        }
                        auctionTable.refresh();
                    });
                    lastUpdate = now;
                }
            }
        };
        refreshTimer.start();
    }
    
    private void searchAuctions(String keyword) {
        if (keyword.isEmpty()) {
            refreshAuctions();
            return;
        }
        auctionData.clear();
        for (AuctionItem item : manager.searchAuctions(keyword)) {
            if (item.getState() == AuctionState.ACTIVE) {
                auctionData.add(new AuctionItemRow(item));
            }
        }
    }
    
    private void showAuctionDetails(AuctionItem item) {
        AuctionDetailsWindow detailsWindow = new AuctionDetailsWindow(item, currentUser, manager);
        detailsWindow.show();
        refreshAuctions(); // Refresh after closing details window
    }
    
    private void showCreateAuction() {
        CreateAuctionWindow createWindow = new CreateAuctionWindow(currentUser, manager);
        createWindow.showAndWait();
        refreshAuctions();
    }
    
    private void showMyBids() {
        MyBidsWindow myBidsWindow = new MyBidsWindow(currentUser, manager);
        myBidsWindow.show();
    }
    
    private void showMyAuctions() {
        MyAuctionsWindow myAuctionsWindow = new MyAuctionsWindow(currentUser, manager);
        myAuctionsWindow.show();
    }
    
    private void showAdminPanel() {
        AdminPanelWindow adminPanel = new AdminPanelWindow(manager, (Admin) currentUser);
        adminPanel.show();
    }
    
    private void logout() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login screen.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Restart application to show login screen
                AuctionApplication app = new AuctionApplication();
                app.start(stage);
            }
        });
    }
    
    /**
     * Inner class to represent auction item rows in table
     */
    public static class AuctionItemRow {
        private AuctionItem auctionItem;
        private String title;
        private String category;
        private String currentBid;
        private String timeRemaining;
        private String status;
        
        public AuctionItemRow(AuctionItem item) {
            this.auctionItem = item;
            refresh();
        }
        
        public void refresh() {
            this.title = auctionItem.getTitle();
            this.category = auctionItem.getCategory().toString();
            this.currentBid = String.format("$%.2f", auctionItem.getCurrentBid());
            
            long minutes = auctionItem.getTimeRemainingMinutes();
            if (minutes > 60) {
                this.timeRemaining = (minutes / 60) + "h " + (minutes % 60) + "m";
            } else {
                this.timeRemaining = minutes + " min";
            }
            
            this.status = auctionItem.isReserveMet() ? "✅ Reserve Met" : "⚠️ Reserve Not Met";
        }
        
        // Getters for PropertyValueFactory
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getCurrentBid() { return currentBid; }
        public String getTimeRemaining() { return timeRemaining; }
        public String getStatus() { return status; }
        public AuctionItem getAuctionItem() { return auctionItem; }
    }
}
