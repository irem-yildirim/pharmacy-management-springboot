Advanced Java for

Information

Management

Week 1: OOP Foundations, Agile, Architecture, JVM

Content
Why Is This Course Different from Other Java Courses?
What Is an Information System?
Object-OrientedThinking
Whatis a Class
Encapsulation
Student Class (canlı kod)
LayeredArchitecture
Service Layer
DAO Layer
UI → Service → DAO akışı
Agile & User Stories
JVM, JDK, JRE
Tools WeWill Use

Why Is This
Course

Different from

Other Java

Courses?

Course Approach

In this course, we will develop a real Information
Management System using Java.
What will we achieve by the end of the semester?

Desktop MVC application using Swing
JDBC and DAO layers
SOLID principles and Design Patterns applied
Web MVC application using Servlet/JSP
Security, validation, and testing processes
A layered, professional Information Management
System
The Approach of This
Course:
A real-world
Information
Management System

will be developed
using Java.
How are professional software systems
designed?
How to set up layered architecture?

How do desktop and web applications
share the same business layer?
How does object-oriented thinking
transform into a true knowledge system?
What will we
have at the end
of the term?
Desktop MVC applicationwithSwing
JDBC andDAO layer
StructurewithSOLID andDesign
Patternsimplemented
Web MVC applicationwithServlet/JSP
Security, validation, andtesting
processes
A layered, professionalInformation
Management System
What Is an Information System?
An Information System is a software system that collects, processes, stores, and
presents data in a meaningful way to users.

An Information System consists of four main components:

Data
Business Logic
User Interface
Database
Basic

Components

of an

Information

System

Component Explanation

Data

Student, course, user,
records
Business Logic

Who can add? Who can
delete? Rules
User Interface

The screens the user
sees
Database Where the data is stored

Real-Life Information

System Examples

❑Student Information System (SIS)

❑Hospital Management System

❑Library Automation

❑CRM (Customer Management)

❑Stock / Inventory System

In this course, you will develop a system similar
tothese.

How does an information system work?

User Interface

Business

Rules

Database

Knowing Java ≠

Being able to
develop
information
systems
The objective of
this course is:
to learn how to
develop
information
systems using
Java.
You will develop a complete information management system that includes
these four components:

Desktop version

Web version

Samebusinessrules

Samedata model

3. Object-Oriented Thinking

Object-oriented programming is not a coding technique, but a way
of thinking about translating the real world into software.
Real World → Software

Whatever exists in the real world also exists in software:

Real World Software

Student Student^ class

Class Course^ class

User User class

Record Enrollment class

Procedural Thinking
“Add students, delete students, list students”
It is function-oriented.
Object-Oriented Thinking
“A student is an object. It has its own
behaviors.”
It is object-oriented.
The Fundamental Question of OOP

"What objects are
in this system?"
This question is
answered before
writing any code.
Possible Objects
in an Information
System
Student Course User

Record Report

How will we

use it in this

lesson?

First we will define the objects.
Then we will convert these
objects into classes.
Then we will set up the system.
Design the objects first, then write the code.
According to Grady Booch:
“Object-oriented design is a method of design encompassing the process of object-oriented decomposition.”

What is Class?
A class is the architectural blueprint that defines how objects are
created.

An object is the actual instance of this blueprint.
Real World Software
Apartment project Class
Apartment in the building Object
What does Class define?
A class defines two things:

Attributes / Fields
Methods / Behaviors
Ex: Student

Attribute Method

id getName()

name enrollCourse()

public class Student {
int id;
String name;
Student(){
name=«noName»;
id=0;
}
void printName() {
System.out.println(name);
}
}
Student
+id:int
+name:String
Student()
printName() :void
UML
Java
codes
Creating an Object
Student s = new Student();

s.name = "Ali";

s.printName();

Student
+id:int
+name:String
Student()
printName() :void
S is a Student type Object
Creation of S object in main method
This week:
We will start designing classes like

Student
Course
User
5. What is Encapsulation?
Encapsulation is the process of hiding data from the outside
world and allowing only controlled access.
Bad Design (No Encapsulation)

public class Student {
public String name;
}
Correct Design (Encapsulation
includedpublic class) Student {
private String name;
public String getName () {
return name;
}
public void setName (String name) {
this.name = name;
}
}
Why Encapsulation?
Reason Explanation
Security Data is protected.
Control It's clear who can change what.
Flexibility
Even if the interior structure
changes, the exterior remains
unaffected.
Real World Analogy
ATM machine:
You can't see inside
You only use the buttons
Key point: Fields are private, methods are public.
Student Class
public class Student {

private int id;
private String name;

public Student(int id, String name){
this.id = id;
this.name = name;
}

public String getName(){
return name;
}
}

Code without encapsulation is not professional software.
System Design Sequence
Find the
objects
Specify its
characteristics.
Specify its
characteristics.
Write code

6. Student Class (Live Code)
Creating Objects...
What other classes will be included in this
system?
7 .What is the

Layered

Architecture

?

Layered architecture is a
method of dividing a software
system into parts according to
their tasks.
Each layer has a single
responsibility.
UI → Service → DAO → Database
Layer Task
UI (User Interface) Interactionuser^ with^ the^
Service Business rules, logic
DAO (Data Access Object) Database operations
Database Data storage
The UI never directly accesses
the database. All
communication occurs through
layers.
Thanks to this structure:
The code doesn't become
complex.
We don't rewrite anything when
transitioning to the web.
We establish a professional
software architecture.
Large systems are composed of small, ordered layers.
Katmanları Kod Üzerinde Gösterelim
com.project
model
service
dao
ui
Package Structure

Where should the Student be placed?
To model package
8. What is the Service Layer?
The service layer is the layer where the
system's business rules reside.

This layer:

Stands between the UI and the DAO
Makes decisions
Enforces rules
The UI only calls the service.
Layered architecture cannot be built
without a service layer.

task Explanation
Enforcing business rules Who can add someone? How can someone be added?
Managing requests from
the UI. Verification, control
Calling the DAO Saving the data
Simple Service Example
package service;

import model.Student;

public class StudentService {

public void addStudent(Student s) {
System.out.println("Business rule check...");
System.out.println("Student added: " + s.getName());
}
}

Call from UI to Service
Student s = new Student(1,"Ali","ali@mail.com");
StudentService service = new StudentService();
service.addStudent(s);
What is the DAO (Data Access Object) Layer?
The DAO layer is the layer that
communicates with the database.
Data access in the system is only
done through this layer.
Correct Design: SQL is located
within the DAO.
Task Explanation
Adding data insert
Data deletion delete
Data update update
Data retrieval select
Simple DAO Example (No DB yet, simulation)
package dao;

import model.Student;

public class StudentDAO {

public void save(Student s){
System.out.println("Saving student to database: " + s.getName());
}
}

Service’in DAO’yu Kullanması
public class StudentService {
StudentDAO dao = new StudentDAO();
public void addStudent(Student s){
System.out.println("Checking rules...");
dao.save(s);
}
}
?
If we switch from MySQL to Oracle tomorrow, which layer
will change?

If we switch from MySQL to Oracle tomorrow, which layer
will change?

DAO(Data Access Object) Layer
UI → Service → DAO
Main (UI) StudentService
(Service)
StudentDAO
(DAO)
Database
Student s = new Student( 1 , "Ali", "ali@mail.com");
StudentService service = new StudentService();
service.addStudent(s);

public void addStudent(Student s){
System.out.println("Checking rules...");
dao.save(s);
}
public void save(Student s){
System.out.println("Saving to database: " + s.getName());
}
Layered architecture means that classes are in the right place.
11. Agile & User Stories
Agile : A method of developing software by breaking it down into
smaller parts.

How will we use Agile in this lesson?
Every week:
We will add a new layer to the system.
User Story Example
“As an admin, I want to add a student so that I can manage records.”
12. JVM, JDK, JRE
JVM : A virtual machine that runs Java code.
Component Task
JVM Runs Java code
JRE Runtime environment
JDK Development tools
Advanced Java for

Information

Management

Week 2 : Inheritance& Polymorphism

Content

Whatis Inheritance?

Whatis Polymorphism?

“is-a” connection

Expandingthesystem—StudentExample

Inheritance?

Polymorphism?

“is-a”

connection?

Expanding the system— Student Example

Advanced Java for

Information

Management

Week 3 : UML & Domain Modeling

Content
WhyAreWeDoingThis
Whatis a “Domain”?
Domain Language (UbiquitousLanguage)
Domain VsTechnical Concepts
Whatis a DomaınModel?
Domain Model ≠ Database Model
Whatis Uml?
WhyweusetheUML?
WhichUML in thisclass?
Basics of theUmlClass Dıagram
Visibility(+ / -/ #)
InheritanceUML Representation
Association(İlişki)
Multiplicity(1.., 0..1)
OurCurrentModel
UML Draft(Conceptual)*

Why Are We Doing This

Domain? Domain, “neyi çözmeye
çalışıyoruz?” sorusunun
cevabıdır.

Domain Language (Ubiquitous

Language)

Domain vs Technical Concepts

Domain concepts (business world):
Student, Course, Enrollment
Technical concepts:
Controller, DAO, Service, Database
Domain ≠ technical layer. The domain is the "heart" of the system.
Domaın Model?
Domain Model = Modeling domain
concepts as classes and
relationships
Question:

What entities are in my system and
how are they related? Example:
Student “enrolls”
Course “has students”
Instructor “teaches courses”
Domain Model ≠

Database Model

Domain Model: The business logic of the software
DB Model: Tables and relationships
The domain model is often richer than the database.
Example:
A domain model requires a class called Enrollment
A database model can only have a "student_course" join table
Uml?

UML = Unified Modeling Language. A standard for illustrating software
design with drawings.
Purpose:
Everyone should speak the same language.
UML is not a "code," it is a "communication tool."
Why we use the UML?

What is UML Used For? It facilitates team communication.
It helps catch errors before coding.
It provides control as the project grows.
It serves as documentation.
Writing code without drawing UML is like building a building without a plan.
Which UML in this class?

UML has many diagrams, but we will
mostly be using the :
Class Diagram
Because:

Shows Java classes and their relationships.
Basics of

the Uml

Class

Dıagram

Visibility (+ / - / #)

Inheritance

UML

Representation

Triangle arrow:

Student → Person
Student is a Person
Association
(İlişki)
Student —Course
relationship.

Example:

Student takes Course.
Course has Students.
In UML, this is represented

by a line.

Multiplicity (1.., 0..1)*

Our Current

Model

UML Draft (Conceptual)

CourseClass (model)

package model;
public class Course {
private String courseCode;
private String courseName;
private int credit;
public Course(String courseCode, String courseName, int credit) {
this.courseCode = courseCode;
this.courseName = courseName;
this.credit = credit;
}
public String getCourseCode() {
return courseCode;
}
public String getCourseName() {
return courseName;
}
public int getCredit() {
return credit;
}
@Override
public String toString() {
return courseCode + " - " + courseName + " (" + credit + " ECTS)";
}
}
EnrollmentClass (model): Student–Course relation
package model;
import java.time.LocalDate;
public class Enrollment {
private Student student;
private Course course;
private LocalDate enrollDate;
private String status; // e.g., "ACTIVE", "DROPPED"
public Enrollment(Student student, Course course, LocalDate enrollDate, String status) {
this.student = student;
this.course = course;
this.enrollDate = enrollDate;
this.status = status;
}
public Student getStudent() {
return student;
}
public Course getCourse() {
return course;
}
public LocalDate getEnrollDate() {
return enrollDate;
}
public String getStatus() {
return status;
}
@Override
public String toString() {
return student.getName() + " enrolled in " + course.getCourseCode() + " on " + enrollDate + " [" + status + "]";
}
}

StudentHas Enrollments(model):
package model;
import java.util.ArrayList;
import java.util.List;
public class Student extends Person {
private double gpa;
// NEW: Student has enrollments
private List enrollments = new ArrayList<>();
public Student(int id, String name, String email, double gpa) {
super(id, name, email);
this.gpa = gpa;
}
// NEW: add enrollment
public void addEnrollment(Enrollment enrollment) {
enrollments.add(enrollment);
}
// NEW: list enrollments
public List getEnrollments() {
return enrollments;
}
public double getGpa() {
return gpa;
}
@Override
public String getInfo() {
return super.getInfo() + " | GPA: " + gpa;
}
}

InstructorTeachesCourses (model):

package model;
import java.util.ArrayList;
import java.util.List;
public class Instructor extends Person {
private String department;
// NEW: Instructor teaches courses
private List<Course> givenCourses = new ArrayList<>();
public Instructor(int id, String name, String email, String department) {
super(id, name, email);
this.department = department;
}
// NEW: add course
public void addCourse(Course course) {
givenCourses.add(course);
}
// NEW: list courses
public List<Course> getGivenCourses() {
return givenCourses;
}
@Override
public String getInfo() {
return super.getInfo() + " | Dept: " + department;
}
}
UI/Main.java
package ui;
import model.*;
import java.time.LocalDate;
public class Main {
public static void main(String[] args) {
// Courses
Course c1 = Course c2 = newnew Course("MIS101", "Course("MIS202", "Object IntroductionOriented to MIS", 6); Programming", 5);
// Instructor
Instructor instructor = new Instructor(10, "Dr. Ayşe Kaya", "ayse@uni.edu", "MIS");
instructor.addCourseinstructor.addCourse(c1);(c2);
// Student
Student student = new Student(1, "Ali Veli", "ali@mail.com", 3.4);
// Enrollment (Student takes courses)
Enrollment e1 = new Enrollment(student, c1, LocalDate.now(), "ACTIVE");
Enrollment e2 = new Enrollment(student, c2, LocalDate.now(), "ACTIVE");
student.addEnrollment(e1);
student.addEnrollment(e2);
// OUTPUTS
System.out.println("=== INSTRUCTOR INFO ===");
System.out.println(instructor.getInfo());
System.out.printlnfor (Course c : instructor.getGivenCourses("Courses taught:"); ()) {
System.out.println(" - " + c);
}
System.out.printlnSystem.out.println("(student.getInfo\n=== STUDENT INFO ===");());
System.out.println("Enrolled courses:");
for (Enrollment e : student.getEnrollments()) {
System.out.println(" - " + e);

(^) }}
}

Advanced Java for

Information

Management

Week 4 : Creational Design Patterns

Advanced Java for

Information

Management

Week 5 : Swing& Desktop MVC

Swing & Desktop MVC (Demo Application)

This week's goal:
View (Swing UI): User interaction
Controller (Service): Business rules / flow
Model: Student, Course, Enrollment (Week2–3)
(Week4) Using Builder + Factory within the UI
Advanced Java for

Information

Management

Week 6 : JDBC, DAO & LayeredArchitecture

1st
way

2nd
way

Reload
again

prev
now

same

To add the editStudent and removeStudent functions while remaining faithful to the
MVC architecture, we need to make updates in the
DAO,
Service,
and UI layers.
editStudent??
removeStudent??

2 more
buttons

Before this step
add id attribute to
Student class

Remove/update student by e-mail attribute:

Advanced Java for

Information

Management

Week 7 : JUnit, TDD, and Refactoring


org.junit.jupiter
junit-jupiter-api
5.10.0
test

StudentDAO.java (New Methods...)

EnrollmentService.java (New Methods...)

Advanced Java for

Information

Management

Week 10 : Web Architecture and Servlets

GET... POST...

Swing JFrame HTTP Servlet

HTTP is stateless!...

What happens
if 100 people

click the 'Save'
button at the

same time?

What happens
if 100 people

click the 'Save'
button at the

same time?

Where to download it?
From the official Apache Tomcat website: tomcat.apache.org
Apache Tomcat

Tomcat 10.x veya 11.x (Alpha)
same

same
Controller Layer (StudentServlet.java)

Location: src/main/java/org/example/web
Controller Layer (StudentServlet.java)

Location: src/main/java/org/example/web
View Layer (index.html)

Location: src/main/webapp
View Layer (index.html)

Location: src/main/webapp
web.xml

Location:
web.xml

Important: Introduce This Folder to IntelliJ

Step 1: Web Project Structure (Refactoring)
In a standard Maven project, the src/main/webapp folder should be created for web features, and
the WEB-INF/web.xml file (or the modern @WebServlet notation) should be added to it.

Automatic Web.xml Generation
Open Project Structure (Ctrl + Alt + Shift + S).
From the left menu, select the Facets tab.
If it's not in the list, click the + button and select
Web. On the right side, in the Deployment Descriptors
section:
Here, click the + button to automatically create the
web.xml file in the correct folder (WEB-INF).
Advanced Java for

Information

Management

Week 11 : JSP, JSTL, andWeb MVC

The classic MVC (Servlet/JSP) structure previously planned in ourcourse notes is intended to
teach the "kitchen" and the fundamental logic of web development.

Spring Boot , on the other hand, allows usto implement this architecture at an industrial
standard, much faster and more securely.

Architectural Continuity: Spring Boot does not break away from the MVC (Model-View-
Controller) principles; instead, it applies these principles with stricter discipline.
JSP vs. Thymeleaf: Instead of traditional JSP, we use Thymeleaf , a modern template engine,
with Spring Boot.
evolution of web Technologies... ☺

Why Use Spring Boot? (Advantages)

Embedded Server: While classic MVC requires installing and configuring an external
Tomcat server, Spring Boot comes with an embedded Tomcat. The application runs

simply with a "Main" method.

Dependency Management (Starters): Structures like spring-boot-starter-web in a Maven
project eliminate the hassle of adding dozens of libraries individually and dealing with

version conflicts.

Auto-Configuration: Instead of writing pages of XML for database connection (MySQL)
and JPA settings, you can get the entire system up and running with just a few lines in

application.properties.

Power of Spring Data JPA: Instead of writing manual SQL queries, you automate
operations like " Save, Delete, FindAll " thanks to the JpaRepository interface.

Modern MVC Flow

Feature Traditional Web MVC (Servlet/JSP) Spring Boot Web MVC

Server
Requires external Tomcat
installation
Comes ready (Embedded)

Configuration Heavy XML or Annotation usage Minimalist (Auto-config)

Data Access JDBC / Manual SQL queries Spring Data JPA / Automatic CRUD

View JSP (Java Server Pages) Thymeleaf^ (Modern HTML
templates)

Spring Web: Required for setting up
the MVC structure, creating
Controllers, and RESTful services.
Spring Data JPA: Essential for easily
performing database operations
(CRUD) and using the
StudentRepositoryinterface.
MySQL Driver: Enables your
application to connect to the mis_db
database shown in the image.
Thymeleaf: A "View" engine used to
dynamically populate your HTML pages
with data from Java code (such as a
student list).
Code Analysis: Breakdown of Components

A. Model (Student.java): Represents the data structure. It
maps the Java object to the MySQL table using @Entity.
B. Repository Layer (Repository.java)
The data access layer. By extending JpaRepository, it provides
all CRUD methods without any manual implementation.
C. Controller Layer (StudentController.java)
The brain of the application. It handles HTTP requests,
communicates with the repository, and returns the appropriate
View (Thymeleaftemplate).
From Theory
to Practice:
JSP vs.
Thymeleaf
Main differences...

Feature Classic MVC
(JSP)
Spring Boot MVC
(Thymeleaf)
Syntax
Java code is
embedded within
HTML. (<% %>)
Standard HTML
attributes are
used.(th:text)
Browser
Compatibili
ty
It can only be
interpreted on
the server side.
Designers can preview
the file directly in the
browser.
Modernity
Old technology is
difficult to
maintain.
It is a modern, fast, and
secure template
engine.
Advanced Java for

Information

Management

Week 12 : BehavioralDesign Patterns

Advanced Java for

Information

Management

Week 13 : Validation, ExceptionHandling, and Security

Advanced Java for

Information

Management

Week 14 : CleanCodeand Refactoring

Test? SpringDocOpenAPI(Swagger UI) library: When you add this library to your project,
Spring Boot automatically creates a web page for you.
On this page, you can see all your API endpoints (StudentRestController), click on them
to send parameters, and test them.

1 - Unit Test Preparation
A unit test checks the correctness of only a specific method (usually the business logic in the
Service layer) without starting the entire application. To do this, update your test class under
src/test/java as follows:

2 - API Documentation (Swagger / OpenAPI): We need to add documentation notes to the code to
show how to use the API. This will make your swagger-uipage look much more professional.
StudentRestController.java (Documented Version)