 
Content 
Content---------------------------------------------------------------------------------------------------------------------------- 1 
Abstract-----------------------------------------------------------------------------------------------------------------------2 
1. Introduction--------------------------------------------------------------------------------------------------------------- 3 
1.1 Problem Definition------------------------------------------------------------------------------------------------3 
1.2 Project Scope------------------------------------------------------------------------------------------------------ 3 
1.3 Objectives---------------------------------------------------------------------------------------------------------- 4 
2. System Analysis----------------------------------------------------------------------------------------------------------4 
2.1 Functional Requirements-----------------------------------------------------------------------------------------4 
2.2 Non-functional Requirements----------------------------------------------------------------------------------- 5 
2.3 Use Case Explanation-------------------------------------------------------------------------------------------- 5 
3. Domain Model & UML------------------------------------------------------------------------------------------------- 6 
3.1 UML Class Diagram----------------------------------------------------------------------------------------------6 
3.2 Explanation of Domain Classes--------------------------------------------------------------------------------- 6 
3.3 Layer Responsibilities-------------------------------------------------------------------------------------------- 7 
A. Service Layer (Business Logic)---------------------------------------------------------------------------- 7 
B. UI (Presentation) Layer-------------------------------------------------------------------------------------10 
4. Architecture Design---------------------------------------------------------------------------------------------------- 12 
4.1 Layered Architecture Explanation-----------------------------------------------------------------------------12 
4.2 Presentation Layer (UI Layer)--------------------------------------------------------------------------------- 12 
4.3 Business Logic Layer (Service Layer)------------------------------------------------------------------------13 
4.4 Data Access Layer (DAO Layer)------------------------------------------------------------------------------13 
4.5 Database Layer---------------------------------------------------------------------------------------------------13 
4.6 MVC Structure (Model-View-Controller)--------------------------------------------------------------------14 
4.7 DAO Pattern Usage--------------------------------------------------------------------------------------------- 14 
5. Desktop Application (Phase 1)--------------------------------------------------------------------------------------- 14 
5.1 Swing Structure (User Interface Structure)------------------------------------------------------------------ 14 
5.2 CRUD Screens (Management Screens)---------------------------------------------------------------------- 15 
5.3 JDBC Integration (Database Integration)-------------------------------------------------------------------- 15 
6. Database Design-------------------------------------------------------------------------------------------------------- 20 
6.1 ER Diagram------------------------------------------------------------------------------------------------------ 20 
6.2 Table structures-------------------------------------------------------------------------------------------------- 21 
6.3 SQL scripts------------------------------------------------------------------------------------------------------- 22 
7. Testing & Validation--------------------------------------------------------------------------------------------------- 24 
7.1 Test Scenarios---------------------------------------------------------------------------------------------------- 24 
7.2 Sample Outputs-------------------------------------------------------------------------------------------------- 24 
7.3 Error Handling Cases------------------------------------------------------------------------------------------- 25 
8. Discussion--------------------------------------------------------------------------------------------------------------- 26 
8.1 Reusability Analysis-------------------------------------------------------------------------------------------- 26 
8.2 Challenges Faced------------------------------------------------------------------------------------------------ 26 
9. Conclusion---------------------------------------------------------------------------------------------------------------26 
9.1 Achievements---------------------------------------------------------------------------------------------------- 26 
9.2 Future Improvements------------------------------------------------------------------------------------------- 27 
References (APA Format)------------------------------------------------------------------------------------------------ 27 
1 
Abstract 
This study outlines the design and implementation of a role-based Inventory and Stock 
Management System aimed at improving operational inefficiencies in small and 
medium-sized enterprises. Traditional stock control methods often depend on manual tracking 
or separate software tools, leading to inconsistencies, delays in updates, and limited 
traceability. To address these issues, the proposed system offers a structured solution driven 
by a relational database. It brings together user role management, product categorization, 
order processing, purchase request workflows, and automatic stock updates within one 
system. 
The system uses Microsoft SQL Server and follows relational database normalization 
principles to maintain data integrity, scalability, and easy maintenance. A role-based access 
control system sets apart regular users, staff members, suppliers, and administrators, allowing 
for controlled access to different parts of the system. Stock quantities are updated in real time 
when orders are placed and when suppliers approve those orders. All inventory movements 
are recorded to ensure auditability and operational transparency. 
By automating stock deductions and restocking processes, the system cuts down on manual 
errors and improves efficiency. The resulting design shows a structured and flexible 
framework for organizations looking to digitalize and standardize their inventory management 
tasks. 
2 
1. Introduction 
1.1 Problem Definition 
This study outlines the design and implementation of a role-based Inventory and Stock 
Management System aimed at improving operational inefficiencies in small and 
medium-sized enterprises. Traditional stock control methods often depend on manual tracking 
or separate software tools, leading to inconsistencies, delays in updates, and limited 
traceability. To address these issues, the proposed system offers a structured solution driven 
by a relational database. It brings together user role management, product categorization, 
order processing, purchase request workflows, and automatic stock updates within one 
system. 
The system uses MySQL and follows relational database normalization principles to maintain 
data integrity, scalability, and easy maintenance. A role-based access control system sets apart 
regular users, staff members, suppliers, and administrators, allowing for controlled access to 
different parts of the system. Stock quantities are updated in real time when orders are placed 
and when suppliers approve those orders. All inventory movements are recorded to ensure 
auditability and operational transparency. 
By automating stock deductions and restocking processes, the system cuts down on manual 
errors and improves efficiency. The resulting design shows a structured and flexible 
framework for organizations looking to digitalize and standardize their inventory management 
tasks. 
1.2 Project Scope 
The project deals with the development of a role-based inventory and stock management 
system that is supposed to manage the operations of product lifecycle in a centralized 
database system. The inventory control system is structured to manage users, products, 
categories, orders, suppliers, and stock movement records.  
The scope of the project includes: 
3 
● There is user authentication and role-based authorization mechanism with different 
access levels for normal users, staff members, suppliers, and administrators.  
● Standard users can only view products and make orders.  
● Authorized staff can view the available stock and generate purchase requisitions when 
the stock falls below the predefined thresholds.  
● Suppliers can examine requests and either approve or reject them.  
● Automated stock update processes: order placement reduces stock, supplier’s approval 
increases stock.  
● All stock transactions are recorded for traceability and auditability.  
Exclusions: 
● Payment processing 
● Shipping or cargo tracking 
● External enterprise integration modules 
1.3 Objectives 
● Role-based inventory and stock management system (design & implement).  
● Deliver a single management system for products, orders, suppliers, and movements 
of stocks.  
● Ensure consistency and traceability of data.  
● Finally, integrate role-based access control for users’ interaction with the system 
securely.  
● Stock updates and procurement workflows ought to be automated.  
● Develop a scalable and maintainable solution that supports digitalization of inventory 
operations.  
2. System Analysis 
2.1 Functional Requirements 
The system must meet the following functional requirements: 
4 
● User registration and login by role.  
● Product viewing for normal users.  
● Order placement for available products.  
● Automatic stock Deduction on Creating Order  
● Monitoring of the stock by the staff;  
● Staff making purchase requests below the minimum stock threshold.  
● Furnishing Supply approval/rejection of purchase requests.  
● Increase in stock automatically when purchase requests are approved.  
● Recording of all stock movement transactions;  
● Administrative management of users and roles.  
2.2 Non-functional Requirements 
Non-functional requirements describe quality attributes and system performance:  
● Ensure data integrity based on relational database constraints.  
● Secure authentication and authorization schemes.  
● Keep the system response times for queries within acceptable limits.  
● Scalability of future module additions.  
● Password security using hash-based storage mechanisms.  
● Maintainable and modular design.  
● Allow concurrent users without inconsistency.  
2.3 Use Case Explanation 
● Customers: gather product information and order products. This process automatically 
decreases stock;  
● Staff: Inventory control, creation of purchase request forms for items with low stocks, 
and the statistics panel.  
● Suppliers: Approve or denote pending purchase requests. The approved requests 
automatically increase the stock;  
● Administrators: For users and roles management and operations on the product catalog 
such as adding, removing, or updating products.  
5 
3. Domain Model & UML 
3.1 UML Class Diagram 
3.2 Explanation of Domain Classes 
● User & Role: manage identity and RBAC. 
● Product: maintains stock levels, minimum thresholds. 
● Category: organizes products hierarchically. 
● Order & PurchaseRequest: manage transactions and procurement workfl 
6 
3.3 Layer Responsibilities 
The application follows a structured layered architecture to guarantee the Separation of 
Concerns (SoC). The layering isolates the user interface layer, business logic layer, and the 
database access layer, making the system maintainable, scalable, and testable.  
A. Service Layer (Business Logic) 
The service layer is the main intermediary between the presentation (UI) layer and the data 
access (DAO) layer. It executes the business-rules, validations, and acts as a coordinator. The 
interfaces (IUserService, IProductService, etc.) ensure loose coupling.  
AdminService (AdminServiceImpl) 
This service is responsible for administrative operations related to product management. 
Responsibilities: 
● Adds new products to the system 
● Removes existing products 
● Validates business rules before database operations 
Business Rules: 
● Product price cannot be negative 
Explanation: 
Before adding a product, the service checks whether the price is valid. If the price is less than 
zero, the operation is rejected. Otherwise, the request is forwarded to the Data Access Layer. 
7 
CustomerService (CustomerServiceImpl) 
● Handles customer purchasing operations. 
Responsibilities: 
● Allows customers to purchase products 
● Applies constraints on purchase quantity 
● Business Rules: 
● Quantity must be greater than 0 
● Maximum purchase limit is 5 units per transaction 
Explanation: 
The service ensures that invalid purchase requests (such as zero or excessive quantities) are 
prevented before calling the DAO layer. 
ProductService (ProductServiceImpl) 
● Manages product-related operations and stock evaluation logic. 
Responsibilities: 
● Retrieves all products from the system 
● Determines stock status 
Business Rules: 
● Checks if stock is below minimum threshold 
Explanation: 
The method isStockLow() compares current stock with the minimum stock level and helps 
trigger procurement processes when needed. 
8 
PurchaseRequestService (PurchaseRequestServiceImpl) 
● Manages procurement requests created by staff. 
Responsibilities: 
● Creates purchase requests 
● Retrieves requests by user 
● Provides reporting and analytics (monthly trends, most requested products, summary 
statistics) 
Business Rules: 
● Request quantity must be greater than 0 
Explanation: 
Invalid requests are filtered before being stored. This service also supports reporting features, 
making it important for decision-making processes. 
SupplierService (SupplierServiceImpl) 
● Handles supplier-related workflows. 
Responsibilities: 
● Retrieves pending purchase requests assigned to suppliers 
● Approves or rejects requests 
● Provides supplier list 
Explanation: 
When a supplier approves a request, the stock is updated accordingly via DAO operations. 
Rejected requests are marked as such without affecting stock. 
9 
UserService (UserServiceImpl) 
Responsible for authentication and user registration. 
Responsibilities: 
● Authenticates users 
● Registers new users 
Business Rules: 
● Email and password cannot be empty 
● Password must be at least 4 characters long 
Explanation: 
Ensures secure and valid user data before interacting with the database. 
B. UI (Presentation) Layer 
This layer represents the visual interface that the user interacts with and is 
implemented entirely using Java Swing. It is designed to meet modern desktop 
application standards. 
1. User Interface Design (Visual Design) 
The application is customized beyond standard Java windows to enhance user 
experience (UX): 
Modern Appearance: 
● Login and Register screens are designed with a dark theme (Dark Mode), 
gradient backgrounds, and rounded input fields (RoundedBorder). 
10 
Dynamic Listing: 
● In the customer interface (CustomerFrame), products are not displayed as a 
static list. Instead, they are dynamically generated from the database and 
shown as “Product Cards” in a grid layout. 
2. Role-Based Screen Management 
The system displays different interfaces depending on the role of the logged-in user: 
Customers: 
● Are directed to a market screen where they can view and purchase products. 
Staff/Admin: 
● Access management panels where they can manage stock levels and perform 
analysis. 
Suppliers: 
● Use a dedicated interface where they can review and approve or reject 
incoming product requests. 
3. Workflow and Feedback (Event Handling & Feedback) 
All user actions (such as button clicks or text input) are captured and processed in this 
layer: 
Triggers: 
● When a button is clicked, the UI layer packages the request and sends it to the 
Controller/Service layer. 
Instant Feedback: 
● Users are notified through dialog messages (JOptionPane) when operations are 
successful or when issues occur (e.g., insufficient stock). 
11 
Automatic Updates: 
After a product is purchased, stock values are updated dynamically on the screen 
without requiring a page refresh, ensuring synchronization with the database. 
4. Architecture Design 
This project is structured according to the principles of Logical Layered Architecture and the 
MVC (Model-View-Controller) design pattern to prevent code clutter and facilitate 
maintenance. Although the application is a monolithic desktop program, the code is clearly 
separated into packages based on functionality. 
4.1 Layered Architecture Explanation 
The application is based on the principle of Separation of Concerns. This ensures that a 
change in the user interface does not affect database queries. Communication between layers 
is hierarchical: View → Controller → Service → DAO → Database. 
4.2 Presentation Layer (UI Layer) 
This is the layer where the user interacts with the system. It is developed using the Java Swing 
library. 
Responsibilities:  
● Collect data from the user and present results visually. 
Scope:  
● Includes classes such as LoginFrame, RegisterFrame, CustomerFrame, and 
ProductCard. Custom Graphics2D drawings are used to give a modern appearance. 
12 
4.3 Business Logic Layer (Service Layer) 
This layer consists of Java classes that act as the "decision center" of the system. 
Responsibilities:  
● Apply business rules before interacting with the database. 
Example: The CustomerServiceImpl class checks whether a customer can purchase more than 
5 products at once. If the rule is violated, the operation is canceled before reaching the DAO 
layer. 
4.4 Data Access Layer (DAO Layer) 
This is the only layer that communicates directly with the database. 
● Responsibilities: Execute SQL queries and perform CRUD (Create, Read, Update, 
Delete) operations. 
Technical Details: The JDBC library is used. Data access is handled through classes such as 
userDAO, productDAO, and customerDAO. 
4.5 Database Layer 
This layer is responsible for persistent data storage. 
Technology: 
●  MySQL Relational Database 
Structure: Relational tables (Users, Products, Orders, Categories) with foreign key 
relationships maintain data integrity. 
13 
4.6 MVC Structure (Model-View-Controller) 
The MVC pattern is applied throughout the project as follows: 
Model: POJO classes representing data structures (e.g., Product.java, User.java). 
View: Java Swing windows. 
Controller: Classes that capture events from the View and trigger the Service layer (e.g., 
UserController, CustomerController). 
4.7 DAO Pattern Usage 
The Data Access Object (DAO) pattern is used to abstract SQL code from the rest of the 
project. This allows for changes in the database schema to require updates only in the relevant 
DAO class, rather than throughout the entire project. 
5. Desktop Application (Phase 1) 
5.1 Swing Structure (User Interface Structure) 
The application is developed using Java Swing and AWT libraries. 
Clean Code Structure:  
● Each screen is designed as a separate JFrame class, while common components 
(Custom Buttons, Rounded Borders) are modularized as reusable classes. 
Modern UI:  
● Standard gray Swing appearance is avoided, and Graphics2D is used to 
implement Dark Mode and gradient designs. 
14 
5.2 CRUD Screens (Management Screens) 
Core data operations (Create, Read, Update, Delete) are performed through the 
following screens: 
Product Management:  
● A screen where admins and staff can add, delete, and update product stock. 
Customer Market:  
● An interactive screen where customers can list products (Read) and place 
orders (Create). 
Supplier Panel:  
● A management interface where suppliers can approve pending requests and 
increase stock. 
5.3 JDBC Integration (Database Integration) 
The application communicates with a MySQL database via JDBC (Java Database 
Connectivity). 
Connection Management:  
● A centralized connection pool is managed through the DBConnection class. 
Data Mapping:  
● Rows retrieved from the database (ResultSet) are converted into corresponding 
Java Model classes (e.g., Product, User) and transferred to the UI layer. 
15 
 
This screen allows the user with admin role to add, delete, and track products. 
 
 
This screen allows users in the Staff role to track product stock levels and request additional 
stock if it decreases. 
 
 
 
16 
 
 
 
 
This is a page where a user with the staff role can view statistics. They can see the most 
requested products, the number of different products, and the month with the most orders. 
 
 
Employees in the Staff role can track the logs of requests they have created from different 
suppliers and view their current status from this screen. 
 
17 
 
 
 
Employees in the "Staff" role use this screen to create a request for a product with low stock. 
You can select any of the listed options and send a request for it. 
 
This screen will be viewed by customers in the "customer" role. Products added by the admin 
are in a "purchasable" status and can be ordered as long as they are not out of stock. 
 
18 
 
 
 
 
 
 
19 
 
 
6. Database Design 
6.1 ER Diagram 
20 
 
6.2 Table structures 
21 
 
 
 
 
 
 
6.3 SQL scripts 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
22 
 
 
 
 
 
 
 
 
23 
 
 
7. Testing & Validation  
7.1 Test Scenarios 
The system was validated using JUnit 5 unit tests targeting the DAO layer. The primary test 
class customerDAOTest focuses on the processSale() method within the customerDAO class, 
which is responsible for placing orders and reducing stock simultaneously. 
Test Setup (@BeforeAll): Before each test run, the test environment is initialized as follows. 
Foreign key checks are temporarily disabled (SET FOREIGN_KEY_CHECKS = 0) to allow 
clean table drops. The Products and Orders tables are dropped and recreated from scratch to 
ensure a predictable, isolated test state. A seed product is inserted: ProductId=1, 
ProductName="Kalem", UnitPrice=5.0, CurrentStock=100, MinimumStockLevel=10. This 
guarantees that test results are not affected by any pre-existing database data. 
Test Case 1 – Successful Sale Processing (testProcessSale): A sale is triggered for 
ProductId=1, UserId=1, Quantity=10, TotalPrice=50.0. The test asserts three conditions: (1) 
the processSale() method returns true, confirming the transaction was accepted by the 
business logic layer; (2) the CurrentStock of the product decreases from 100 to 90, confirming 
the stock deduction was written to the database; (3) exactly one record appears in the Orders 
table for the given UserId and ProductId, confirming the order was persisted correctly. 
7.2 Sample Outputs 
Test Method 
Input 
Expected Output 
Result 
testProcessSale 
Stock Verification 
Order Record 
Verification 
UserId=1, ProductId=1, Qty=10, 
Price=50.0 
ProductId=1 after sale 
Orders WHERE UserId=1, 
ProductId=1 
Returns true 
PASSED 
CurrentStock = 90 PASSED 
COUNT = 1 
PASSED 
24 
All three assertions passed successfully during test execution, confirming that the 
processSale() method correctly performs both the stock update and the order insertion as an 
atomic operation. 
7.3 Error Handling Cases 
The following error scenarios are handled across the service and DAO layers: 
Invalid Quantity: The CustomerServiceImpl enforces that quantity must be greater than 0 
and must not exceed 5 units per transaction. Requests violating these constraints are rejected 
before reaching the DAO layer, preventing unnecessary database calls. 
Insufficient Stock: If the requested quantity exceeds CurrentStock, the processSale() method 
returns false and no order record is written. The UI layer then notifies the user via a 
JOptionPane dialog indicating insufficient stock. 
Database Connectivity Errors: JDBC operations are wrapped in try-with-resources blocks. 
In the event of a SQLException (e.g., connection timeout, unknown column), the exception is 
caught and propagated upward, preventing silent failures. 
Foreign Key Violations: During test setup, SET FOREIGN_KEY_CHECKS = 0 is used 
explicitly to manage table teardown. In production, foreign key constraints between Orders, 
Products, and Users tables are enforced by the database, preventing orphaned records. 
Empty or Invalid User Credentials: The UserServiceImpl validates that email and password 
fields are non-empty and that the password meets the minimum 4-character requirement 
before any database query is executed. 
25 
8. Discussion  
8.1 Reusability Analysis 
Modular Structure: Since the project utilizes Interfaces and the DAO pattern, switching from 
MySQL to another database in the future would only require changes in the DAO layer. There 
is no need to rewrite the entire system. 
Custom UI Components: Classes such as RoundedBorder and Custom Button are reused 
across all screens (Login, Register, Admin), preventing code duplication and improving 
maintainability. 
8.2 Challenges Faced 
Database Connectivity: During JDBC connections between Java and MySQL, errors such as 
"Table not found" or "Unknown column" (e.g., due to case sensitivity) initially slowed down 
development. 
UI/UX in Swing: Transforming standard Java Swing components into a modern, dark-themed 
interface, particularly with custom Graphics2D drawings, was technically the most 
time-consuming task. 
Role-Based Access: Implementing the logic to direct each user to the appropriate screens 
based on their role and to prevent unauthorized operations was the most complex part of the 
business logic. 
9. Conclusion  
9.1 Achievements 
Role-Based System: A fully compatible and authorized system for Admin, Staff, Supplier, and 
Customer roles has been successfully implemented. 
Automation: Instead of manual stock tracking, a structure has been created where purchases 
and approval processes automatically update the stock. 
26 
UI/UX Transformation: The standard Swing interface has been transformed into a 
professional-looking design with Dark Mode and user-friendly notifications. 
Layered Architecture: Code is organized according to industrial standards by separating it into 
Controller, Service, and DAO layers. 
9.2 Future Improvements 
Advanced Reporting: Dynamic charts showing sales trends and a PDF reporting module can 
be added. 
Cloud Integration: The database can be migrated from a local setup (localhost) to a 
cloud-based system (AWS or Azure). 
Security Enhancement: Stronger hashing algorithms (e.g., BCrypt) for passwords and 
two-factor authentication (2FA) can be integrated. 
Web/Mobile Client: A web or mobile interface can be added using the existing Service layer. 
References (APA Format)  
Eckstein, R., Loy, M., & Wood, D. (1998). Java swing. O'Reilly & Associates, Inc.. 
Java Web Development. Artificial Intelligence and Computing Innovations, 6(1). 
Ramirez, P. (2026). Performance Analysis of Front-End and Back-End Separation in 
Tatlı, E. İ. (2011). JAVA’DA GÜVENLİ YAZILIM GELİŞTİRME. 
Silberschatz, A., Korth, H. F., & Sudarshan, S. (2002). Database system concepts (Vol. 
5). New York: McGraw-Hill. 
27 