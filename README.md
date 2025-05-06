# Phonebook API

A simple REST API phonebook service built with Spring Boot and MongoDB.

## Features

- Full CRUD operations for contacts
- Pagination support (maximum 10 contacts per page)
- Search functionality across all contact fields
- Docker containerization for both the application and database
- Comprehensive error handling and logging

## Tech Stack

- Java 17
- Spring Boot 3.4.5
- MongoDB
- Docker

## API Documentation

### Get Contacts (with pagination and search)
```
GET /api/contacts?page=0&size=10&query={searchText}
```
- `page`: Page number (starts from 0)
- `size`: Number of contacts per page (configurable via `phonebook.pagination.max-page-size`, default max is 10)
- `query`: Optional search parameter
    - When provided, searches across firstName, lastName, phone, and address fields
    - When omitted, returns all contacts with pagination

#### Examples:
Get first page of all contacts (10 per page):
```
GET /api/contacts?page=0&size=10
```
#### Response format:
The response shows 10 contacts from a total of 15 contacts in the system, sorted by firstName in ascending order
```json
{
  "contacts": [
    {
      "firstName": "Alice",
      "lastName": "Smith",
      "phone": "123-456-7890",
      "address": "123 Oak St, New York, USA"
    },
    {
      "firstName": "Bob",
      "lastName": "Johnson",
      "phone": "234-567-8901",
      "address": "456 Pine St, Chicago, USA"
    },
    // ... more contacts
  ],
  "totalElements": 15
}
```

Search contacts containing "john" (paginated):
```
GET /api/contacts?page=0&size=10&query=john
```
#### Response format:
The response shows 10 contacts from a total of 42 contacts in the system, sorted by firstName in ascending
```json
{
  "contacts": [
    {
      "firstName": "Bob",
      "lastName": "Johnson",
      "phone": "050-555-0505",
      "address": "456 Main St, City, Country"
    },
    {
      "firstName": "John",
      "lastName": "Doe",
      "phone": "123-456-7890",
      "address": "123 Main St, City, Country"
    },
    // ... more contacts
  ],
  "totalElements": 42
}
```

### Create Contact
```
POST /api/contacts
```
Request body:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "123-456-7890",
  "address": "123 Main St, City, Country"
}
```

### Update Contact
```
PUT /api/contacts/{id}
```
Request body:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "123-456-7890",
  "address": "123 Main St, City, Country"
}
```

### Delete Contact
```
DELETE /api/contacts/{id}
```

## Running the Application

### Prerequisites
- Docker and Docker Compose installed on your machine

### Steps

1. Clone the repository:
```bash
git clone <repository-url>
cd phonebook-api
```

2. Build the application:
```bash
mvn clean package
```

3. Start the application with Docker Compose:
```bash
docker-compose up -build
```

4. The API will be available at:
```
http://localhost:8080/api/contacts
```

## Testing

Run the tests using Maven:
```bash
mvn test
```