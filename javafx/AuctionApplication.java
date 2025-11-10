package com.auction.javafx;

import com.auction.enums.UserRole;
import com.auction.managers.AuctionManager;
import com.auction.models.User;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * JavaFX Main Application for Online Auction System
 */
public class AuctionApplication extends Application {
    
    private Stage primaryStage;
    private AuctionManager manager;
    private User currentUser;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.manager = AuctionManager.getInstance();
        
        primaryStage.setTitle("Online Auction System");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        
        showLoginScreen();
        primaryStage.show();
    }
    
    /**
     * Show login/registration screen
     */
    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");
        
        // Title
        Label titleLabel = new Label("ONLINE AUCTION SYSTEM");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        
        // Login Panel
        VBox loginPanel = new VBox(15);
        loginPanel.setPadding(new Insets(30));
        loginPanel.setMaxWidth(400);
        loginPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        Label loginTitle = new Label("Login to Your Account");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);
        
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password");
                return;
            }
            
            User user = manager.login(username, password);
            if (user != null) {
                currentUser = user;
                showDashboard();
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });
        
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setOnAction(e -> showRegistrationScreen());
        
        loginPanel.getChildren().addAll(loginTitle, usernameField, passwordField,
                                       messageLabel, loginButton, registerLink);
        
        root.getChildren().addAll(titleLabel, loginPanel);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
    
    /**
     * Show registration screen
     */
    private void showRegistrationScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");
        
        VBox regPanel = new VBox(15);
        regPanel.setPadding(new Insets(30));
        regPanel.setMaxWidth(400);
        regPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        Label regTitle = new Label("Create New Account");
        regTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        CheckBox adminCheck = new CheckBox("Register as Admin");
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);
        
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                              "-fx-font-size: 14px; -fx-padding: 10px 40px;");
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String email = emailField.getText().trim();
            UserRole role = adminCheck.isSelected() ? UserRole.ADMIN : UserRole.USER;
            
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                messageLabel.setText("Please fill all fields");
                return;
            }
            
            if (manager.registerUser(username, password, email, role)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Registration Successful");
                alert.setContentText("You can now login with your credentials");
                alert.showAndWait();
                showLoginScreen();
            } else {
                messageLabel.setText("Username already exists");
            }
        });
        
        Hyperlink backLink = new Hyperlink("Back to Login");
        backLink.setOnAction(e -> showLoginScreen());
        
        regPanel.getChildren().addAll(regTitle, usernameField, passwordField,
                                     emailField, adminCheck, messageLabel,
                                     registerButton, backLink);
        
        root.getChildren().add(regPanel);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
    
    /**
     * Show main dashboard
     */
    private void showDashboard() {
        DashboardController dashboard = new DashboardController(primaryStage, manager, currentUser);
        dashboard.show();
    }
}
