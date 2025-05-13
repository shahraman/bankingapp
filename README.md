# Spring Boot Banking Application for a SG Kata purpose

## Overview

This is a SG Kata Spring Boot application demonstrating basic banking functionalities. Using REST APIs, service layers, data persistence with in Memory H2 database, unit tests and BDD testing with Cucumber.
The application use alse swagger UI to streamline REST API testing

**Current Features:**
*   Account creation
*   Depositing funds into an account
*   Withdrawing funds from an account
*   Checking account balance
*   Basic error and exception handling for common scenarios (e.g., insufficient funds, account not found)
*   Logging
*   Transaction management (ACID)

## Technologies Used

*   **Java 21** (or your preferred LTS version)
*   **Spring Boot 3.4.5
    *   Spring Web (for REST APIs)
    *   Spring Data JPA (if using a database)
    *   Spring Boot Starter Test
*   **Maven**  - for dependency management and build
*   **Cucumber** - for Behavior-Driven Development (BDD)
    *   `cucumber-java`
    *   `cucumber-spring`
    *   `cucumber-junit-platform-engine`
*   **JUnit 5** - for running tests
*   **H2 Database** (Optional, for in-memory testing or local development)
*   **Lombok** (Optional, for reducing boilerplate code)
*   **AssertJ** (Optional, for fluent assertions in tests)
  

## Project structure including layers/packages
* controller
* service
* repository
* model
* dto
* exception

## Unit test and BDD could be run with the command:
mvn test
## Run the application using: 
mvn spring-boot:run     or from your IDE run  BankingappApplication
## Use the REST endpoint from local deployment using swagger UI: 
http://localhost:8080/swagger-ui.html
![image](https://github.com/user-attachments/assets/40696843-9709-461c-b748-41005e3e5660)

### Future enhancements:  
* This application doesn't handle authentication nor authorization IAM access
* This application doesn't handle SSL/TLS 
* The domain design driven could be improved to have a dedicated entities to handle customers, accounts, transactons/operations ... instead of only one entity (account entity used here).
* The application could be containerized with Dockerfile to be deployed in container (docker-compose, kubernetes, ...)
* The application could be integrated in CI/CD pipeline via Jenkinsfile for example 


