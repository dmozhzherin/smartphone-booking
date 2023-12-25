
# Smartphone Booking Backend

## Description
A backend for a smartphone booking application. 
The application allows users to book and return smartphones.
The application is built in Kotlin using the Ktor framework.
The application uses a PostgreSQL database to store data.
The application provides a REST API for the frontend to use.
The API is documented using OpenAPI 3.0.
The API is "secured" using BASIC HTTP authentication.
The API provides endpoints for the following operations:
- List all smartphones and their availability
- List all bookings
- List all bookings for a specific user
- Book a smartphone
- Return a smartphone

The application is deployed using Docker and Docker Compose.

## Assumptions and Relaxed Requirements
- A user can book any number of smartphones at the same time.
- Any user can list all bookings, but only their own bookings can be returned.
- There are no limits on the number of list items returned by the API.


## Installation
### Prerequisites
- Java 17
- Docker
- Docker Compose

Build with Maven

Run the following commands to start the application:

 - change into directory **deployment**
 - run command **docker compose up**

## Known Issues
- This is my first experience with Ktor :-)