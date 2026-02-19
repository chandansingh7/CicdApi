# POS System - CI/CD Demo

A simple Point of Sale (POS) system built with Java and Gradle, featuring CI/CD pipeline with GitHub Actions.

## Features

- Product management
- Shopping cart functionality
- Checkout process
- Inventory management
- Unit tests with JUnit 5
- CI/CD pipeline with GitHub Actions

## Prerequisites

- Java 11 or higher
- No Gradle installation required - the project includes Gradle Wrapper (`gradlew`)

## Project Structure

```
CicdApi/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── pos/
│   │               ├── Product.java
│   │               ├── CartItem.java
│   │               ├── Cart.java
│   │               ├── POSSystem.java
│   │               └── POSApplication.java
│   └── test/
│       └── java/
│           └── com/
│               └── pos/
│                   ├── ProductTest.java
│                   ├── CartTest.java
│                   └── POSSystemTest.java
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── .github/
│   └── workflows/
│       └── ci.yml
├── .gitignore
└── README.md
```

## Building the Project

```bash
# Compile the project
./gradlew clean compileJava

# Run tests
./gradlew test

# Build the application (includes tests)
./gradlew build

# Run the application
./gradlew run

# Or run the JAR directly
java -jar target/libs/cicdpos-1.0.0.jar
```

## Running Tests

```bash
./gradlew test
```

## CI/CD Pipeline

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. Checks out the code
2. Sets up JDK 11
3. Builds the project with Gradle
4. Runs all unit tests
5. Uploads the JAR artifact
6. (On `main` branch) Deploys the built artifact to Azure Web App

The pipeline runs automatically on:
- Push to `main`, `master`, or `develop` branches
- Pull requests to `main`, `master`, or `develop` branches

### Azure deployment setup (free tier)

1. In the Azure Portal, create a **Free-tier App Service (Linux, Java SE)**, e.g. `cicdpos-demo-app`.
2. In the App Service, go to **Overview → Get publish profile** and download the XML file.
3. In your GitHub repo, go to **Settings → Secrets and variables → Actions** and add:
   - `AZURE_WEBAPP_NAME` = your app name (e.g. `cicdpos-demo-app`)
   - `AZURE_WEBAPP_PUBLISH_PROFILE` = contents of the publish profile XML.
4. Push to the `main` branch; GitHub Actions will build, test, and deploy the latest JAR to your Azure Web App.

## Usage Example

```java
POSSystem pos = new POSSystem();

// Add items to cart
pos.addToCart("P001", 1); // Laptop
pos.addToCart("P002", 2); // Mouse

// Checkout
double total = pos.checkout();
System.out.println("Total: $" + total);
```

## License

This is a demo project for CI/CD purposes.
