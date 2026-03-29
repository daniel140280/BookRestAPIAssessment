# BooksRestful — Book REST API

A RESTful web service built with Java and deployed on Apache Tomcat. It exposes full CRUD operations on a Books database and supports three data exchange formats: **JSON**, **XML**, and **Plain Text**.

---

## Table of Contents

- [What This Project Does](#what-this-project-does)
- [Requirements](#requirements)
- [Project Structure](#project-structure)
- [Setup & Running Locally](#setup--running-locally)
- [API Reference](#api-reference)
- [Data Formats](#data-formats)
- [Design Patterns Used](#design-patterns-used)
- [Notable Features](#notable-features)

---

## What This Project Does

This is the **server-side** component of the Books web application. It provides a single API endpoint (`/Bookapi`) that the JavaScript frontend (BookUIAssignment) calls to:

- Retrieve all books
- Search books by title, genre, or year
- Add a new book
- Update an existing book
- Delete a book

All data is stored in a MySQL database hosted at MMU (Mudfoot).

---

## Requirements

Before running this project, make sure you have:

| Requirement | Version | Notes |
|---|---|---|
| Java JDK | 21 | [Download](https://adoptium.net/) |
| Apache Maven | 3.x | [Download](https://maven.apache.org/download.cgi) — or use the VS Code extension |
| Apache Tomcat | 9.x | [Download](https://tomcat.apache.org/download-90.cgi) |
| VS Code | Latest | With **Extension Pack for Java** and **Community Server Connectors** installed |
| MMU VPN / Campus WiFi | — | Required to reach the Mudfoot database |

---

## Project Structure

```
BookRestAPIAssessment/
├── src/main/java/mmu/ac/uk/
│   ├── controllers/
│   │   └── BooksAPIController.java   ← The main servlet — handles all HTTP requests
│   ├── database/
│   │   ├── BookDAO.java              ← All database queries (CRUD + search)
│   │   └── DatabaseConnection.java  ← Singleton JDBC connection
│   ├── filters/
│   │   └── CorsFilter.java          ← Allows the frontend (port 5173) to call this API
│   ├── helpers/
│   │   ├── BookSanitiser.java        ← Cleans book data before saving
│   │   ├── ContentTypeHelper.java    ← Normalises HTTP headers
│   │   └── DateHelper.java          ← Normalises date formats
│   ├── models/
│   │   ├── Book.java                 ← The Book data model
│   │   └── BookList.java             ← Wrapper used for XML serialisation
│   └── serialisationhandler/
│       ├── FormatHandler.java        ← Strategy interface
│       ├── FormatHandlerFactory.java ← Factory that picks the right handler
│       ├── JsonFormatHandler.java    ← Handles JSON
│       ├── XmlFormatHandler.java     ← Handles XML
│       └── PlainTextFormatHandler.java ← Handles plain text
├── src/main/webapp/WEB-INF/lib/      ← Bundled JAR dependencies
└── pom.xml                           ← Maven build file
```

---

## Setup & Running Locally

### Step 1 — Connect to MMU Network

The database is hosted on MMU's Mudfoot server. You must be on **campus WiFi or connected via MMU VPN** for the database to work.

### Step 2 — Build the WAR file

Open a terminal in the `BookRestAPIAssessment` folder and run:

```bash
mvn clean package
```

This compiles the Java code and produces a deployable file at:
```
target/BooksRestful.war
```

> A WAR file (Web Application Archive) is like a zip file that Tomcat knows how to deploy as a web app.

### Step 3 — Start Tomcat

In VS Code, open the **Servers** panel (added by Community Server Connectors):
1. Right-click your Tomcat 9 instance → **Start Server**
2. Confirm Tomcat is running by visiting `http://localhost:8080` — you should see the Tomcat welcome page

### Step 4 — Deploy the WAR

1. Go to `http://localhost:8080/manager/html` (log in with your Tomcat manager credentials)
2. Scroll down to **"WAR file to deploy"**
3. Click **Choose File** → select `target/BooksRestful.war`
4. Click **Deploy**

You should now see `/BooksRestful` listed as **running**.

### Step 5 — Test the API

Open Bruno, Postman, or your browser and make a request:

```
GET http://localhost:8080/BooksRestful/Bookapi
Accept: application/json
```

You should receive a JSON array of books.

---

## API Reference

All requests go to:
```
http://localhost:8080/BooksRestful/Bookapi
```

| Method | Purpose | Required Headers | Body |
|--------|---------|-----------------|------|
| `GET` | Get all books | `Accept: application/json` (or xml / text/plain) | None |
| `GET` | Search books | `Accept: ...` + query param `?q=searchTerm` | None |
| `POST` | Add a new book | `Content-Type: application/json` (or xml / text/plain) | Book data (no ID) |
| `PUT` | Update a book | `Content-Type: application/json` (or xml / text/plain) | Full book data (with ID) |
| `DELETE` | Delete a book | `Content-Type: application/json` (or xml / text/plain) | Book ID |

### Search with Pagination

```
GET /Bookapi?q=fantasy&page=1&limit=20
```

Searches across title, genre, and year.

---

## Data Formats

The API supports three formats. You control which one is used via the `Accept` header (for GET) or `Content-Type` header (for POST/PUT/DELETE).

### JSON (`application/json`)

```json
{
  "id": 1,
  "title": "The Kite Runner",
  "author": "Khaled Hosseini",
  "date": "29/05/2003",
  "genres": "Fiction, Drama",
  "characters": "Amir, Hassan",
  "synopsis": "A story of friendship and redemption in Afghanistan."
}
```

### XML (`application/xml`)

```xml
<books>
  <book>
    <id>1</id>
    <title>The Kite Runner</title>
    <author>Khaled Hosseini</author>
    <date>29/05/2003</date>
    <genres>Fiction, Drama</genres>
    <characters>Amir, Hassan</characters>
    <synopsis>A story of friendship and redemption in Afghanistan.</synopsis>
  </book>
</books>
```

### Plain Text (`text/plain`)

Fields separated by `##`, one book per line:
```
1##The Kite Runner##Khaled Hosseini##29/05/2003##Fiction, Drama##Amir, Hassan##A story of friendship and redemption in Afghanistan.
```

For POST (no ID), omit the first field. For DELETE, send only the ID.

---

## Design Patterns Used

This project demonstrates several software engineering design patterns:

### Strategy Pattern
`FormatHandler` is an interface with three concrete implementations (`JsonFormatHandler`, `XmlFormatHandler`, `PlainTextFormatHandler`). The servlet never knows which format it is using — it just calls `handler.serialise()` and `handler.deserialise()`. Swapping or adding a new format requires no changes to the servlet.

### Factory Pattern
`FormatHandlerFactory.getHandler(header)` takes the raw HTTP header value and returns the correct handler. All the decision logic is in one place. The servlet just calls the factory and uses whatever it gets back.

### Facade Pattern
`FormatHandlerFactory` also acts as a Facade — the servlet calls one method and gets a ready-to-use object back. All the complexity of choosing and creating the right handler is hidden behind that single call.

### Singleton Pattern
`DatabaseConnection` is implemented as a Java enum singleton. This ensures only one JDBC connection is created for the entire lifetime of the application, preventing the overhead of opening a new connection on every request.

### DAO Pattern (Data Access Object)
`BookDAO` separates all database logic from the rest of the application. The servlet never writes SQL — it just calls methods like `dao.getAllBooks()` or `dao.insertBook(book)`. This makes the database layer easy to swap or test independently.

---

## Notable Features

- **Three data formats** — JSON, XML, and Plain Text all fully supported for both reading and writing
- **Content negotiation** — the API uses the `Accept` and `Content-Type` headers to automatically serve the right format
- **Server-side validation** — all book fields are validated before touching the database (empty fields, date format, field length limits)
- **SQL injection prevention** — all database queries use `PreparedStatement` with parameterised inputs, never raw string concatenation
- **Pagination** — search results support `page` and `limit` query parameters
- **CORS support** — a `CorsFilter` allows the frontend running on `localhost:5173` to communicate with this API during local development
