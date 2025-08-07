# Military Reserve Job Recruitment System

<ul>
<h2>submitting:</h2>
<li><b>Yochai Benita- </b> yochaiben@edu.jmc.ac.il</li>
<li><b>Benjamin Rosin-</b> benjaminro@edu.jmc.ac.il</li>
</ul>

<h3>[check the video](https://drive.google.com/file/d/1gjO5F0n2UB10wzytdplqQxuovcHlqikq/view?usp=sharing)</h3>


## General Functionality

This is a Spring Boot web application that serves as a job recruitment platform for military reserve positions. The system connects military units with reservists, allowing commanders to post job positions and reservists to apply for them. The application includes user registration, authentication, position management, application processing, and interview scheduling.

## Main Pages and Their Goals

### 1. **Home Page (`/`)**
- Landing page with system overview
- Call-to-action for registration
- Feature highlights and benefits

### 2. **Login Page (`/login`)**
- User authentication
- Error handling for failed login attempts
- Session management

### 3. **Registration Page (`/register`)**
- New user registration with role selection
- Military ID document upload
- Form validation and error handling

### 4. **Dashboard (`/dashboard`)**
- User's personal dashboard
- Display of submitted applications
- Relevant interviews and their status
- Real-time updates via polling

### 5. **Positions Page (`/positions`)**
- List of all active job positions
- Search and filtering capabilities
- Application submission interface

### 6. **Position Details (`/position/{id}`)**
- Detailed view of specific position
- Application management for commanders
- Status updates and applicant tracking

### 7. **Application Details (`/application/{id}`)**
- Detailed view of job application
- Interview scheduling for commanders
- Status management and communication

### 8. **Admin Dashboard (`/admin/dashboard`)**
- User management and approval system
- System overview and statistics
- Document verification interface

### 9. **Add Position (`/positions/add`)**
- Position creation form for commanders
- Job requirements and location specification
- Status management

### 10. **About Page (`/about`)**
- System information and contact details

## Database Beans: Classes and Relations

### Core Entities

#### 1. **AppUser** (User Management)
- **Fields**: id, username, firstName, lastName, password, email, militaryIdDoc, about, role, registrationStatus
- **Relations**: 
  - One-to-Many with Position (as publisher)
  - One-to-Many with Application (as applicant)
- **Enums**: Role (ADMIN, COMMANDER, RESERVIST), RegistrationStatus (PENDING, APPROVED, BLOCKED)

#### 2. **Position** (Job Positions)
- **Fields**: id, jobTitle, location, assignmentType, description, requirements, status, publisher
- **Relations**:
  - Many-to-One with AppUser (publisher)
  - One-to-Many with Application
- **Enums**: PositionStatus (ACTIVE, CANCELED, FULFILLED, FROZEN), LocationRegion

#### 3. **Application** (Job Applications)
- **Fields**: id, applicant, position, applicationDate, status
- **Relations**:
  - Many-to-One with AppUser (applicant)
  - Many-to-One with Position
  - One-to-Many with Interview
- **Enums**: ApplicationStatus (PENDING, APPROVED, REJECTED, CANCELED)

#### 4. **Interview** (Interview Management)
- **Fields**: id, application, interviewDate, location, status, notes, rejectionReason, interviewSummary, isVirtual, jitsiLink
- **Relations**:
  - Many-to-One with Application
- **Enums**: InterviewStatus (SCHEDULED, CONFIRMED, REJECTED, COMPLETED, CANCELED)

### Supporting Enums

- **LocationRegion**: NORTH, VALLEY, CENTER, JERUSALEM_AND_SURROUNDINGS, JUDEA_AND_SAMARIA, GAZA, SOUTH
- **Role**: ADMIN, COMMANDER, RESERVIST
- **RegistrationStatus**: PENDING, APPROVED, BLOCKED
- **PositionStatus**: ACTIVE, CANCELED, FULFILLED, FROZEN
- **ApplicationStatus**: PENDING, APPROVED, REJECTED, CANCELED
- **InterviewStatus**: SCHEDULED, CONFIRMED, REJECTED, COMPLETED, CANCELED

## Technology Stack

- **Backend**: Spring Boot 3.5.0
- **View Engine**: Thymeleaf
- **Database**: MySQL 8.0
- **Security**: Spring Security with BCrypt
- **ORM**: Spring Data JPA
- **Frontend**: Bootstrap 5, JavaScript
- **Build Tool**: Maven

## Features Implemented

### ✅ **Spring Boot MVC Requirements**
- Full MVC structure with Thymeleaf templates
- Server-side logic with limited JavaScript
- 10+ major pages with dynamic content
- Session management for user state

### ✅ **Database & JPA**
- MySQL database named "ex4"
- 4 repository beans with relations
- JPA entities with proper relationships
- Data persistence for all application state

### ✅ **Spring Security**
- User authentication and authorization
- Role-based access control
- Custom user registration
- Password encryption with BCrypt

### ✅ **Dependency Injection & Beans**
- Proper `@Autowired` usage
- Service layer injection
- Configuration beans
- No global static objects

### ✅ **Session Management**
- Session-based user state
- Remember recent searches
- User preferences storage
- Security session handling

### ✅ **REST API**
- Custom REST endpoints for AJAX calls
- JSON responses for dynamic updates
- Polling mechanism for real-time updates

## Known Bugs

1. **Admin Credentials**: Default admin credentials should be changed in production

## SQL Import File

We have included a `database_setup.sql` file in the repository for easy database setup. This file contains:
- Database schema creation
- Initial admin user creation
- Sample data for testing
- Proper table relationships and constraints

## Setup Instructions

### Prerequisites
- Java 24
- MySQL 8.0
- Maven 3.6+

### Database Setup
1. Create MySQL database named "ex4"
2. Update database credentials in `application.properties`
3. Import the provided SQL file: `database_setup.sql` (included in the repository)
4. Run the application - tables will be auto-created if not already present

### Running the Application
1. Clone the repository
2. Navigate to project directory
3. Run: `mvn spring-boot:run`
4. Access application at: `http://localhost:8080`

## Credentials

### Default Admin Account
- **Username**: admin
- **Password**: admin123
- **Role**: ADMIN

### User Roles
- **ADMIN**: Full system access, user management
- **COMMANDER**: Can create positions, manage applications, schedule interviews
- **RESERVIST**: Can apply for positions, view applications, attend interviews

## Additional Information

- **Language**: Hebrew (RTL) with English backend
- **File Upload**: Military ID documents stored as BLOB in database
- **Real-time Updates**: AJAX polling for dashboard and position updates
- **Error Handling**: Custom error pages and global exception handler
- **Validation**: Server-side validation with custom validators

## Project Structure

```
src/main/java/com/example/ex4/
├── config/          # Configuration classes
├── controllers/     # MVC controllers
├── dto/            # Data transfer objects
├── models/         # JPA entities
├── repositories/   # Data access layer
├── services/       # Business logic
└── validation/     # Custom validators

src/main/resources/
├── static/         # CSS, JS, images
└── templates/      # Thymeleaf templates
```

The application successfully demonstrates all required Spring Boot technologies and provides a complete, functional job recruitment system for military reserve positions.

