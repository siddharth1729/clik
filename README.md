![Clik UrlShortner](/preview.png)

# Clik - A Self-Deployable URL Shortener

Clik is a modern, self-deployable URL shortener built with Spring Boot. It provides a secure and efficient way to create short URLs from long ones.

## Features

- URL shortening with custom short codes
- Click tracking and analytics
- User authentication and authorization
- Secure URL management
- MySQL database integration
- Thymeleaf templating for web interface

## Prerequisites

- Java 21 (or Java 17 for better compatibility)
- Maven 3.9+
- MySQL 8.0+
- SDKMAN! (recommended for Java version management)

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/clik.git
cd clik
```

### 2. Set Up Java Environment
Using SDKMAN!:
```bash
# Install Java 21
sdk install java 21.0.3-tem

# Use Java 21
sdk use java 21.0.3-tem
```

### 3. Configure Database
Create a MySQL database and update the `application.properties` file with your database credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clik
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── in/siddharthsabron/clik/
│   │       ├── config/        # Configuration classes
│   │       ├── controller/    # REST controllers
│   │       ├── dto/          # Data Transfer Objects
│   │       ├── entity/       # JPA entities
│   │       ├── repository/   # JPA repositories
│   │       ├── service/      # Business logic
│   │       └── ClikApplication.java
│   └── resources/
│       ├── static/          # Static resources
│       ├── templates/       # Thymeleaf templates
│       └── application.properties
└── test/                    # Test classes
```

## Dependencies

- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- Hibernate 6.2.5
- MySQL Connector
- Lombok 1.18.32
- Thymeleaf
- Spring Boot DevTools

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

- **Siddharth Sabron**
  - Email: siddharth.sabron@gmail.com
  - Website: https://siddharthsabron.in

## Acknowledgments

- Spring Boot team for the amazing framework
- The open-source community for various libraries and tools used in this project

## 🚀 Features

- **Fast URL Shortening**: Generate short, unique URLs quickly using a custom Base62 encoding system
- **Scalable Architecture**: Sharded database design to handle high-volume URL shortening
- **Click Tracking**: Track the number of clicks for each shortened URL
- **User Management**: Optional user accounts to manage and track URLs
- **Secure**: Built with Spring Security and follows security best practices
- **RESTful API**: Clean and well-documented API endpoints
- **Concurrent Processing**: Uses thread pools for non-blocking click tracking
- **Database Auditing**: Automatic tracking of creation and modification timestamps

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.4.2
- Spring Security
- Spring Data JPA
- MySQL Database
- Maven
- Hibernate (with Envers for auditing)
- Lombok

## 📋 Prerequisites

- JDK 21 or later
- MySQL 8.0 or later
- Maven 3.6 or later
- Git (optional)

## 🚦 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/siddharth1729/clik.git
cd clik
```

### 2. Configure Database

Create a MySQL database named `clik`:

```sql
CREATE DATABASE clik;
```

### 3. Configure Application Properties

The application uses profile-based configuration. For development, update `src/main/resources/profiles/dev.application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clik
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build and Run

```bash
# Using Maven Wrapper
./mvnw clean install
./mvnw spring-boot:run

# Or using Maven directly
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🔌 API Endpoints

### URL Shortening

#### Create Short URL (Anonymous)
```http
POST /api/shorten
Content-Type: application/json

{
    "longUrl": "https://example.com/very/long/url/that/needs/shortening"
}
```

Response:
```json
{
    "shortUrl": "http://localhost:8080/s/abc123"
}
```

#### Create Short URL (Authenticated User)
```http
POST /api/shorten
Authorization: Bearer {token}
Content-Type: application/json

{
    "longUrl": "https://example.com/very/long/url"
}
```

### User Management

#### Register New User
```http
POST /api/users/register
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "securePassword123"
}
```

## 🏗️ Architecture

### URL Shortening Process

1. **Hash Generation**: Long URLs are hashed using SHA-256
2. **Sharding**: URLs are distributed across shards based on the hash
3. **Unique ID Generation**: Uses a Snowflake-like algorithm for generating unique IDs
4. **Base62 Encoding**: IDs are encoded to create short, readable URLs
5. **Concurrent Processing**: Click tracking is handled asynchronously

### Database Schema

The application uses two main tables:
- `short_urls`: Stores URL mappings and click statistics
- `users`: Manages user accounts

### Traditional Deployment
1. Build the JAR file:
   ```bash
   ./mvnw clean package
   ```
2. Run the JAR:
   ```bash
   java -jar target/clik-0.0.1-SNAPSHOT.jar
   ```

## 📞 Support

For support, email siddharth.sabron@gmail.com or open an issue in the repository.

---

Made with ❤️ by Siddharth Sabron