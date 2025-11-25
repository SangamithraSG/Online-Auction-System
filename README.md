**Dynamic Online Auction System (Java + JavaFX)**

An interactive desktop Online Auction System built with Java and JavaFX, showcasing strong Object-Oriented Design and a modern GUI for real-time bidding.

**Features:**
  User registration and login
  Role-based access: User and Admin
  Create auctions (title, description, category, starting price, reserve price, duration)
  Live auction list with current bid and status
  Place bids and view bid history (“My Bids”)
  Manage created auctions (“My Auctions”), see reserve status and winner info
  Admin panel: view system stats, users, and all auctions; remove auctions

**Tech Stack:**
  Java (JDK 11+)
  JavaFX (OpenJFX)
  
**Core:**
  Java Collections, Streams, and exception handling

**OOP & Java Concepts:**
  Classes & objects: User, Admin, AuctionItem, Bid, AuctionManager, etc.
  Inheritance & polymorphism: Admin extends User, role-specific behavior.
  Encapsulation & abstraction: private fields with getters/setters; AuctionManager hides business logic.
  Composition: auctions contain bids; users track their bids/auctions.
  Enums: AuctionState, ItemCategory, UserRole.
  Interfaces / patterns: observer-style bid updates (e.g., BidObserver).
  Collections & Streams: List, Map for users, auctions, bids.
  JavaFX & event-driven programming: Stage, Scene, TableView, Alert, event handlers, live countdowns.

**Project Structure (simplified):**

  src/com/auction/enums/ – enums (states, roles, categories)
  src/com/auction/models/ – core domain models (users, auctions, bids)
  src/com/auction/managers/ – business logic and persistence
  src/com/auction/observers/ – observer interfaces (if present)
  src/com/auction/ui/ – console UI (optional)
  src/com/auction/javafx/ – JavaFX GUI (AuctionApplication, dashboard, windows)

**Setup & Run:**
  
  Install JDK 11+ and download JavaFX SDK.


