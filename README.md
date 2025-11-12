# MusicApp

MusicApp is a web application for managing albums, reviews, and user preferences, including email notifications. The system is designed with a microservices architecture, including a main application and an email notification service.

---

## Technology Stack

- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Database:** MySQL
- **Frontend:** Thymeleaf
- **Microservices & Communication:** REST + Feign Client
- **Email Service:** Spring Mail (SMTP)
- **Build Tool:** Maven
- **Validation:** Jakarta Bean Validation

---

## Architecture Overview

MusicApp consists of two main applications:

- **Main Application (`MusicApp`)**
    - Handles users, albums, reviews, favourites
    - MVC controllers for web pages
    - REST integration with the Email Notification Service
- **Email Notification Service (`emailNotification`)**
    - Handles user email preferences
    - Sends email notifications
    - REST API for creating preferences, fetching preferences, and sending emails

---

## Functionalities

The application implements the following functionalities:

- **Create Album**  
   - Only users with role `ADMIN` or `ARTIST` can create new albums.
   - Sends email notifications to users who favourited the artist.

- **Edit Album**  
   - Artists or Admins can update album details.

- **Change Album Visibility**  
   - Artists or Admins can toggle album status (`VISIBLE` / `INVISIBLE`).

- **Add Review**  
   - Users can add delete their own reviews for albums.
   - Notifies album owner via email.

- **Report Review**  
   - Users can report inappropriate reviews.
   - Admins can restore or delete reported reviews.

- **Add / Delete Favourite Album**  
   - Users can mark albums as favourite or remove them from favourites.

- **Email Notifications**  
   - Users can enable/disable notifications.
   - Emails sent only if notifications are active.

- **User Management (Admin Only)**  
   - View all users
   - Change roles or status

---

## Data Models / Entities

### Main Application

- **User** – stores authentication, profile, and role info.
- **Album** – stores album details, status, associated artist.
- **Review** – stores user reviews and report information.
- **FavouriteAlbum** – links users with their favourite albums.

### Email Notification Service

- **EmailPreference** – stores per-user notification preferences.
- **Email** – stores email logs and delivery status.

---

## Setup and Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- Git

### Installing

1. Download the app:
   ```bash
   https://github.com/kirilkitanov/MusicApp.git
   ```
2. Open both projects in IntelliJ IDEA (MusicApp and emailNotification).
- Launch IntelliJ IDEA.
- Select File → Open and navigate to the MusicApp folder.
- Click OK to open the project.
- Then open the emailNotification.

3. Wait for project dependencies to download
- IntelliJ will automatically detect it is a Maven project.
- It will start downloading all required dependencies.
- Wait until the process finishes (you can see progress in the bottom status bar).

4. Run the Main Application
- Open the main class with the @SpringBootApplication annotation and named Application.
- Click the Run button (green arrow) in IntelliJ.
- Alternatively, right-click the class → Run 'Application'.

5. Access the application
- Main Application usually runs on http://localhost:8080
- Email Notification runs on http://localhost:8081
- Make sure both services are running to use email notifications.

---
## Help

### Common issues:

- **Database connection error:**
Make sure MySQL is running and credentials in application.properties are correct.

- **Email not sent:**
Check that your Gmail SMTP credentials are valid and that “Allow less secure apps” (or App Password) is configured.

- **Port already in use:**
Stop any process using ports 8080 or 8081.
---
## Authors

Kiril Kitanov — Project Author and Owner

[GitHub Profile](https://github.com/kirilkitanov)

---
## Version History

- **v.0.2**
   - Added Email Notification microservice
   -  Enhanced Album Management features
   -  Bug fixes and optimizations

- **v.0.1**
   - Initial release with Albums, Reviews, and Favourites

