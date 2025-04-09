# Post-Service

This project is built using [Ktor](https://ktor.io), [PostgreSQL](https://www.postgresql.org/), and leverages **reactive connections** for asynchronous database interaction. It provides a RESTful API to manage cities with basic CRUD functionality.

## Features

Here's a list of the key features included in this project:

| Name                           | Description                                                                 |
|---------------------------------|-----------------------------------------------------------------------------|
| **Ktor Routing**                | Provides a flexible, structured routing DSL to define API endpoints.         |
| **Kotlinx Serialization**       | Handles JSON serialization using the `kotlinx.serialization` library.       |
| **Reactive Postgres Connection**| Uses [R2DBC](https://github.com/r2dbc/r2dbc) for asynchronous, reactive database access with PostgreSQL. |
| **CRUD Operations**             | Full CRUD functionality for managing cities: create, read, update, delete.  |
| **PostgreSQL**                  | Postgres database integration for storing city data, including async operations with reactive connection. |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| ------------------------------ | -------------------------------------------------------------------- |
| `./gradlew test`               | Run tests                                                            |
| `./gradlew build`              | Build the project                                                     |
| `./gradlew fatJar`             | Build an executable JAR with all dependencies included                |
| `./gradlew buildImage`         | Build a Docker image to run the server                               |
| `./gradlew publishImageToLocalRegistry` | Publish the Docker image to the local registry               |
| `./gradlew run`                | Run the server                                                        |
| `./gradlew runDocker`          | Run using the local Docker image                                      |

Once the server is running, you should see output like the following:


### Endpoints

The project includes basic CRUD operations for managing cities:

- **POST `/cities`** – Create a new city
- **GET `/cities/{id}`** – Get a city by its ID
- **PUT `/cities/{id}`** – Update a city by its ID
- **DELETE `/cities/{id}`** – Delete a city by its ID

---

## Configuration

The application reads from a configuration file (e.g., `application.yaml`) to set up the database connection. Example configuration for R2DBC:

```yaml
ktor:
  application:
    modules:
      - angrymiaucino.ApplicationKt.module
  deployment:
    port: 8080

r2dbc:
  url: "r2dbc:postgresql://root:123456@localhost:5432/postgres"
```